package service.saju_taro_service.controller.reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.saju_taro_service.domain.reservation.ReservationStatus;
import service.saju_taro_service.dto.reservation.ReservationRequest;
import service.saju_taro_service.global.exception.CustomException;
import service.saju_taro_service.global.exception.ErrorCode;
import service.saju_taro_service.global.util.SecurityUtil;
import service.saju_taro_service.service.reservation.ReservationService;

import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // 예약 생성
    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody ReservationRequest req) {
        Long userId = SecurityUtil.currentUserId();
        return ResponseEntity.ok(reservationService.createReservation(userId, req));
    }

    // 내 예약 목록 조회
    @GetMapping("/my")
    public ResponseEntity<?> getMyReservations(@RequestParam(required = false) String status) {
        String role = SecurityUtil.currentRole();
        if (!"USER".equals(role)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "사용자만 접근 가능합니다.");
        }

        Long userId = SecurityUtil.currentUserId();
        return ResponseEntity.ok(reservationService.getUserReservations(userId, status));
    }

    /** ✅ 상담사 – 특정 날짜 예약목록 조회 (달력 클릭 시 호출) */
    @GetMapping("/counselor/day")
    public ResponseEntity<?> getCounselorReservationsByDay(@RequestParam String date) {
        String role = SecurityUtil.currentRole();
        if (!"COUNSELOR".equals(role)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "상담사만 접근 가능합니다.");
        }

        Long counselorId = SecurityUtil.currentUserId();
        return ResponseEntity.ok(reservationService.getCounselorReservationsByDay(counselorId, date));
    }

    /**
     * ✅ 상담사 전용 - 예약 상태 업데이트
     * 예시: PUT /api/reservations/12/status
     * Body: { "status": "COMPLETED" }
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateReservationStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String role = SecurityUtil.currentRole();
        if (!"COUNSELOR".equals(role)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "상담사만 상태를 변경할 수 있습니다.");
        }

        String statusStr = body.get("status");
        if (statusStr == null || statusStr.isBlank()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "status 값이 필요합니다.");
        }

        ReservationStatus newStatus;
        try {
            newStatus = ReservationStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "유효하지 않은 상태값입니다.");
        }

        reservationService.updateStatus(id, newStatus);
        return ResponseEntity.ok(Map.of(
                "message", "예약 상태가 " + newStatus.name() + " 으로 변경되었습니다."
        ));
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
        String role = SecurityUtil.currentRole();
        if (!"COUNSELOR".equals(role) && !"ADMIN".equals(role)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        reservationService.updateStatus(reservationId, status);
        return ResponseEntity.ok("예약 상태가 변경되었습니다.");
    }
}
