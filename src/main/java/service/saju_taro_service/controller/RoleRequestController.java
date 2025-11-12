package service.saju_taro_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.saju_taro_service.domain.role.RoleRequest;
import service.saju_taro_service.dto.role.RoleRequestResponse;
import service.saju_taro_service.global.exception.CustomException;
import service.saju_taro_service.global.exception.ErrorCode;
import service.saju_taro_service.global.util.SecurityUtil;
import service.saju_taro_service.service.role.RoleRequestService;

import java.util.List;

@Tag(name = "Role Request API", description = "사용자 → 상담사 역할 전환 및 관리자 승인/거절 기능")
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleRequestController {
    private final RoleRequestService roleRequestService;

    /**
     * 사용자 → 상담사 신청
     */
    @Operation(
            summary = "상담사 역할 신청",
            description = """
                    일반 사용자가 상담사로 전환을 요청합니다.  
                    로그인된 사용자 ID를 기반으로 자동 신청됩니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상담사 신청 완료",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "\"상담사 신청이 접수되었습니다.\""))),
            @ApiResponse(responseCode = "401", description = "로그인이 필요함", content = @Content)
    })
    @PostMapping("/request")
    public ResponseEntity<?> requestCounselorRole() {
        Long userId = SecurityUtil.currentUserId();
        roleRequestService.requestCounselorRole(userId);
        return ResponseEntity.ok("상담사 신청이 접수되었습니다.");
    }

    /** 관리자 → 대기 요청 조회 */
    @Operation(
            summary = "상담사 신청 대기 목록 조회 (ADMIN 전용)",
            description = """
                    관리자가 상담사 전환 요청 중 대기 상태인 신청 내역을 조회합니다.  
                    승인 또는 거절 전 검토 단계에서 사용됩니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    [
                                      {
                                        "requestId": 1,
                                        "userName": "홍길동",
                                        "userEmail": "hong@email.com",
                                        "userNumber": "010-1234-5678",
                                        "requestedAt": "2025-10-26T12:34:56",
                                        "status": "PENDING"
                                      }
                                    ]
                                    """))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content)
    })
    @GetMapping("/requests")
    public ResponseEntity<?> getPendingRequests() {
        validateAdmin();
        List<RoleRequest> list = roleRequestService.getPendingRequests();
        List<RoleRequestResponse> dto = list.stream()
                .map(RoleRequestResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(dto);
    }

    /**
     * 관리자 → 승인
     */
    @Operation(
            summary = "상담사 신청 승인 (ADMIN 전용)",
            description = """
                    관리자가 특정 상담사 신청 요청을 승인합니다.  
                    승인 시 해당 사용자의 권한이 COUNSELOR로 변경됩니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "승인 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "\"요청이 승인되었습니다.\""))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당 요청 ID를 찾을 수 없음", content = @Content)
    })
    @PostMapping("/approve/{requestId}")
    public ResponseEntity<?> approve(
            @Parameter(description = "승인할 요청 ID", example = "1")
            @PathVariable Long requestId) {
        validateAdmin();
        roleRequestService.approveRequest(requestId);
        return ResponseEntity.ok("요청이 승인되었습니다.");
    }

    /**
     * 관리자 → 거절
     */
    @Operation(
            summary = "상담사 신청 거절 (ADMIN 전용)",
            description = """
                    관리자가 특정 상담사 신청 요청을 거절합니다.  
                    거절 시 해당 요청 상태가 REJECTED로 변경됩니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "거절 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "\"요청이 거절되었습니다.\""))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당 요청 ID를 찾을 수 없음", content = @Content)
    })
    @PostMapping("/reject/{requestId}")
    public ResponseEntity<?> reject(
            @Parameter(description = "거절할 요청 ID",example = "1")
            @PathVariable Long requestId) {
        validateAdmin();
        roleRequestService.rejectRequest(requestId);
        return ResponseEntity.ok("요청이 거절되었습니다.");
    }

    private void validateAdmin() {
        String role = SecurityUtil.currentRole();
        if (!"ADMIN".equals(role)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

}
