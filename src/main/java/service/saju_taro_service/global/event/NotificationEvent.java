package service.saju_taro_service.global.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import service.saju_taro_service.domain.notification.NotificationType;

@Getter
@AllArgsConstructor
public class NotificationEvent {
    private final Long userId;
    private final Long counselorId;
    private final NotificationType type;
    private final String message;
}
