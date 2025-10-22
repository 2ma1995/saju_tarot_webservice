package service.saju_taro_service.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import service.saju_taro_service.global.config.JwtTokenProvider;
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

    public AuthResponse login(AuthRequest req) {
        User user = userRepository.findByEmailAndIsActiveTrue(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }
        String token = jwtTokenProvider.createToken(user.getId(), user.getUserRole().name());
        return new AuthResponse(token, user.getId(), user.getUserRole().name());
    }
}
