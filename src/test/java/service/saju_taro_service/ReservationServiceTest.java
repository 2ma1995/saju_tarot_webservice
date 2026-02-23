package service.saju_taro_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.saju_taro_service.domain.reservation.Reservation;
import service.saju_taro_service.domain.reservation.ReservationStatus;
import service.saju_taro_service.domain.schedule.Schedule;
import service.saju_taro_service.domain.serviceItem.ServiceItem;
import service.saju_taro_service.domain.user.User;
import service.saju_taro_service.dto.reservation.ReservationRequest;
import service.saju_taro_service.global.event.EventPublisher;
import service.saju_taro_service.global.exception.CustomException;
import service.saju_taro_service.repository.ReservationRepository;
import service.saju_taro_service.repository.ScheduleRepository;
import service.saju_taro_service.repository.ServiceItemRepository;
import service.saju_taro_service.repository.UserRepository;
import service.saju_taro_service.service.reservation.ReservationService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    ReservationRepository reservationRepository;
    @Mock
    ScheduleRepository scheduleRepository;
    @Mock
    ServiceItemRepository serviceItemRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    EventPublisher eventPublisher;

    @InjectMocks
    ReservationService reservationService;

    private User user;
    private User counselor;
    private Schedule schedule;
    private ServiceItem serviceItem;
    private Reservation reservation;

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
        schedule.setAvailable(true);

        serviceItem = new ServiceItem();
        serviceItem.setId(5L);

        reservation = Reservation.builder()
                .user(user)
                .counselor(counselor)
                .schedule(schedule)
                .serviceItem(serviceItem)
                .reservationStatus(ReservationStatus.RESERVED)
                .reservationTime(LocalDateTime.now().plusDays(1))
                .isActive(true)
                .build();
    }

    // ===================== createReservation =====================

    @Test
    @DisplayName("✅ 예약 생성 - 정상: 스케줄이 isAvailable=false로 점유되어야 함")
    void createReservation_success_scheduleShouldBeOccupied() {
        ReservationRequest req = new ReservationRequest();
        req.setCounselorId(2L);
        req.setServiceItemId(5L);
        req.setScheduleId(10L);
        req.setReservationTime(LocalDateTime.now().plusDays(1));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(counselor));
        when(serviceItemRepository.findById(5L)).thenReturn(Optional.of(serviceItem));
        when(scheduleRepository.findByIdWithLock(10L)).thenReturn(Optional.of(schedule));
        when(reservationRepository.save(any())).thenReturn(reservation);

        reservationService.createReservation(1L, req);

        // ✅ 스케줄이 점유되었는지 확인
        assertThat(schedule.isAvailable()).isFalse();
        verify(scheduleRepository).save(schedule);
        verify(eventPublisher, times(2)).publishNotification(any()); // 사용자 + 상담사 알림
    }

    @Test
    @DisplayName("❌ 예약 생성 - 이미 예약된 스케줄 → CustomException")
    void createReservation_alreadyBooked() {
        schedule.setAvailable(false); // 이미 점유됨

        ReservationRequest req = new ReservationRequest();
        req.setCounselorId(2L);
        req.setServiceItemId(5L);
        req.setScheduleId(10L);
        req.setReservationTime(LocalDateTime.now().plusDays(1));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(counselor));
        when(serviceItemRepository.findById(5L)).thenReturn(Optional.of(serviceItem));
        when(scheduleRepository.findByIdWithLock(10L)).thenReturn(Optional.of(schedule));

        assertThatThrownBy(() -> reservationService.createReservation(1L, req))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("❌ 예약 생성 - 로그인 안된 사용자 → CustomException")
    void createReservation_nullUserId() {
        assertThatThrownBy(() -> reservationService.createReservation(null, mock(ReservationRequest.class)))
                .isInstanceOf(CustomException.class);
    }

    // ===================== cancelReservation =====================

    @Test
    @DisplayName("✅ 예약 취소 - 스케줄이 isAvailable=true로 복구되어야 함")
    void cancelReservation_restoresSchedule() {
        schedule.setAvailable(false);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        reservationService.cancelReservation(1L);

        assertThat(reservation.getReservationStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(schedule.isAvailable()).isTrue(); // ✅ 스케줄 복구
        verify(scheduleRepository).save(schedule);
    }

    @Test
    @DisplayName("❌ 예약 취소 - 이미 완료된 예약 → CustomException")
    void cancelReservation_alreadyCompleted() {
        reservation.setReservationStatus(ReservationStatus.COMPLETED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancelReservation(1L))
                .isInstanceOf(CustomException.class);
    }

    // ===================== updateStatus =====================

    @Test
    @DisplayName("✅ 상태 변경 - CANCELLED: 스케줄이 isAvailable=true로 복구되어야 함")
    void updateStatus_cancelled_restoresSchedule() {
        schedule.setAvailable(false);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        reservationService.updateStatus(1L, ReservationStatus.CANCELLED);

        assertThat(reservation.getReservationStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(schedule.isAvailable()).isTrue();
        verify(scheduleRepository).save(schedule);
    }

    @Test
    @DisplayName("✅ 상태 변경 - COMPLETED: 사용자 + 상담사에게 알림 2개 발행")
    void updateStatus_completed_sendsNotifications() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        reservationService.updateStatus(1L, ReservationStatus.COMPLETED);

        assertThat(reservation.getReservationStatus()).isEqualTo(ReservationStatus.COMPLETED);
        verify(eventPublisher, times(2)).publishNotification(any()); // 사용자 + 상담사
    }

    @Test
    @DisplayName("❌ 상태 변경 - 이미 종료된 예약 재변경 시도 → CustomException")
    void updateStatus_alreadyEnded() {
        reservation.setReservationStatus(ReservationStatus.COMPLETED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.updateStatus(1L, ReservationStatus.CONFIRMED))
                .isInstanceOf(CustomException.class);
    }
}
