package service.saju_taro_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import service.saju_taro_service.domain.notification.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
