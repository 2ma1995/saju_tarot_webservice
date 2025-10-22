package service.saju_taro_service.controller.reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.saju_taro_service.domain.reservation.ReservationStatus;
import service.saju_taro_service.dto.reservation.ReservationRequest;
import service.saju_taro_service.global.util.SecurityUtil;
import service.saju_taro_service.service.service.ReservationService;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;

    // 예약 생성
    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody ReservationRequest req) {
        return ResponseEntity.ok(reservationService.createReservation(req));
    }

    // 내 예약 목록 조회
    @GetMapping("/my")
    public ResponseEntity<?> getMyReservations() {
        Long userId = SecurityUtil.currentUserId();
        return ResponseEntity.ok(reservationService.getUserReservations(userId));
    }

    // 예약 취소
    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long reservationId) {
        reservationService.cancelReservation(reservationId);
        return ResponseEntity.ok("예약이 취소되었습니다.");
    }

    // 예약 상태 변경
    @PutMapping("/{reservationId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long reservationId,
                                          @RequestParam ReservationStatus status) {
        reservationService.updateStatus(reservationId, status);
        return ResponseEntity.ok("예약 상태가 변경되었습니다.");
    }
}
