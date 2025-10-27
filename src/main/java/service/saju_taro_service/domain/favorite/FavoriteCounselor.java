package service.saju_taro_service.domain.favorite;

import jakarta.persistence.*;
import lombok.*;
import service.saju_taro_service.domain.common.BaseTimeEntity;
import service.saju_taro_service.domain.user.User;

@Entity
@Table(name = "favorite_counselors",uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "counselor_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteCounselor extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id", nullable = false)
    private User counselor;
}
