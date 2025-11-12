package service.saju_taro_service.service.reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.saju_taro_service.domain.notification.NotificationType;
import service.saju_taro_service.domain.reservation.Reservation;
import service.saju_taro_service.domain.reservation.ReservationStatus;
import service.saju_taro_service.domain.schedule.Schedule;
import service.saju_taro_service.domain.serviceItem.ServiceItem;
import service.saju_taro_service.domain.user.User;
import service.saju_taro_service.dto.reservation.ReservationRequest;
import service.saju_taro_service.dto.reservation.ReservationResponse;
import service.saju_taro_service.global.event.EventPublisher;
import service.saju_taro_service.global.event.NotificationEvent;
import service.saju_taro_service.global.exception.CustomException;
import service.saju_taro_service.global.exception.ErrorCode;
import service.saju_taro_service.repository.ReservationRepository;
import service.saju_taro_service.repository.ScheduleRepository;
import service.saju_taro_service.repository.ServiceItemRepository;
import service.saju_taro_service.repository.UserRepository;
import service.saju_taro_service.service.notification.NotificationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ScheduleRepository scheduleRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    /**
     * ✅ 예약 생성
     **/
    @Transactional
    public ReservationResponse createReservation(Long userId, ReservationRequest req) {
        if (userId == null) throw new CustomException(ErrorCode.UNAUTHORIZED, "로그인이 필요합니다.");

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        User counselor = userRepository.findById(req.getCounselorId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "상담사를 찾을 수 없습니다."));

        // 서비스 항목 조회
        ServiceItem serviceItem = serviceItemRepository.findById(req.getServiceItemId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "서비스를 찾을 수 없습니다."));

        // 스케줄 존재 여부 확인
        Schedule schedule = scheduleRepository.findById(req.getScheduleId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "스케줄을 찾을 수 없습니다"));

        // 이미 예약된 스케줄인지 확인
        if (!schedule.isAvailable()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "이미 예약된 시간대 입니다");
        }

        // 스케줄 점유 처리
        schedule.setAvailable(false);
        scheduleRepository.save(schedule);

        // 예약 생성
        Reservation reservation = Reservation.builder()
                .user(user)
                .counselor(counselor)
                .serviceItem(serviceItem)
                .schedule(schedule)
                .reservationTime(req.getReservationTime())
                .note(req.getNote())
                .reservationStatus(ReservationStatus.RESERVED)
                .isActive(true)
                .build();

        Reservation saved = reservationRepository.save(reservation);

        // 알림로직 / 비동기 알림 이벤트 발행
        String timeText = req.getReservationTime().toLocalDate() + " " + req.getReservationTime().toLocalTime();

        eventPublisher.publishNotification(new NotificationEvent(
                user.getId(), counselor.getId(), NotificationType.RESERVATION,
                "[예약 완료] " + timeText + " 상담 예약이 접수되었습니다."));

        eventPublisher.publishNotification(new NotificationEvent(
                counselor.getId(), user.getId(), NotificationType.RESERVATION,
                "[신규 예약] " + user.getName() + "님의 " + timeText + " 상담 예약이 들어왔습니다."));

        return ReservationResponse.fromEntity(saved);
    }

    /**
     * 사용자 예약 목록 조회
     **/
    @Transactional(readOnly = true)
    public List<ReservationResponse> getUserReservations(Long userId, String status) {
        List<Reservation> list = reservationRepository.findByUserIdOrderByReservationTimeDesc(userId);

        if (status == null || status.isBlank()) {
            return list.stream()
                    .map(ReservationResponse::fromEntity)
                    .toList();
        }

        ReservationStatus parsedStatus;
        try {
            parsedStatus = ReservationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "잘못된 예약 상태 값입니다.");
        }

        return list.stream()
                .filter(r -> r.getReservationStatus() == parsedStatus)
                .map(ReservationResponse::fromEntity)
                .toList();
    }

    /**
     * 상담사 전체 예약 목록 조회
     **/
    @Transactional(readOnly = true)
    public List<ReservationResponse> getCounselorReservations(Long counselorId) {
        return reservationRepository.findByCounselorIdOrderByReservationTimeDesc(counselorId)
                .stream()
                .map(ReservationResponse::fromEntity)
                .toList();
    }

    /**
     * ✅ 상담사 - 특정 날짜 예약 목록 조회 (달력 클릭 시, 이름/전화 표시)
     */
    @Transactional(readOnly = true)
    public List<ReservationResponse> getCounselorReservationsByDay(Long counselorId, String dateStr) {
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "날짜 형식이 올바르지 않습니다. (예: 2025-10-25)");
        }

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        // 상담사 해당 날짜 예약 조회
        List<Reservation> reservations = reservationRepository.findByCounselorIdOrderByReservationTimeDesc(counselorId)
                .stream()
                .filter(r -> r.getReservationTime() != null &&
                        !r.getReservationTime().isBefore(start) &&
                        !r.getReservationTime().isAfter(end))
                .toList();

        // 사용자 이름/전화번호 포함하여 반환
        return reservations.stream()
                .map(r -> ReservationResponse.fromEntityWithUser(r,
                        r.getUser() != null ? r.getUser().getName() : "알수 없음",
                        r.getUser() != null ? r.getUser().getPhone() : "-"))
                .toList();
    }

    /**
     * 예약 취소
     */
    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND, "예약을 찾을 수 없습니다."));

        if (r.getReservationStatus() == ReservationStatus.COMPLETED)
            throw new CustomException(ErrorCode.RESERVATION_ALREADY_COMPLETED, "이미 완료된 예약은 취소할 수 없습니다.");

        //  스케줄 복원
        if (r.getSchedule() != null) {
            Schedule schedule = r.getSchedule();
            schedule.setAvailable(true);
            scheduleRepository.save(schedule);
        }

        // 예약 상태 변경₩
        r.setReservationStatus(ReservationStatus.CANCELLED);

        // 알림 이벤트 발행
        String timeText = r.getReservationTime().toLocalDate() + " " + r.getReservationTime().toLocalTime();
        eventPublisher.publishNotification(new NotificationEvent(
                r.getUser().getId(),
                r.getCounselor().getId(),
                NotificationType.CANCEL,
                "[예약 취소] " + timeText + " 상담 예약이 취소되었습니다."));
        eventPublisher.publishNotification(new NotificationEvent(
                r.getCounselor().getId(),
                r.getUser().getId(),
                NotificationType.CANCEL,
                "[예약 취소] " + r.getUser().getName() + "님의 " + timeText + " 상담 예약이 취소되었습니다.")
        );
    }

    /**
     * ✅ 상태 업데이트 (상담사용)
     */
    @Transactional
    public void updateStatus(Long reservationId, ReservationStatus newStatus) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND, "예약을 찾을 수 없습니다."));

        // 이미 취소나 완료된 예약은 재변경 불가
        if (r.getReservationStatus() == ReservationStatus.COMPLETED ||
                r.getReservationStatus() == ReservationStatus.CANCELLED) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "이미 종료된 예약은 변경할 수 없습니다.");
        }

        // 상태 업데이트
        r.setReservationStatus(newStatus);

        // 예약 취소 시 스케줄 다시 열기
        if (newStatus == ReservationStatus.CANCELLED && r.getSchedule() != null) {
            Schedule schedule = r.getSchedule();
            schedule.setAvailable(true);
            scheduleRepository.save(schedule);
        }

        // 완료 시 알림
        if (newStatus == ReservationStatus.COMPLETED) {
            String timeText = r.getReservationTime().toLocalDate() + " " + r.getReservationTime().toLocalTime();
            eventPublisher.publishNotification(new NotificationEvent(
                    r.getUser().getId(), r.getCounselor().getId(), NotificationType.COMPLETE,
                    "[상담 완료] " + timeText + " 상담이 완료되었습니다. 리뷰를 남겨주세요."));
            eventPublisher.publishNotification(new NotificationEvent(
                    r.getCounselor().getId(), r.getCounselor().getId(), NotificationType.COMPLETE,
                    "[상담 완료] " + timeText + " 상담이 완료되었습니다."));
        }
    }
}