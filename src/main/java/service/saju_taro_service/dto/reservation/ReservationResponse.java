package service.saju_taro_service.dto.reservation;

import lombok.Data;
import service.saju_taro_service.domain.reservation.Reservation;
import service.saju_taro_service.domain.reservation.ReservationStatus;

import java.time.LocalDateTime;

@Data
public class ReservationResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userPhone;

    private Long counselorId;
    private String counselorName;

    private Long serviceItemId;

    private LocalDateTime reservationTime;
    private ReservationStatus status;
    private String note;

    public static ReservationResponse fromEntity(Reservation r){
        ReservationResponse res = new ReservationResponse();
        res.setId(r.getId());
       // 유저
        if (r.getUser() != null) {
            res.setUserId(r.getUser().getId());
            res.setUserName(r.getUser().getName());
            res.setUserPhone(r.getUser().getPhone());
        }
        // 상담사
        if (r.getCounselor() != null) {
            res.setCounselorId(r.getCounselor().getId());
            res.setCounselorName(r.getCounselor().getName());
        }

        // 서비스 항목
        if (r.getServiceItem() != null) {
            res.setServiceItemId(r.getServiceItem().getId());
        }
        res.setReservationTime(r.getReservationTime());
        res.setStatus(r.getReservationStatus());
        res.setNote(r.getNote());
        return res;
    }

    /** ✅ 상담사용 - 사용자 이름/전화 포함 버전 */
    public static ReservationResponse fromEntityWithUser(Reservation r, String name, String phone) {
        ReservationResponse res = fromEntity(r);
        res.setUserName(name);
        res.setUserPhone(phone);
        return res;
    }
}