package service.saju_taro_service.service.counselor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.saju_taro_service.domain.payment.PaymentStatus;
import service.saju_taro_service.domain.reservation.ReservationStatus;
import service.saju_taro_service.dto.reservation.ReservationResponse;
import service.saju_taro_service.dto.schedule.CounselorScheduleDashboardResponse;
import service.saju_taro_service.dto.schedule.ScheduleResponse;
import service.saju_taro_service.dto.user.CounselorMonthlyDashboardResponse;
import service.saju_taro_service.global.util.SecurityUtil;
import service.saju_taro_service.repository.PaymentRepository;
import service.saju_taro_service.repository.ReservationRepository;
import service.saju_taro_service.repository.ReviewRepository;
import service.saju_taro_service.repository.ScheduleRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CounselorDashboardService {
    private final ScheduleRepository scheduleRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public CounselorScheduleDashboardResponse getDashboard() {
        Long counselorId = SecurityUtil.currentUserId();

        //오늘 날짜 기준
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        // 오늘 스케줄
        var todaySchedules = scheduleRepository
                .findByCounselorIdAndStartTimeBetweenOrderByStartTime(
                        counselorId, startOfDay, endOfDay)
                .stream()
                .filter(s -> s.getStartTime().isAfter(LocalDateTime.now()))
                .map(ScheduleResponse::fromEntity)
                .toList();

        // 다가올 예약
        var upcomingReservations = reservationRepository
                .findByCounselorIdAndReservationTimeAfterAndReservationStatus(
                        counselorId, LocalDateTime.now(), ReservationStatus.RESERVED)
                .stream()
                .map(ReservationResponse::fromEntity)
                .toList();

        // 최근 완료 예약 5건
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

    /** ✅ 월간 통합 통계 (예약 + 매출 + 후기) */
    @Transactional(readOnly = true)
    public CounselorMonthlyDashboardResponse getMonthlyDashboard(Integer year, Integer month) {
        Long counselorId = SecurityUtil.currentUserId();

        LocalDate now = LocalDate.now();
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();

        LocalDate startOfMonth = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
        LocalDateTime start = startOfMonth.atStartOfDay();
        LocalDateTime end = endOfMonth.atTime(LocalTime.MAX);

        // 월간 스케줄
        var schedules = scheduleRepository
                .findByCounselorIdAndStartTimeBetweenOrderByStartTime(counselorId, start, end)
                .stream()
                .map(ScheduleResponse::fromEntity)
                .toList();

        // 월간 예약
        var reservations = reservationRepository
                .findByCounselorIdOrderByReservationTimeDesc(counselorId)
                .stream()
                .filter(r -> !r.getReservationTime().isBefore(start) && !r.getReservationTime().isAfter(end))
                .toList();

        // ✅ 일별 예약 건수
        Map<String, Long> dailyCount = reservations.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getReservationTime().toLocalDate().toString(),
                        Collectors.counting()
                ));

        // ✅ 총 매출 합계 (결제 완료 건만)
        double totalRevenue = paymentRepository.findAll().stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PAID)
                .filter(p -> reservations.stream()
                        .anyMatch(r -> r.getId().equals(p.getReservation().getId())))
                .mapToDouble(p -> p.getAmount())
                .sum();

        // ✅ 후기 통계 (평균 + 개수)
        var reviews = reviewRepository.findByCounselorIdAndIsActiveTrueOrderByCreatedAtDesc(counselorId);
        double avgRating = reviews.stream().mapToInt(r -> r.getRating()).average().orElse(0.0);
        int reviewCount = reviews.size();

        return CounselorMonthlyDashboardResponse.builder()
                .year(targetYear)
                .month(targetMonth)
                .schedules(schedules)
                .reservations(reservations.stream().map(ReservationResponse::fromEntity).toList())
                .dailyReservationCount(dailyCount)
                .totalRevenue(totalRevenue)
                .averageRating(avgRating)
                .reviewCount(reviewCount)
                .build();
    }
}
