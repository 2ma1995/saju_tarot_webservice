package service.saju_taro_service.domain.payment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import service.saju_taro_service.domain.common.BaseTimeEntity;
import service.saju_taro_service.domain.reservation.Reservation;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

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

    // ✅ Toss Payments에서 발급하는 고유 결제 키 (환불 시 필요)
    @Column(name = "payment_key", length = 255)
    private String paymentKey;

    private LocalDateTime paidAt;

}
