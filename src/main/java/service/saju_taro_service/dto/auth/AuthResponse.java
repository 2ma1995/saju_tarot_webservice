package service.saju_taro_service.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long userId;
    private String role;
}
