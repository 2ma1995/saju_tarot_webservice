package service.saju_taro_service.dto.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import service.saju_taro_service.domain.user.User;
import service.saju_taro_service.dto.review.ReviewResponse;

import java.util.List;

@Getter
@Setter
@Builder
public class CounselorDetailResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Double averageRating;
    private Integer reviewCount;
    private List<ReviewResponse> reviews;

    public static CounselorDetailResponse fromEntity(User user, List<ReviewResponse> reviews) {
        return CounselorDetailResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .averageRating(user.getAverageRating())
                .reviewCount(user.getReviewCount())
                .reviews(reviews)
                .build();
    }
}

