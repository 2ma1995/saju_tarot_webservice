package service.saju_taro_service.service.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.saju_taro_service.domain.notification.Notification;
import service.saju_taro_service.domain.notification.NotificationType;
import service.saju_taro_service.dto.notification.NotificationResponse;
import service.saju_taro_service.global.exception.CustomException;
import service.saju_taro_service.global.exception.ErrorCode;
import service.saju_taro_service.repository.NotificationRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    // 공통 생성기
    @Transactional
    public void create(Long userId, Long counselorId, NotificationType type, String message) {
        Notification notification = Notification.builder()
                .userId(userId)
                .counselorId(counselorId)
                .type(type)
                .message(message)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    //예약 생성시 : 사용자,상담사 모두에게 알림
    @Transactional
    public void onReservationCreated(Long userId, Long counselorUserId, Long counselorId, String timeText) {
        create(userId, counselorId, NotificationType.RESERVATION,
                "[예약 완료]" + timeText + "상담 예약이 접수되었습니다.");
        create(counselorUserId, counselorId, NotificationType.RESERVATION,
                "[신규 예약]" + timeText + "상담 예약이 접수되었습니다.");
    }

    // 예약 취소 시
    @Transactional
    public void onReservationCanceled(Long userId, Long counselorUserId, Long counselorId, String timeText) {
        create(userId, counselorId, NotificationType.CANCEL,
                "[예약 취소] " + timeText + " 상담 예약이 취소되었습니다.");
        create(counselorUserId, counselorId, NotificationType.CANCEL,
                "[예약 취소] " + timeText + " 상담 예약이 취소되었습니다.");
    }

    // 예약 완료시
    @Transactional
    public void onReservationCompleted(Long userId, Long counselorUserId, Long counselorId, String timeText) {
        create(userId, counselorId, NotificationType.COMPLETE,
                "[상담 완료] " + timeText + " 상담이 완료되었습니다. 리뷰를 남겨주세요.");
        create(counselorUserId, counselorId, NotificationType.COMPLETE,
                "[상담 완료] " + timeText + " 상담이 완료되었습니다.");
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMy(Long userId, int page, int size) {
        Page<Notification> p = notificationRepository
                .findByUserIdOrderByIdDesc(userId, PageRequest.of(page, size));
        return p.map(NotificationResponse::fromEntity).getContent();
    }

    @Transactional
    public void markRead(Long id, Long userId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        if (!notification.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        notification.markRead();
    }

    @Transactional
    public int markAllRead(Long userId) {
        List<Notification> list = notificationRepository
                .findByUserIdOrderByIdDesc(userId, PageRequest.of(0, 500)).getContent();
        int changed = 0;
        for (Notification n : list) {
            if (!n.isRead()) { n.markRead(); changed++; }
        }
        return changed;
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

}
