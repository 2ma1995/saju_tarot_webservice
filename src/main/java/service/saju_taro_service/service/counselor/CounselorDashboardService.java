package service.saju_taro_service.service.counselor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.saju_taro_service.domain.reservation.ReservationStatus;
import service.saju_taro_service.dto.reservation.ReservationResponse;
import service.saju_taro_service.dto.schedule.CounselorScheduleDashboardResponse;
import service.saju_taro_service.dto.schedule.ScheduleResponse;
import service.saju_taro_service.global.util.SecurityUtil;
import service.saju_taro_service.repository.ReservationRepository;
import service.saju_taro_service.repository.ScheduleRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CounselorDashboardService {
    private final ScheduleRepository scheduleRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public CounselorScheduleDashboardResponse getDashboard() {
        Long counselorId = SecurityUtil.currentUserId();

        //오늘 스케줄 조회
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        var todaySchedules = scheduleRepository
                .findByCounselorIdAndStartTimeBetweenOrderByStartTime(
                        counselorId, startOfDay, endOfDay)
                .stream()
                .filter(s -> s.getStartTime().isAfter(LocalDateTime.now()))
                .map(ScheduleResponse::fromEntity)
                .toList();

        // ✅ 다가올 예약 (현재 시점 이후)
        var upcomingReservations = reservationRepository
                .findByCounselorIdAndReservationTimeAfterAndReservationStatus(
                        counselorId, LocalDateTime.now(), ReservationStatus.RESERVED)
                .stream()
                .map(ReservationResponse::fromEntity)
                .toList();

        // ✅ 최근 완료된 예약 5개
        var recentCompleted = reservationRepository
                .findTop5ByCounselorIdAndReservationStatusOrderByReservationTimeDesc(
                        counselorId, ReservationStatus.COMPLETED)
                .stream()
                .map(ReservationResponse::fromEntity)
                .toList();

        return CounselorScheduleDashboardResponse.builder()
                .todaySchedules(todaySchedules)
                .upcomingReservations(upcomingReservations)
                .recentCompleted(recentCompleted)
                .build();
    }

    @Transactional(readOnly = true)
    public CounselorScheduleDashboardResponse getMonthlyDashboard(Integer year, Integer month) {
        Long counselorId = SecurityUtil.currentUserId();

        LocalDate now = LocalDate.now();
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();

        LocalDate startOfMonth = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
        LocalDateTime start = startOfMonth.atStartOfDay();
        LocalDateTime end = endOfMonth.atTime(LocalTime.MAX);

        // 스케줄 + 예약 조회
        var schedules = scheduleRepository
                .findByCounselorIdAndStartTimeBetweenOrderByStartTime(counselorId, start, end)
                .stream()
                .map(ScheduleResponse::fromEntity)
                .toList();

        var reservations = reservationRepository
                .findByCounselorIdOrderByReservationTimeDesc(counselorId)
                .stream()
                .filter(r -> !r.getReservationTime().isBefore(start) && !r.getReservationTime().isAfter(end))
                .map(ReservationResponse::fromEntity)
                .toList();

        return CounselorScheduleDashboardResponse.builder()
                .todaySchedules(schedules)
                .upcomingReservations(reservations)
                .recentCompleted(List.of())
                .build();
    }

}
