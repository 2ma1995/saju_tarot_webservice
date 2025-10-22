package service.saju_taro_service.dto.reservation;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationRequest {
    private Long counselorId;
    private Long serviceItemId;
    private LocalDateTime reservationTime;
    private String note;
}
