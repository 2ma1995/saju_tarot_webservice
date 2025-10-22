package service.saju_taro_service.service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.saju_taro_service.domain.reservation.Reservation;
import service.saju_taro_service.domain.reservation.ReservationStatus;
import service.saju_taro_service.dto.reservation.ReservationRequest;
import service.saju_taro_service.dto.reservation.ReservationResponse;
import service.saju_taro_service.global.util.SecurityUtil;
import service.saju_taro_service.repository.ReservationRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;

    /**
     * ✅ 예약 생성
     **/
    @Transactional
    public ReservationResponse createReservation(ReservationRequest req) {
        Long userId = SecurityUtil.currentUserId();
        if (userId == null) throw new SecurityException("로그인이 필요합니다.");

        Reservation reservation = Reservation.builder()
                .userId(userId)
                .counselorId(req.getCounselorId())
                .serviceItemId(req.getServiceItemId())
                .reservationTime(req.getReservationTime())
                .note(req.getNote())
                .reservationStatus(ReservationStatus.RESERVED)
                .build();

        return ReservationResponse.fromEntity(reservationRepository.save(reservation));
    }

    /**
     * ✅ 사용자 예약 목록 조회
     **/
    @Transactional(readOnly = true)
    public List<ReservationResponse> getUserReservations(Long userId) {
        return reservationRepository.findByUserIdOrderByReservationTimeDesc(userId)
                .stream().map(ReservationResponse::fromEntity).toList();
    }

    /** ✅ 상담사 예약 목록 조회 **/
    @Transactional(readOnly = true)
    public List<ReservationResponse> getCounselorReservations(Long counselorId) {
        return reservationRepository.findByCounselorIdOrderByReservationTimeDesc(counselorId)
                .stream().map(ReservationResponse::fromEntity).toList();
    }

    /** ✅ 예약 취소 */
    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
        if (r.getReservationStatus() == ReservationStatus.COMPLETED)
            throw new IllegalStateException("이미 완료된 예약은 취소할 수 없습니다.");

        r.setReservationStatus(ReservationStatus.CANCELED);
    }

    /** ✅ 상태 업데이트 (상담사용) */
    @Transactional
    public void updateStatus(Long reservationId, ReservationStatus newStatus) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
        r.setReservationStatus(newStatus);
    }
}