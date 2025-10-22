package service.saju_taro_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import service.saju_taro_service.domain.payment.Payment;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionId(String txId);
}
