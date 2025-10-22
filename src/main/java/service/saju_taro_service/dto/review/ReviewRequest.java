package service.saju_taro_service.dto.review;

import lombok.Data;

@Data
public class ReviewRequest {
    private Long reservationId;
    private int rating;
    private String comment;
}
