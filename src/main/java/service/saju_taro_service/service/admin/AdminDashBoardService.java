package service.saju_taro_service.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.saju_taro_service.domain.payment.Payment;
import service.saju_taro_service.domain.payment.PaymentStatus;
import service.saju_taro_service.repository.PaymentRepository;
import service.saju_taro_service.repository.ReservationRepository;
import service.saju_taro_service.repository.ReviewRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashBoardService {
    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final PaymentRepository paymentRepository;

    /** ✅ 일별 예약 건수 / 평균 평점 / 매출 합계 */
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats(LocalDate startDate, LocalDate endDate) {
        var reservations = reservationRepository.findAll();
        var reviews = reviewRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        var payments = paymentRepository.findAll();

        // 일별 예약 건수
        Map<String, Long> dailyReservationCount = reservations.stream()
                .filter(r -> !r.getCreatedAt().toLocalDate().isBefore(startDate)
                        && !r.getCreatedAt().toLocalDate().isAfter(endDate))
                .collect(Collectors.groupingBy(
                        r -> r.getCreatedAt().toLocalDate().toString(),
                        Collectors.counting()
                ));

        double avgRating = reviews.stream()
                .mapToInt(r -> r.getRating())
                .average().orElse(0.0);

        double totalRevenue = payments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PAID)
                .mapToDouble(Payment::getAmount)
                .sum();

        return Map.of(
                "dailyReservationCount", dailyReservationCount,
                "averageRating", avgRating,
                "totalRevenue", totalRevenue
        );
    }


    /** ✅ 월별 매출 통계 */
    @Transactional(readOnly = true)
    public Map<String, Object> getMonthlyRevenueStats(int year) {
        var payments = paymentRepository.findAll();

        // 결제 완료 && 해당 연도 필터
        List<Payment> filtered = payments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PAID)
                .filter(p -> p.getPaidAt() != null && p.getPaidAt().getYear() == year)
                .toList();

        // "yyyy-MM" 기준으로 그룹화
        Map<String, List<Payment>> groupedByMonth = filtered.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getPaidAt().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        TreeMap::new, // 월 순서 정렬
                        Collectors.toList()
                ));

        // 월별 통계 계산
        Map<String, Map<String, Object>> monthlyStats = new LinkedHashMap<>();
        groupedByMonth.forEach((month, monthPayments) -> {

            int count = monthPayments.size();
            double total = monthPayments.stream().mapToDouble(Payment::getAmount).sum();
            double avg = count > 0 ? total / count : 0.0;

            monthlyStats.put(month, Map.of(
                    "count", count,
                    "totalRevenue", total,
                    "averagePerPayment", avg
            ));
        });

        // 총합 계산
        double grandTotal = filtered.stream().mapToDouble(Payment::getAmount).sum();

        return Map.of(
                "year", year,
                "grandTotal", grandTotal,
                "monthlyStats", monthlyStats
        );
    }
}
