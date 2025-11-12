package service.saju_taro_service.dto.profile;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProfileRequest {
    private String bio;
    private String experience;
    private List<String> tags;
    private String imageUrl;
}
