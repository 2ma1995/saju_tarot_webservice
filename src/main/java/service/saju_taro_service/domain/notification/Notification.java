package service.saju_taro_service.domain.notification;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import service.saju_taro_service.domain.common.BaseTimeEntity;

@Entity
@Table(name = "notifications")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Notification extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(length = 500, nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private NotificationType type = NotificationType.RESERVATION;
}
