package service.saju_taro_service.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequest {
    @NotBlank(message = "refreshToken은 필수입니다.")
    private String refreshToken;
}
