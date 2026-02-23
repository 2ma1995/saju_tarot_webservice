package service.saju_taro_service.domain.review;

import jakarta.persistence.*;
import lombok.*;
import service.saju_taro_service.domain.common.BaseTimeEntity;
import service.saju_taro_service.domain.reservation.Reservation;
import service.saju_taro_service.domain.user.User;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id", nullable = false)
    private User counselor;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false)
    private int rating;

    @Column(length = 1000)
    private String comment;

    @Builder.Default
    private boolean isActive = true;
}
