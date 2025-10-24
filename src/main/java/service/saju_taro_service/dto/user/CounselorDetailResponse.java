package service.saju_taro_service.dto.user;

import lombok.Getter;
import lombok.Setter;
import service.saju_taro_service.domain.user.User;
import service.saju_taro_service.dto.review.ReviewResponse;

import java.util.List;

@Getter
@Setter
public class CounselorDetailResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Double averageRating;
    private Integer reviewCount;
    private List<ReviewResponse> reviews;

    public static CounselorDetailResponse fromEntity(User user, List<ReviewResponse> reviews) {
        CounselorDetailResponse dto = new CounselorDetailResponse();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAverageRating(user.getAverageRating());
        dto.setReviewCount(user.getReviewCount());
        dto.setReviews(reviews);
        return dto;
    }
}

