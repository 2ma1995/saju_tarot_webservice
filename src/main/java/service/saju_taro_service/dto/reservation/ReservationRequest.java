package service.saju_taro_service.dto.reservation;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationRequest {
    private Long counselorId; // 상담사 ID
    private Long serviceItemId; //서비스 ID
    private Long scheduleId; // 예약할 스케줄 ID
    private LocalDateTime reservationTime;
    private String note; // 메모
}
