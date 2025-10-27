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
import service.saju_taro_service.repository.PaymentRepository;
import service.saju_taro_service.repository.ReservationRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
//환불로직
public class PaymentScheduler {
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;

    /**
     * ✅ 매 1시간마다 미완료 예약 자동 환불
     */
    @Scheduled(cron = "0 0 * * * *") // 매 정시마다 실행
    @Transactional
    public void autoRefundUncompletedPayments() {
        log.info("🔁 [Scheduler] 자동 환불 검증 시작...");

        List<Payment> paidPayments = paymentRepository.findAll().stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PAID && p.getPaidAt() != null)
                .toList();

        int refundCount = 0;

        for (Payment payment : paidPayments) {
            Reservation reservation = reservationRepository.findById(payment.getReservationId()).orElse(null);
            if (reservation == null) continue;

            // 상담이 24시간 이상 미완료 상태면 환불
            boolean expired = Duration.between(payment.getPaidAt(), LocalDateTime.now()).toHours() >= 24;
            boolean notCompleted = reservation.getReservationStatus() != ReservationStatus.COMPLETED;

            if (expired && notCompleted) {
                log.info("[AUTO REFUND] 예약 ID: {} / 결제 ID: {} / 상태변경: PAID -> REFUND" , reservation.getId(), payment.getId());
                payment.setPaymentStatus(PaymentStatus.REFUND);
                reservation.setReservationStatus(ReservationStatus.CANCELLED);

                paymentRepository.save(payment);
                reservationRepository.save(reservation);
                refundCount++;
            }
        }
        log.info("✅ [Scheduler 완료] 자동 환불 처리 건수 = {}", refundCount);
    }
}





