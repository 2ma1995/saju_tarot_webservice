package service.saju_taro_service.service.auth;

import com.nimbusds.oauth2.sdk.token.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.saju_taro_service.dto.user.UserResponse;
import service.saju_taro_service.global.exception.CustomException;
import service.saju_taro_service.global.exception.ErrorCode;
import service.saju_taro_service.global.jwt.JwtTokenProvider;
import service.saju_taro_service.domain.user.User;
import service.saju_taro_service.dto.auth.AuthRequest;
import service.saju_taro_service.dto.auth.AuthResponse;
import service.saju_taro_service.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse login(AuthRequest req) {
        User user = userRepository.findByEmailAndIsActiveTrue(req.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD, "비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createToken(user.getId(), user.getUserRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken();

        // Redis에 refreshToken 저장 (기존 토큰 자동 덮어쓰기)
        refreshTokenService.saveRefreshToken(user.getId(), refreshToken);

        return new AuthResponse(
                accessToken,
                refreshToken,
                UserResponse.fromEntity(user)
        );
    }

    public String refreshAccessToken(String refreshTokenStr) {
        // RefreshToken 검증 및 userId 추출
        Long userId = refreshTokenService.validateAndGetUserId(refreshTokenStr);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 새로운 AccessToken 생성
        return jwtTokenProvider.createToken(user.getId(), user.getUserRole().name());
    }
}
