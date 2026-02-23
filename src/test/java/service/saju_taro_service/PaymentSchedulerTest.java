package service.saju_taro_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.saju_taro_service.domain.payment.Payment;
import service.saju_taro_service.domain.payment.PaymentStatus;
import service.saju_taro_service.domain.reservation.Reservation;
import service.saju_taro_service.domain.reservation.ReservationStatus;
import service.saju_taro_service.domain.schedule.Schedule;
import service.saju_taro_service.domain.user.User;
import service.saju_taro_service.repository.PaymentRepository;
import service.saju_taro_service.repository.ReservationRepository;
import service.saju_taro_service.repository.ScheduleRepository;
import service.saju_taro_service.service.payment.PaymentScheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentSchedulerTest {

    @Mock
    PaymentRepository paymentRepository;
    @Mock
    ReservationRepository reservationRepository;
    @Mock
    ScheduleRepository scheduleRepository;

    @InjectMocks
    PaymentScheduler paymentScheduler;

    private User user;
    private Schedule schedule;
    private Reservation reservation;
    private Payment payment;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        schedule = new Schedule();
        schedule.setId(10L);
        schedule.setAvailable(false); // 현재 점유 상태

        reservation = Reservation.builder()
                .user(user)
                .schedule(schedule)
                .reservationStatus(ReservationStatus.CONFIRMED)
                .reservationTime(LocalDateTime.now().minusDays(2))
                .isActive(true)
                .build();

        payment = new Payment();
        payment.setId(100L);
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now().minusHours(25)); // 24시간 초과
        payment.setAmount(50000);
        payment.setReservation(reservation);
    }

    @Test
    @DisplayName("✅ 자동 환불 - 24시간 초과 미완료 예약: 결제 REFUND + 예약 CANCELLED + 스케줄 복구")
    void autoRefund_expiredAndNotCompleted_shouldCancelAndRestoreSchedule() {
        // findExpiredPaidPayments 가 이미 조건 필터된 결과를 반환
        when(paymentRepository.findExpiredPaidPayments(any())).thenReturn(List.of(payment));
        when(reservationRepository.findById(any())).thenReturn(Optional.of(reservation));
        when(paymentRepository.save(any())).thenReturn(payment);
        when(reservationRepository.save(any())).thenReturn(reservation);
        when(scheduleRepository.save(any())).thenReturn(schedule);

        paymentScheduler.autoRefundUncompletedPayments();

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.REFUND);
        assertThat(reservation.getReservationStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(schedule.isAvailable()).isTrue(); // ✅ 스케줄 복구 확인
        verify(scheduleRepository).save(schedule);
    }

    @Test
    @DisplayName("✅ 자동 환불 제외 - 상담 완료된 예약은 환불하지 않음")
    void autoRefund_completedReservation_shouldSkip() {
        reservation.setReservationStatus(ReservationStatus.COMPLETED);

        // DB 쿼리가 이미 만료된 결제를 반환하지만 예약 완료 상태라 스킵
        when(paymentRepository.findExpiredPaidPayments(any())).thenReturn(List.of(payment));
        when(reservationRepository.findById(any())).thenReturn(Optional.of(reservation));

        paymentScheduler.autoRefundUncompletedPayments();

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(reservation.getReservationStatus()).isEqualTo(ReservationStatus.COMPLETED);
        assertThat(schedule.isAvailable()).isFalse();
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    @DisplayName("✅ 자동 환불 제외 - 만료 결제가 없으면 아무것도 처리하지 않음")
    void autoRefund_noExpiredPayments_shouldDoNothing() {
        // DB 쿼리가 빈 결과 반환 (24시간 미만 or PAID 아님)
        when(paymentRepository.findExpiredPaidPayments(any())).thenReturn(List.of());

        paymentScheduler.autoRefundUncompletedPayments();

        verify(reservationRepository, never()).findById(any());
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    @DisplayName("✅ 자동 환불 제외 - 예약 없는 경우 NPE 없이 스킵")
    void autoRefund_reservationNotFound_shouldSkipGracefully() {
        when(paymentRepository.findExpiredPaidPayments(any())).thenReturn(List.of(payment));
        when(reservationRepository.findById(any())).thenReturn(Optional.empty());

        // 예외 없이 정상 실행되어야 함
        assertThatNoException().isThrownBy(() ->
                paymentScheduler.autoRefundUncompletedPayments());
    }
}
