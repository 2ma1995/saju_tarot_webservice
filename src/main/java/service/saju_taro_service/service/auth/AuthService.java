package service.saju_taro_service.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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

    public AuthResponse login(AuthRequest req) {
        User user = userRepository.findByEmailAndIsActiveTrue(req.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createToken(user.getId(), user.getUserRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken();

        // redis에 refreshToken저장 (기존 데이터 덮어쓰기)
        refreshTokenService.saveRefreshToken(user.getId(), refreshToken);

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getUserRole().name(),
                accessToken,
                refreshToken
        );
    }

    public String refreshAccessToken(Long userId, String refreshToken) {
        if (!refreshTokenService.validateRefreshToken(userId, refreshToken)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "리프레시 토큰이 유효하지 않습니다. 다시로그인하세요");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        return jwtTokenProvider.createToken(user.getId(), user.getUserRole().name());
    }
}
