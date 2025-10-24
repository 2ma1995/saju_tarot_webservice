package service.saju_taro_service.dto.review;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReviewRequest {
    private Long reservationId;
    private int rating;
    private String comment;
}
