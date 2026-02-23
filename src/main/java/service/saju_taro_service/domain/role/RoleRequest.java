package service.saju_taro_service.domain.role;

import jakarta.persistence.*;
import lombok.*;
import service.saju_taro_service.domain.common.BaseTimeEntity;
import service.saju_taro_service.domain.user.User;
import service.saju_taro_service.domain.user.UserRole;

@Entity
@Table(name = "role_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleRequest extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_role", nullable = false, length = 20)
    private UserRole requestedRole;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RequestStatus status = RequestStatus.PENDING;
}
