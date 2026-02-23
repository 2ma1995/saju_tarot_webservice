package service.saju_taro_service.controller;

import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.saju_taro_service.dto.auth.AuthRequest;
import service.saju_taro_service.dto.user.RefreshTokenRequest;
import service.saju_taro_service.global.exception.CustomException;
import service.saju_taro_service.global.exception.ErrorCode;
import service.saju_taro_service.global.util.SecurityUtil;
import service.saju_taro_service.service.auth.AuthService;
import service.saju_taro_service.service.auth.RefreshTokenService;

import java.util.Map;

@Tag(name = "Auth API", description = "JWT 기반 인증/인가 기능 (로그인, 토큰 재발급, 로그아웃)")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
        private final AuthService authService;
        private final RefreshTokenService refreshTokenService;

        // 로그인
        @Operation(summary = "로그인", description = """
                        사용자가 이메일과 비밀번호를 입력해 로그인합니다.
                        성공 시 Access Token과 Refresh Token을 반환합니다.
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(mediaType = "application/json", schema = @Schema(example = """
                                        {
                                          "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                          "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                          "user": {
                                            "id": 1,
                                            "email": "admin@test.com",
                                            "name": "관리자",
                                            "role": "ADMIN"
                                          }
                                        }
                                        """))),
                        @ApiResponse(responseCode = "401", description = "잘못된 이메일 또는 비밀번호", content = @Content)
        })
        @PostMapping("/login")
        public ResponseEntity<?> login(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "로그인 요청 DTO", required = true, content = @Content(schema = @Schema(example = """
                                        {
                                          "email": "admin@test.com",
                                          "password": "test1234!"
                                        }
                                        """))) @RequestBody AuthRequest req) {
                return ResponseEntity.ok(authService.login(req));
        }

        // Access Token 재발급
        @Operation(summary = "Access Token 재발급", description = """
                        Refresh Token을 사용해 새로운 Access Token을 발급받습니다.
                        만료된 Access Token으로 인증이 필요할 때 사용합니다.
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "토큰 재발급 성공", content = @Content(mediaType = "application/json", schema = @Schema(example = """
                                        {
                                          "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                                        }
                                        """))),
                        @ApiResponse(responseCode = "400", description = "Refresh Token이 유효하지 않음", content = @Content),
                        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
        })
        @PostMapping("/refresh")
        public ResponseEntity<?> refreshAccessToken(@Valid @RequestBody RefreshTokenRequest req) {
                String newAccessToken = authService.refreshAccessToken(req.getRefreshToken());
                return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        }

        // 로그아웃
        @Operation(summary = "로그아웃", description = """
                        현재 로그인한 사용자의 Refresh Token을 삭제하여 로그아웃 처리합니다.
                        헤더에 JWT Access Token이 필요합니다.
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "로그아웃 완료", content = @Content(mediaType = "application/json", schema = @Schema(example = "\"로그아웃이 완료되었습니다.\""))),
                        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content)
        })
        @PostMapping("/logout")
        public ResponseEntity<?> logout() {
                Long userId = SecurityUtil.currentUserId();
                if (userId == null) {
                        throw new CustomException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");
                }
                refreshTokenService.deleteRefreshToken(userId);
                return ResponseEntity.ok("로그아웃이 완료되었습니다.");
        }

}
