package service.saju_taro_service.dto.profile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileRequest {
    private String bio;
    private String experience;
    private String tags;
    private String imageUrl;
}
