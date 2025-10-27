package service.saju_taro_service.domain.profile;

import jakarta.persistence.*;
import lombok.*;
import service.saju_taro_service.domain.common.BaseTimeEntity;
import service.saju_taro_service.domain.user.User;

@Entity
@Table(name = "profiles")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Profile extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="counselor_id", nullable = false)
    private User counselor;

    @Column(length = 1000)
    private String bio; //자기소개

    @Column(length = 500)
    private String experience; // 경력

    @Column(length = 255)
    private String tags; //주요분야(예: 연애, 진로, 사업 등)

    private String imageUrl;// 프로필 이미지 경로

}
