package service.saju_taro_service.dto.review;

import lombok.Data;
import service.saju_taro_service.domain.review.Review;

import java.time.LocalDateTime;

@Data
public class ReviewResponse {
    private Long id;
    private Long userId;
    private Long reservationId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    public static ReviewResponse fromEntity(Review review) {
        ReviewResponse res = new ReviewResponse();
        res.setId(review.getId());
        res.setUserId(review.getUserId());
        res.setReservationId(review.getReservationId());
        res.setRating(review.getRating());
        res.setComment(review.getComment());
        res.setCreatedAt(review.getCreatedAt());
        return res;
    }
}
