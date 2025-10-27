package service.saju_taro_service.domain.reservation;

import jakarta.persistence.*;
import lombok.*;
import service.saju_taro_service.domain.common.BaseTimeEntity;
import service.saju_taro_service.domain.schedule.Schedule;
import service.saju_taro_service.domain.serviceItem.ServiceItem;
import service.saju_taro_service.domain.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Reservation extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 예약자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id", nullable = false)
    private User counselor; // 상담사

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_item_id", nullable = false)
    private ServiceItem serviceItem; // 상담 서비스 ID

    @Column(name = "reservation_time",nullable = false)
    private LocalDateTime reservationTime; // 예약 일시

    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status", nullable = false, length = 20)
    private ReservationStatus reservationStatus = ReservationStatus.RESERVED;

    private String note;

    @Column(name = "is_active",nullable = false)
    private Boolean isActive = true;
}
