package service.saju_taro_service.dto.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import service.saju_taro_service.dto.reservation.ReservationResponse;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
@Builder
public class CounselorScheduleDashboardResponse {
    private List<ScheduleResponse> todaySchedules; // 오늘스케줄
    private List<ReservationResponse> upcomingReservations; // 다가올 예약
    private List<ReservationResponse> recentCompleted; // 최근 완료된 예약
}
