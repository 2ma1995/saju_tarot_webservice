package service.saju_taro_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import service.saju_taro_service.domain.serviceItem.ServiceItem;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {
}
