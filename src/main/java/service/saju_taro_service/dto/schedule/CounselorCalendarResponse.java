package service.saju_taro_service.dto.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import service.saju_taro_service.domain.reservation.Reservation;
import service.saju_taro_service.domain.schedule.Schedule;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@Builder
public class CounselorCalendarResponse {
    private Long counselorId;
    private String month;
    private List<DaySchedule> days;

    @Data
    @AllArgsConstructor
    @Builder
    public static class DaySchedule {
        private LocalDate date;
        private boolean hasReservation; // 예약존재 여부 표시
        private List<TimeSlot> schedules;
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class TimeSlot {
        private String startTime;
        private String endTime;
        private boolean available;
        private String status; // 예약 상태 표시
    }

    public static CounselorCalendarResponse from(
            List<Schedule> schedules,
            List<Reservation> reservations,
            LocalDate start, LocalDate end,
            String role
            ) {

        // 날짜별 스케줄 그룹핑
        Map<LocalDate, List<Schedule>> schedulesByDate = schedules.stream()
                .collect(Collectors.groupingBy(s -> s.getStartTime().toLocalDate()));

        // 예약이 존재하는 날짜 Set (NPE 방지)
        Set<LocalDate> reservedDates = reservations.stream()
                .filter(r -> r.getReservationTime() != null)
                .map(r -> r.getReservationTime().toLocalDate())
                .collect(Collectors.toSet());

        // scheduleId -> reservation 매핑 (빠른 조회용, NPE 방지)
        Map<Long, Reservation> reservationByScheduleId = reservations.stream()
                .filter(r -> r.getScheduleId() != null)
                .collect(Collectors.toMap(Reservation::getScheduleId, r -> r, (a, b) -> a)); // 충돌시 첫 값

        List<DaySchedule> days = new ArrayList<>();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            boolean hasRes = reservedDates.contains(date);

            List<TimeSlot> slots = schedulesByDate.getOrDefault(date, Collections.emptyList())
                    .stream()
                    .map(s -> {
                        Reservation res = reservationByScheduleId.get(s.getId());
                        boolean hasReservationForSlot = (res != null);
                        return TimeSlot.builder()
                                .startTime(s.getStartTime().toLocalTime().toString())
                                .endTime(s.getEndTime().toLocalTime().toString())
                                .available(s.isAvailable())
                                .status(role.equals("USER")
                                        ? (hasReservationForSlot ? "회색표시" : null)
                                        : (hasReservationForSlot ? "예약됨" : "가능"))
                                .build();
                    })
                    .filter(ts -> ts.getStatus() != null) // USER일 때는 예약된 슬롯만
                    .sorted(Comparator.comparing(TimeSlot::getStartTime))
                    .toList();

            days.add(DaySchedule.builder()
                    .date(date)
                    .hasReservation(hasRes)
                    .schedules(slots)
                    .build());
        }

        return CounselorCalendarResponse.builder()
                .counselorId(schedules.isEmpty() ? null : schedules.get(0).getCounselorId())
                .month(start.getYear() + "-" + String.format("%02d", start.getMonthValue()))
                .days(days)
                .build();
    }
}

