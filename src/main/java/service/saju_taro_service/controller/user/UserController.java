package service.saju_taro_service.controller.user;

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
import service.saju_taro_service.domain.user.User;
import service.saju_taro_service.dto.user.UserResponse;
import service.saju_taro_service.dto.user.UserSignupRequest;
import service.saju_taro_service.dto.user.UserUpdateRequest;
import service.saju_taro_service.global.exception.CustomException;
import service.saju_taro_service.global.exception.ErrorCode;
import service.saju_taro_service.global.util.SecurityUtil;
import service.saju_taro_service.repository.UserRepository;
import service.saju_taro_service.service.user.UserService;

import java.util.List;
import java.util.Map;

@Tag(name = "User API", description = "사용자 회원가입, 조회, 수정, 탈퇴 및 FCM 토큰 관리")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * 회원가입
     **/
    @Operation(summary = "회원가입", description = "사용자가 이메일, 비밀번호, 이름, 연락처를 입력하여 회원가입을 수행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 요청", content = @Content),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일", content = @Content)
    })
    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "회원가입 요청 데이터",
                    required = true,
                    content = @Content(
                            schema = @Schema(example = """
                                {
                                  "name": "홍길동",
                                  "nickname": "타로마스터",
                                  "email": "hong@example.com",
                                  "password": "1234abcd!",
                                  "phone": "010-1234-5678"
                                }
                                """))
            )
            @RequestBody UserSignupRequest req) {
        return ResponseEntity.ok(userService.signup(req));
    }

    /**
     * 사용자 조회
     **/
    @Operation(summary = "단일 사용자 조회", description = "사용자 ID로 특정 사용자의 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없음", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@Parameter(description = "조회할 사용자 ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    /**
     * 전체 사용자 조회 (관리자)
     **/
    @Operation(summary = "전체 사용자 조회 (ADMIN 전용)", description = "관리자가 전체 사용자 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        validateAdmin();
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * 사용자 정보 수정
     **/
    @Operation(summary = "사용자 정보 수정", description = "사용자가 본인의 정보를 수정합니다. (이름, 연락처 등)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@Parameter(description = "수정할 사용자 ID", example = "1") @PathVariable Long id, @RequestBody UserUpdateRequest req) {
        Long tokenUserId = SecurityUtil.currentUserId();
        String tokenRole = SecurityUtil.currentRole();
        return ResponseEntity.ok(userService.updateUser(id, req, tokenUserId, tokenRole));
    }

    /**
     * 사용자 비활성화(소프트 딜리트)
     **/
    @Operation(summary = "회원 탈퇴", description = "사용자가 본인의 계정을 비활성화(soft delete) 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 완료"),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivateUser(@Parameter(description = "비활성화할 사용자 ID", example = "1") @PathVariable Long id) {
        Long tokenUserId = SecurityUtil.currentUserId();
        String tokenRole = SecurityUtil.currentRole();
        userService.deactivateUser(id, tokenUserId, tokenRole);
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }

    /**
     * FCM 토큰 업데이트
     **/
    @Operation(summary = "FCM 토큰 업데이트", description = "사용자가 알림용 FCM 토큰을 등록 또는 갱신합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 업데이트 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (fcmToken 누락)", content = @Content)
    })
    @PutMapping("/fcm-token")
    public ResponseEntity<Map<String, Object>> updateFcmToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "업데이트할 FCM토큰",
                    required = true,
                    content = @Content(schema = @Schema(example = "{\"fcmToken\": \"abcd1234xyz\"}"))
            )
            @RequestBody Map<String, String> body) {
        Long userId = SecurityUtil.currentUserId();
        String token = body.get("fcmToken");
        if (token == null || token.isBlank()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "fcmToken 값이 비어있습니다.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        user.updateFcmToken(token);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("updated", true, "userId", user.getId(), "fcmToken", token));
    }

    // 관리자 권한 확인
    private void validateAdmin() {
        String role = SecurityUtil.currentRole();
        if (!"ADMIN".equals(role)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }
}
