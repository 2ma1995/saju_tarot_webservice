package service.saju_taro_service.domain.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import service.saju_taro_service.domain.common.BaseTimeEntity;
import service.saju_taro_service.domain.profile.Profile;
import service.saju_taro_service.dto.user.UserSignupRequest;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "counselor", fetch = FetchType.LAZY)
    private Profile profile;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 50, unique = true)
    private String nickname;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 15)
    private String phone;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, length = 15)
    private UserRole userRole = UserRole.USER;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Builder.Default
    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Builder.Default
    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "fcm_token")
    private String fcmToken;

    // 비즈니스 로직 메서드로 변경
    public void updateInfo(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void updateRole(UserRole role) {
        this.userRole = role;
    }

    public void updateRating(Double averageRating, Integer reviewCount) {
        this.averageRating = averageRating;
        this.reviewCount = reviewCount;
    }

    public static User create(UserSignupRequest req, PasswordEncoder encoder) {
        return User.builder()
                .name(req.getName())
                .nickname(req.getNickname())
                .email(req.getEmail())
                .password(encoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .userRole(UserRole.USER)
                .isActive(true) // ✅ Builder로 true를 강제 지정
                .build();
    }
}
