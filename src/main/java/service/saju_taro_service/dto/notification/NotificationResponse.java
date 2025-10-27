package service.saju_taro_service.dto.notification;

import lombok.*;
import service.saju_taro_service.domain.notification.Notification;
import service.saju_taro_service.domain.notification.NotificationType;

import java.time.LocalDateTime;

@Getter @Builder
@AllArgsConstructor @NoArgsConstructor
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;

    public static NotificationResponse fromEntity(Notification notification){
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
