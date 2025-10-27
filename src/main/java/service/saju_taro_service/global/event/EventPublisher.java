package service.saju_taro_service.global.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventPublisher {
    private final ApplicationEventPublisher publisher;

    public void publishNotification(NotificationEvent event) {
        publisher.publishEvent(event);
    }
}
