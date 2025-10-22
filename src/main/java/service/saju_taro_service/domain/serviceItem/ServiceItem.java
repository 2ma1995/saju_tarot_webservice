package service.saju_taro_service.domain.serviceItem;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import service.saju_taro_service.domain.common.BaseTimeEntity;

@Entity
@Table(name = "service_items")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ServiceItem extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false,length = 20)
    private ServiceType serviceType;

    @Column(nullable = false,length = 255)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private int duration;

    @Column(nullable = false)
    private int price;

    @Column(name = "is_active",nullable = false)
    private Boolean isActive = true;
}
