package service.saju_taro_service.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.saju_taro_service.domain.user.UserRole;
import service.saju_taro_service.dto.user.UserResponse;
import service.saju_taro_service.global.exception.CustomException;
import service.saju_taro_service.global.exception.ErrorCode;
import service.saju_taro_service.global.util.SecurityUtil;
import service.saju_taro_service.service.admin.AdminUserService;

@Tag(name = "Admin API", description = "관리자 전용 기능")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final AdminUserService adminUserService;

    /** ✅ 관리자: 사용자 목록 조회 */
    @Operation(
            summary = "사용자 목록 조회",
            description = """
                    관리자 전용 사용자 조회 기능입니다.  
                    role(권한) 또는 active(활성화 상태)로 필터링할 수 있으며,  
                    페이지네이션(page, size)을 지원합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "content": [
                                        {
                                          "id": 1,
                                          "name": "소지원",
                                          "email": "sojiwon@example.com",
                                          "role": "USER",
                                          "isActive": true
                                        },
                                        {
                                          "id": 2,
                                          "name": "혜림",
                                          "email": "hyelim@example.com",
                                          "role": "COUNSELOR",
                                          "isActive": true
                                        }
                                      ],
                                      "pageable": {
                                        "pageNumber": 0,
                                        "pageSize": 10
                                      },
                                      "totalElements": 42,
                                      "totalPages": 5
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content)
    })
    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @Parameter(description = "필터링할 역할 (예: USER, COUNSELOR, ADMIN)", example = "USER")
            @RequestParam(required = false) String role,
            @Parameter(description = "활성 상태 (true=활성, false=비활성)", example = "true")
            @RequestParam(required = false) Boolean active,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지당 표시 수", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        validateAdmin();
        Page<UserResponse> users = adminUserService.getAllUsers(role, active, page, size);
        return ResponseEntity.ok(users);
    }

    /** ✅ 관리자: 사용자 역할 변경 */
    @Operation(
            summary = "사용자 역할 변경",
            description = """
                    관리자가 특정 사용자의 권한(Role)을 변경합니다.  
                    가능한 값: USER, COUNSELOR, ADMIN
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "역할 변경 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "\"사용자 역할이 변경되었습니다.\""))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 역할값", content = @Content)
    })
    @PutMapping("/{userId}/role")
    public ResponseEntity<?> changeRole(@Parameter(description = "대상 사용자 ID", example = "5") @PathVariable Long userId,
                                        @Parameter(description = "변경할 역할", example = "COUNSELOR") @RequestParam String newRole
    ) {
        validateAdmin();
        adminUserService.changeUserRole(userId, UserRole.valueOf(newRole));
        return ResponseEntity.ok("사용자 역할이 변경되었습니다.");
    }

    /** ✅ 관리자: 사용자 비활성화 */
    @Operation(
            summary = "사용자 비활성화",
            description = """
                    관리자가 특정 사용자를 비활성화 처리합니다.  
                    비활성화된 사용자는 로그인 및 서비스 이용이 불가능합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비활성화 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "\"사용자가 비활성화되었습니다.\""))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content)
    })
    @PutMapping("/{userId}/deactivate")
    public ResponseEntity<?> deactivate(
            @Parameter(description = "비활성화할 사용자 ID", example = "5") @PathVariable Long userId
    ) {
        validateAdmin();
        adminUserService.deactivateUser(userId);
        return ResponseEntity.ok("사용자가 비활성화되었습니다.");
    }

    /** ✅ 관리자: 다른 사용자에게 관리자 권한 부여 */
    @Operation(
            summary = "관리자 권한 부여",
            description = """
                    기존 관리자가 다른 사용자에게 ADMIN 권한을 부여합니다.  
                    요청자 본인의 ID(SecurityUtil.currentUserId())를 함께 기록합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "권한 부여 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "\"해당 사용자에게 관리자 권한이 부여되었습니다.\""))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content)
    })
    @PostMapping("/{targetId}/promote")
    public ResponseEntity<?> promoteToAdmin(
            @Parameter(description = "관리자로 승격할 사용자 ID", example = "7") @PathVariable Long targetId
    ) {
        validateAdmin();
        Long requesterId = SecurityUtil.currentUserId();
        adminUserService.promoteToAdmin(targetId, requesterId);
        return ResponseEntity.ok("해당 사용자에게 관리자 권한이 부여되었습니다.");
    }

    /** 관리자 권한 검증 */
    private void validateAdmin() {
        String role = SecurityUtil.currentRole();
        if (!"ADMIN".equals(role)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }
}
