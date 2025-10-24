package service.saju_taro_service.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupRequest {
    private String name;
    private String nickname;
    private String email;
    private String password;
    private String phone;
}
