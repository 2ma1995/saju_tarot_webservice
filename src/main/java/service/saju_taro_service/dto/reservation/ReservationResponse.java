package service.saju_taro_service.dto.reservation;

import lombok.Data;
import service.saju_taro_service.domain.reservation.Reservation;
import service.saju_taro_service.domain.reservation.ReservationStatus;

import java.time.LocalDateTime;

@Data
public class ReservationResponse {
    private Long id;
    private Long userId;
    private Long counselorId;
    private Long serviceItemId;
    private LocalDateTime reservationTime;
    private ReservationStatus status;
    private String note;

    public static ReservationResponse fromEntity(Reservation r){
        ReservationResponse res = new ReservationResponse();
        res.setId(r.getId());
        res.setUserId(r.getUserId());
        res.setCounselorId(r.getCounselorId());
        res.setServiceItemId(r.getServiceItemId());
        res.setReservationTime(r.getReservationTime());
        res.setStatus(r.getReservationStatus());
        res.setNote(r.getNote());
        return res;
    }
}