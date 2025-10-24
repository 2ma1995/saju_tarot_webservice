package service.saju_taro_service.dto.schedule;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class ScheduleRequest {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
