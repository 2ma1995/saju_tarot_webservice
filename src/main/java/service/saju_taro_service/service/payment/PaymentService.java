package service.saju_taro_service.service.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.saju_taro_service.domain.notification.NotificationType;
import service.saju_taro_service.domain.payment.Payment;
import service.saju_taro_service.domain.payment.PaymentMethod;
import service.saju_taro_service.domain.payment.PaymentStatus;
import service.saju_taro_service.domain.reservation.Reservation;
import service.saju_taro_service.domain.reservation.ReservationStatus;
import service.saju_taro_service.domain.user.User;
import service.saju_taro_service.dto.payment.PaymentResponse;
import service.saju_taro_service.global.event.EventPublisher;
import service.saju_taro_service.global.event.NotificationEvent;
import service.saju_taro_service.global.toss.TossPaymentsClient;
import service.saju_taro_service.repository.PaymentRepository;
import service.saju_taro_service.repository.ReservationRepository;
import service.saju_taro_service.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;
    private final TossPaymentsClient tossPaymentsClient;

    /** ✅ 결제 요청 생성 (예약 직후) */
    @Transactional
    public Payment createPayment(Long reservationId, int amount) {
        Payment payment = new Payment();
        payment.setReservationId(reservationId);
        payment.setAmount(amount);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setMethod(PaymentMethod.CARD);
        return paymentRepository.save(payment);
    }

    // ==================== 기존 메서드 (유지) ====================

    /** ✅ 결제 완료 처리 (기존 방식 - PG Webhook or 클라이언트 콜백) */
    @Transactional
    public void completePayment(String txId) {
        Payment payment = paymentRepository.findByTransactionId(txId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // 🔹 Reservation 상태 변경
        Reservation reservation = reservationRepository.findById(payment.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("예약 정보를 찾을 수 없습니다."));
        reservation.setReservationStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        // 🔔 알림 발행 (결제 완료)
        User user = userRepository.findById(reservation.getUserId()).orElse(null);
        if (user != null) {
            eventPublisher.publishNotification(new NotificationEvent(
                    user.getId(),
                    reservation.getCounselorId(),
                    NotificationType.PAYMENT,
                    "[결제 완료] 예약 #" + reservation.getId() + "의 결제가 완료되었습니다. 금액: " + payment.getAmount() + "원"
            ));
        }

        // 🔹 알림 발행 (사용자 + 상담사)
        triggerPaymentNotification(reservation, payment);
    }

    /** ✅ 환불 처리 (기존 방식) */
    @Transactional
    public void refundPayment(String txId) {
        Payment payment = paymentRepository.findByTransactionId(txId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));
        payment.setPaymentStatus(PaymentStatus.REFUND);
        paymentRepository.save(payment);

        // 🔹 환불 시 예약 상태 취소로 변경
        Reservation reservation = reservationRepository.findById(payment.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("예약 정보를 찾을 수 없습니다."));
        reservation.setReservationStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        // 🔔 알림 발행 (환불 완료)
        User user = userRepository.findById(reservation.getUserId()).orElse(null);
        if (user != null) {
            eventPublisher.publishNotification(new NotificationEvent(
                    user.getId(),
                    reservation.getCounselorId(),
                    NotificationType.REFUND,
                    "[환불 완료] 예약 #" + reservation.getId() + "의 결제가 취소되고 환불되었습니다. 금액: " + payment.getAmount() + "원"
            ));
        }
    }

    // ==================== Toss Payments 전용 메서드 (신규) ====================

    /**
     * ✅ Toss Payments 결제 승인 처리
     * @param paymentKey Toss에서 발급한 결제 키
     * @param orderId 주문 ID (transactionId)
     * @param amount 결제 금액
     */
    @Transactional
    public void confirmTossPayment(String paymentKey, String orderId, int amount) {
        // 1. Toss Payments API 호출하여 결제 승인
        Map<String, Object> result = tossPaymentsClient.confirmPayment(paymentKey, orderId, amount);

        // 2. DB에서 결제 정보 조회
        Payment payment = paymentRepository.findByTransactionId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        // 3. 결제 상태 업데이트
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        payment.setPaymentKey(paymentKey); // ✅ paymentKey 저장 (환불 시 필요)

        // Toss에서 받은 결제 방법 저장
        String method = (String) result.get("method");
        if (method != null && method.equalsIgnoreCase("카드")) {
            payment.setMethod(PaymentMethod.CARD);
        }

        paymentRepository.save(payment);

        // 4. 예약 상태 변경
        Reservation reservation = reservationRepository.findById(payment.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("예약 정보를 찾을 수 없습니다."));
        reservation.setReservationStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        // 5. 알림 발행
        triggerPaymentNotification(reservation, payment);

        log.info("✅ Toss 결제 승인 완료 - orderId: {}, paymentKey: {}", orderId, paymentKey);
    }

    /**
     * ✅ Toss Payments 환불 처리
     * @param txId 거래 ID
     * @param reason 환불 사유
     */
    @Transactional
    public void refundTossPayment(String txId, String reason) {
        Payment payment = paymentRepository.findByTransactionId(txId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        if (payment.getPaymentStatus() != PaymentStatus.PAID) {
            throw new IllegalStateException("결제 완료 상태에서만 환불이 가능합니다.");
        }

        if (payment.getPaymentKey() == null) {
            throw new IllegalStateException("결제 키가 없어 환불할 수 없습니다.");
        }

        // Toss Payments API를 통한 실제 환불 처리
        try {
            tossPaymentsClient.cancelPayment(payment.getPaymentKey(), reason);
        } catch (Exception e) {
            log.error("❌ Toss Payments 환불 실패: {}", e.getMessage());
            throw new RuntimeException("환불 처리 중 오류가 발생했습니다.");
        }

        // DB 상태 업데이트
        payment.setPaymentStatus(PaymentStatus.REFUND);
        paymentRepository.save(payment);

        // 예약 상태 취소로 변경
        Reservation reservation = reservationRepository.findById(payment.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("예약 정보를 찾을 수 없습니다."));
        reservation.setReservationStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        // 환불 알림 발행
        User user = userRepository.findById(reservation.getUserId()).orElse(null);
        if (user != null) {
            eventPublisher.publishNotification(new NotificationEvent(
                    user.getId(),
                    reservation.getCounselorId(),
                    NotificationType.REFUND,
                    "[환불 완료] 예약 #" + reservation.getId() + "의 결제가 취소되고 환불되었습니다. 금액: " + payment.getAmount() + "원"
            ));
        }

        log.info("✅ Toss 환불 완료 - txId: {}, reason: {}", txId, reason);
    }

    // ==================== 공통 메서드 ====================

    /** ✅ 사용자: 내 결제내역 조회 */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getMyPayments(Long userId) {
        return paymentRepository.findAll().stream()
                .filter(p -> {
                    var reservation = reservationRepository.findById(p.getReservationId()).orElse(null);
                    return reservation != null && reservation.getUserId().equals(userId);
                })
                .map(PaymentResponse::fromEntity)
                .toList();
    }

    /** ✅ 관리자: 전체 결제내역 조회 (필터 가능) */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments(String status) {
        return paymentRepository.findAll().stream()
                .filter(p -> status == null || p.getPaymentStatus().name().equalsIgnoreCase(status))
                .map(PaymentResponse::fromEntity)
                .toList();
    }

    /** ✅ 결제 완료 시 알림 이벤트 발행 */
    private void triggerPaymentNotification(Reservation reservation, Payment payment) {
        User user = userRepository.findById(reservation.getUserId()).orElse(null);
        User counselor = userRepository.findById(reservation.getCounselorId()).orElse(null);

        if (user != null) {
            eventPublisher.publishNotification(new NotificationEvent(
                    user.getId(),
                    reservation.getCounselorId(),
                    NotificationType.PAYMENT,
                    "[결제 완료] 예약 #" + reservation.getId() + "의 결제가 완료되었습니다. (" + payment.getAmount() + "원)"
            ));
        }

        if (counselor != null) {
            eventPublisher.publishNotification(new NotificationEvent(
                    counselor.getId(),
                    reservation.getCounselorId(),
                    NotificationType.PAYMENT,
                    "[신규 결제] " + (user != null ? user.getName() : "사용자") + "님의 상담 결제가 완료되었습니다."
            ));
        }
    }
}
