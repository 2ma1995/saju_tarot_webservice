package service.saju_taro_service.dto.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import service.saju_taro_service.dto.reservation.ReservationResponse;
import service.saju_taro_service.dto.schedule.ScheduleResponse;

import java.util.List;
import java.util.Map;

@Getter @Setter
@Builder
public class CounselorMonthlyDashboardResponse {
    private int year;
    private int month;
    private List<ScheduleResponse> schedules;
    private List<ReservationResponse> reservations;
    private Map<String,Long> dailyReservationCount;
    private double totalRevenue;
    private double averageRating;
    private int reviewCount;
}
