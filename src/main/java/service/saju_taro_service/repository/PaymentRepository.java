package service.saju_taro_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import service.saju_taro_service.domain.payment.Payment;
import service.saju_taro_service.domain.payment.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionId(String txId);

    // ✅ 사용자 ID로 결제 내역 조회 (성능 최적화: findAll 대체)
    List<Payment> findByReservation_User_Id(Long userId);

    // ✅ 결제 상태로 조회 (성능 최적화: findAll 대체)
    List<Payment> findByPaymentStatus(PaymentStatus status);

    // ✅ PaymentScheduler용: PAID 상태이며 paidAt이 특정 시간 이전인 결제 조회
    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'PAID' AND p.paidAt <= :cutoffTime")
    List<Payment> findExpiredPaidPayments(@Param("cutoffTime") LocalDateTime cutoffTime);
}
