package service.saju_taro_service.dto.payment;

import lombok.Getter;
import lombok.Setter;
import service.saju_taro_service.domain.payment.Payment;

import java.time.LocalDateTime;

@Getter @Setter
public class PaymentResponse {
    private Long id;
    private Long reservationId;
    private int amount;
    private String method;
    private String status;
    private String transactionId;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;

    public static PaymentResponse fromEntity(Payment payment) {
        PaymentResponse dto = new PaymentResponse();
        dto.setId(payment.getId());
        dto.setReservationId(payment.getReservation().getId());
        dto.setAmount(payment.getAmount());
        dto.setMethod(payment.getMethod() != null ? payment.getMethod().name() : null);
        dto.setStatus(payment.getPaymentStatus().name());
        dto.setTransactionId(payment.getTransactionId());
        dto.setPaidAt(payment.getPaidAt());
        dto.setCreatedAt(payment.getCreatedAt());
        return dto;
    }

}
