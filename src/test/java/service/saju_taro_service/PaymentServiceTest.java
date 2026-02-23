package service.saju_taro_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.saju_taro_service.domain.payment.Payment;
import service.saju_taro_service.domain.payment.PaymentMethod;
import service.saju_taro_service.domain.payment.PaymentStatus;
import service.saju_taro_service.domain.reservation.Reservation;
import service.saju_taro_service.domain.reservation.ReservationStatus;
import service.saju_taro_service.domain.schedule.Schedule;
import service.saju_taro_service.domain.user.User;
import service.saju_taro_service.global.event.EventPublisher;
import service.saju_taro_service.global.event.NotificationEvent;
import service.saju_taro_service.global.exception.CustomException;
import service.saju_taro_service.global.toss.TossPaymentsClient;
import service.saju_taro_service.repository.PaymentRepository;
import service.saju_taro_service.repository.ReservationRepository;
import service.saju_taro_service.repository.ScheduleRepository;
import service.saju_taro_service.service.payment.PaymentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    PaymentRepository paymentRepository;
    @Mock
    ReservationRepository reservationRepository;
    @Mock
    ScheduleRepository scheduleRepository;
    @Mock
    EventPublisher eventPublisher;
    @Mock
    TossPaymentsClient tossPaymentsClient;

    @InjectMocks
    PaymentService paymentService;

    private User user;
    private User counselor;
    private Schedule schedule;
    private Reservation reservation;
    private Payment payment;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("테스트유저");

        counselor = new User();
        counselor.setId(2L);
        counselor.setName("테스트상담사");

        schedule = new Schedule();
        schedule.setId(10L);
        schedule.setAvailable(false); // 예약 중 상태

        reservation = Reservation.builder()
                .user(user)
                .counselor(counselor)
                .schedule(schedule)
                .reservationStatus(ReservationStatus.CONFIRMED)
                .reservationTime(LocalDateTime.now().plusDays(1))
                .build();

        payment = new Payment();
        payment.setId(100L);
        payment.setTransactionId("tx-001");
        payment.setPaymentKey("pk-toss-001");
        payment.setPaymentStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        payment.setAmount(50000);
        payment.setReservation(reservation);
        payment.setMethod(PaymentMethod.CARD);
    }

    // ===================== createPayment =====================

    @Test
    @DisplayName("✅ 결제 생성 - 정상 케이스")
    void createPayment_success() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment result = paymentService.createPayment(1L, 50000);

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.getAmount()).isEqualTo(50000);
        assertThat(result.getMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(result.getTransactionId()).isNotNull();
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("❌ 결제 생성 - 존재하지 않는 예약 → CustomException")
    void createPayment_reservationNotFound() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.createPayment(999L, 50000))
                .isInstanceOf(CustomException.class);
    }

    // ===================== refundTossPayment =====================

    @Test
    @DisplayName("✅ Toss 환불 - 정상 케이스: 스케줄이 isAvailable=true로 복구되어야 함")
    void refundTossPayment_restoresSchedule() {
        when(paymentRepository.findByTransactionId("tx-001")).thenReturn(Optional.of(payment));
        doNothing().when(tossPaymentsClient).cancelPayment(anyString(), anyString());
        when(paymentRepository.save(any())).thenReturn(payment);
        when(reservationRepository.save(any())).thenReturn(reservation);
        when(scheduleRepository.save(any())).thenReturn(schedule);

        paymentService.refundTossPayment("tx-001", "고객 요청");

        // 결제 상태 REFUND 확인
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.REFUND);
        // 예약 상태 CANCELLED 확인
        assertThat(reservation.getReservationStatus()).isEqualTo(ReservationStatus.CANCELLED);
        // 스케줄 복구 확인 ✅
        assertThat(schedule.isAvailable()).isTrue();
        verify(scheduleRepository).save(schedule);
    }

    @Test
    @DisplayName("❌ Toss 환불 - PAID 아닌 결제 환불 시도 → CustomException")
    void refundTossPayment_notPaidStatus() {
        payment.setPaymentStatus(PaymentStatus.PENDING);
        when(paymentRepository.findByTransactionId("tx-001")).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.refundTossPayment("tx-001", "테스트"))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("❌ Toss 환불 - paymentKey 없는 경우 → CustomException")
    void refundTossPayment_noPaymentKey() {
        payment.setPaymentKey(null);
        when(paymentRepository.findByTransactionId("tx-001")).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.refundTossPayment("tx-001", "테스트"))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("❌ Toss 환불 - Toss API 호출 실패 → CustomException")
    void refundTossPayment_tossApiFails() {
        when(paymentRepository.findByTransactionId("tx-001")).thenReturn(Optional.of(payment));
        doThrow(new RuntimeException("Toss API 오류")).when(tossPaymentsClient).cancelPayment(anyString(), anyString());

        assertThatThrownBy(() -> paymentService.refundTossPayment("tx-001", "테스트"))
                .isInstanceOf(CustomException.class);
    }

    // ===================== refundPayment (기존 방식) =====================

    @Test
    @DisplayName("✅ 기존 환불 - 스케줄이 isAvailable=true로 복구되어야 함")
    void refundPayment_restoresSchedule() {
        payment.setPaymentStatus(PaymentStatus.PENDING);
        when(paymentRepository.findByTransactionId("tx-001")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenReturn(payment);
        when(reservationRepository.save(any())).thenReturn(reservation);
        when(scheduleRepository.save(any())).thenReturn(schedule);

        paymentService.refundPayment("tx-001");

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.REFUND);
        assertThat(reservation.getReservationStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(schedule.isAvailable()).isTrue(); // ✅ 복구 확인
        verify(scheduleRepository).save(schedule);
    }

    // ===================== 알림 발신자 ID 버그 수정 검증 =====================

    @Test
    @DisplayName("✅ 결제 완료 알림 - 상담사 알림의 발신자가 counselor가 아니라 user여야 함")
    void confirmTossPayment_notificationSenderMustBeUser() {
        when(paymentRepository.findByTransactionId("ord-001")).thenReturn(Optional.of(payment));
        when(tossPaymentsClient.confirmPayment(anyString(), anyString(), anyInt()))
                .thenReturn(Map.of("method", "카드"));
        when(paymentRepository.save(any())).thenReturn(payment);
        when(reservationRepository.save(any())).thenReturn(reservation);

        // paymentKey 없는 payment에 설정
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentKey(null);

        // 새 payment로 재설정
        Payment freshPayment = new Payment();
        freshPayment.setId(200L);
        freshPayment.setTransactionId("ord-001");
        freshPayment.setPaymentStatus(PaymentStatus.PENDING);
        freshPayment.setAmount(50000);
        freshPayment.setReservation(reservation);
        freshPayment.setMethod(PaymentMethod.CARD);
        when(paymentRepository.findByTransactionId("ord-001")).thenReturn(Optional.of(freshPayment));

        paymentService.confirmTossPayment("pk-001", "ord-001", 50000);

        // 발행된 이벤트 캡처
        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(eventPublisher, atLeast(1)).publishNotification(captor.capture());

        List<NotificationEvent> events = captor.getAllValues();

        // 상담사(userId=2L)가 수신자인 이벤트 찾기
        NotificationEvent counselorEvent = events.stream()
                .filter(e -> Long.valueOf(2L).equals(e.getUserId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("상담사 수신 알림 없음"));

        // ✅ 발신자(counselorId 필드)는 user(1L)여야 함, counselor(2L)이면 버그
        assertThat(counselorEvent.getCounselorId())
                .as("상담사 알림의 발신자는 사용자(1L)여야 합니다.")
                .isEqualTo(1L);
    }
}
