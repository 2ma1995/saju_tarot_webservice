package service.saju_taro_service.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import service.saju_taro_service.dto.user.UserResponse;

@Getter @AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private UserResponse user;
}
