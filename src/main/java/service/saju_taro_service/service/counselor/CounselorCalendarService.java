package service.saju_taro_service.service.counselor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.saju_taro_service.domain.reservation.Reservation;
import service.saju_taro_service.domain.schedule.Schedule;
import service.saju_taro_service.dto.schedule.CounselorCalendarResponse;
import service.saju_taro_service.repository.ReservationRepository;
import service.saju_taro_service.repository.ScheduleRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CounselorCalendarService {

    private final ScheduleRepository scheduleRepository;
    private final ReservationRepository reservationRepository;

    // 상담사 월간 달력 조회 (예약여부포함)
    @Transactional(readOnly = true)
    public CounselorCalendarResponse getCalendar(Long counselorId, Integer year, Integer month, String role) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        // 스케줄 조회
        List<Schedule> schedules = scheduleRepository
                .findByCounselorIdAndStartTimeBetweenOrderByStartTime(
                        counselorId, start.atStartOfDay(), end.atTime(23, 59));
        // 예약 조회
        List<Reservation> reservations = reservationRepository
                .findByCounselorIdOrderByReservationTimeDesc(counselorId)
                .stream()
                .filter(r -> r.getReservationTime() != null &&
                        !r.getReservationTime().isBefore(start.atStartOfDay()) &&
                        !r.getReservationTime().isAfter(end.atTime(23, 59)))
                .toList();

        // ✅ 사용자 / 상담사 역할별 표시 로직 분기
        if ("USER".equals(role)) {
            // 사용자는 예약된 것만 보이게 (회색)
            schedules = schedules.stream()
                    .filter(s -> reservations.stream()
                            .anyMatch(r -> r.getScheduleId() != null && r.getScheduleId().equals(s.getId())))
                    .sorted(Comparator.comparing(Schedule::getStartTime))
                    .toList();
        } else if ("COUNSELOR".equals(role)) {
            // 상담사는 전체 스케줄, 예약여부 포함 표시
            schedules = schedules.stream()
                    .sorted(Comparator.comparing(Schedule::getStartTime))
                    .toList();
        }

        return CounselorCalendarResponse.from(schedules, reservations, start, end, role);
    }
}

