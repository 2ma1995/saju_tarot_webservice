package service.saju_taro_service.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import service.saju_taro_service.domain.user.User;

@Getter
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;

    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getUserRole().name()
        );
    }
}
