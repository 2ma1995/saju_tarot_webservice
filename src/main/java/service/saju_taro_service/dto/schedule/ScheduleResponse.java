package service.saju_taro_service.dto.schedule;

import lombok.Getter;
import lombok.Setter;
import service.saju_taro_service.domain.schedule.Schedule;

import java.time.LocalDateTime;

@Getter @Setter
public class ScheduleResponse {
    private Long id;
    private Long counselorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean isAvailable;

    public static ScheduleResponse fromEntity(Schedule schedule){
        ScheduleResponse res = new ScheduleResponse();
        res.setId(schedule.getId());
        res.setCounselorId(schedule.getCounselorId());
        res.setStartTime(schedule.getStartTime());
        res.setEndTime(schedule.getEndTime());
        res.setAvailable(schedule.isAvailable());
        return res;
    }

}
