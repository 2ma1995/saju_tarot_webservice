package service.saju_taro_service.domain.payment;

import jakarta.persistence.*;
import service.saju_taro_service.domain.common.BaseTimeEntity;

import java.time.LocalDateTime;

@Entity
public class Payment extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "erservation_id", nullable = false)
    private Long reservationId;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 10)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "transaction_id",length = 255)
    private String transactionId;

    private LocalDateTime paidAt;

}
