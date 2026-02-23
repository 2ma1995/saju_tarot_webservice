package service.saju_taro_service.service.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import service.saju_taro_service.domain.payment.Payment;
import service.saju_taro_service.domain.payment.PaymentStatus;
import service.saju_taro_service.domain.reservation.Reservation;
import service.saju_taro_service.domain.reservation.ReservationStatus;
import service.saju_taro_service.domain.schedule.Schedule;
import service.saju_taro_service.repository.PaymentRepository;
import service.saju_taro_service.repository.ReservationRepository;
import service.saju_taro_service.repository.ScheduleRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
// 환불로직
public class PaymentScheduler {
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final ScheduleRepository scheduleRepository;

    /**
     * ✅ 매 1시간마다 미완료 예약 자동 환불
     */
    @Scheduled(cron = "0 0 * * * *") // 매 정시마다 실행
    @Transactional
    public void autoRefundUncompletedPayments() {
        log.info("🔁 [Scheduler] 자동 환불 검증 시작...");

        // ✅ DB에서 PAID 상태이며 24시간 경과한 결제만 조회 (성능 개선)
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        List<Payment> expiredPayments = paymentRepository.findExpiredPaidPayments(cutoffTime);

        int refundCount = 0;

        for (Payment payment : expiredPayments) {
            Reservation reservation = reservationRepository.findById(payment.getReservation().getId()).orElse(null);
            if (reservation == null)
                continue;

            // 이미 완료된 상담은 환불하지 않음
            if (reservation.getReservationStatus() == ReservationStatus.COMPLETED)
                continue;

            log.info("[AUTO REFUND] 예약 ID: {} / 결제 ID: {} / 상태변경: PAID -> REFUND", reservation.getId(),
                    payment.getId());
            payment.setPaymentStatus(PaymentStatus.REFUND);
            reservation.setReservationStatus(ReservationStatus.CANCELLED);

            // ✅ 스케줄 복구 (다시 예약 가능 상태로)
            if (reservation.getSchedule() != null) {
                Schedule schedule = reservation.getSchedule();
                schedule.setAvailable(true);
                scheduleRepository.save(schedule);
                log.info("[AUTO REFUND] 스케줄 복구 완료 - scheduleId: {}", schedule.getId());
            }

            paymentRepository.save(payment);
            reservationRepository.save(reservation);
            refundCount++;
        }
        log.info("✅ [Scheduler 완료] 자동 환불 처리 건수 = {}", refundCount);
    }
}
