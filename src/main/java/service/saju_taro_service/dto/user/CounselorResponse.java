package service.saju_taro_service.dto.user;

import lombok.Getter;
import lombok.Setter;
import service.saju_taro_service.domain.user.User;

@Getter @Setter
public class CounselorResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Double averageRating;
    private Integer reviewCount;

    public static CounselorResponse fromEntity(User user){
        CounselorResponse dto = new CounselorResponse();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAverageRating(user.getAverageRating());
        dto.setReviewCount(user.getReviewCount());
        return dto;
    }

}
