package service.saju_taro_service.domain.reservation;

import jakarta.persistence.*;
import lombok.*;
import service.saju_taro_service.domain.common.BaseTimeEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Reservation extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id",nullable = false)
    private Long userId; // 예약자

    @Column(nullable = false)
    private Long counselorId; // 상담사

    @Column(name = "service_item_id",nullable = false)
    private Long serviceItemId; // 상담 서비스 ID

    @Column(name = "reservation_time",nullable = false)
    private LocalDateTime reservationTime; // 예약 일시

    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status", nullable = false, length = 20)
    private ReservationStatus reservationStatus = ReservationStatus.RESERVED;

    private String note;

    @Column(name = "is_active",nullable = false)
    private Boolean isActive = true;
}
