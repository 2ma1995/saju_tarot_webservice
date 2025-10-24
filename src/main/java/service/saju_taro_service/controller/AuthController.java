package service.saju_taro_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.saju_taro_service.dto.auth.AuthRequest;
import service.saju_taro_service.global.exception.CustomException;
import service.saju_taro_service.global.exception.ErrorCode;
import service.saju_taro_service.global.util.SecurityUtil;
import service.saju_taro_service.service.auth.AuthService;
import service.saju_taro_service.service.auth.RefreshTokenService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    // Access Token 재발급
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestParam Long userId,
                                                @RequestParam String refreshToken) {
        String newAccessToken = authService.refreshAccessToken(userId, refreshToken);
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    //로그아웃
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
