package service.saju_taro_service.dto.review;

import lombok.Getter;
import lombok.Setter;
import service.saju_taro_service.domain.review.Review;
import service.saju_taro_service.domain.user.User;

import java.time.LocalDateTime;

@Getter @Setter
public class ReviewResponse {
    private Long id;
    private Long userId;
    private String nickname;
    private Long reservationId;
    private Long counselorId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    public static ReviewResponse fromEntity(Review review, User user) {
        ReviewResponse res = new ReviewResponse();
        res.setId(review.getId());
        res.setUserId(review.getUser().getId());
        res.setNickname(user != null ? user.getName() : "탈퇴한 사용자");
        res.setReservationId(review.getReservation().getId());
        res.setCounselorId(review.getCounselor().getId());
        res.setRating(review.getRating());
        res.setComment(review.getComment());
        res.setCreatedAt(review.getCreatedAt());
        return res;
    }
}
