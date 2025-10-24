package service.saju_taro_service.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class AuthResponse {
    private Long id;
    private String name;
    private String role;
    private String token;
    private String refreshToken;
}
