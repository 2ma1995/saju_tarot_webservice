package service.saju_taro_service.domain.consultation;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import service.saju_taro_service.domain.common.BaseTimeEntity;

@Entity
@Table(name = "consultations")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Consultation extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "counselor_name", length = 100)
    private String counselorName;

    @Column(length = 2000)
    private String summary;
}
