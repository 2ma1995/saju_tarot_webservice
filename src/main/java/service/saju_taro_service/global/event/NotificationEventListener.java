package service.saju_taro_service.global.event;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import service.saju_taro_service.domain.notification.Notification;
import service.saju_taro_service.domain.notification.NotificationType;
import service.saju_taro_service.domain.user.User;
import service.saju_taro_service.repository.NotificationRepository;
import service.saju_taro_service.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FirebaseMessaging firebaseMessaging;
    private final JavaMailSender mailSender;

    @Async("notificationExecutor")
    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("📬 NotificationEvent received: {}", event.getType());

        // 1️⃣ DB 저장 (내부 알림)
        Notification n = Notification.builder()
                .userId(event.getUserId())
                .counselorId(event.getCounselorId())
                .type(event.getType())
                .message(event.getMessage())
                .isRead(false)
                .build();
        notificationRepository.save(n);

        // 2️⃣ FCM 전송
        try {
            User targetUser = userRepository.findById(event.getUserId()).orElse(null);
            if (targetUser == null || targetUser.getFcmToken() == null || targetUser.getFcmToken().isBlank()) {
                log.info("⚠️ FCM skipped: no valid token for userId={}", event.getUserId());
            }else {
                sendFcmWithRetry(event, targetUser.getFcmToken(), 0);
            }
            // 이메일 발송
            if (targetUser != null && targetUser.getEmail() != null && !targetUser.getEmail().isBlank()) {
                sendEmail(event, targetUser);
            }

        } catch (Exception e) {
            log.error("❌ FCM preparation failed. userId=" + event.getUserId(), e);
        }
    }

    /**
     * 🔁 FCM 발송 + 백오프 재시도 (최대 3회)
     */
    private void sendFcmWithRetry(NotificationEvent event, String token, int retryCount) {
        try {
            com.google.firebase.messaging.Notification fcm =
                    com.google.firebase.messaging.Notification.builder()
                            .setTitle(getTitle(event.getType()))
                            .setBody(event.getMessage())
                            .build();

            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(fcm)
                    .putData("type", event.getType().name())
                    .putData("counselorId", String.valueOf(event.getCounselorId()))
                    .build();

            FirebaseMessaging.getInstance().send(message);
            log.info("✅ FCM sent successfully to token={}, type={}", token, event.getType());

        } catch (FirebaseMessagingException e) {
            String errorCode = e.getErrorCode().toString();
            log.warn("⚠️ FCM send failed: code={} (attempt={})", errorCode, retryCount + 1);

            // ❌ 1) 유효하지 않은 토큰은 즉시 삭제
            if ("registration-token-not-registered".equals(errorCode)
                    || "invalid-argument".equals(errorCode)
                    || "invalid-registration-token".equals(errorCode)) {
                invalidateUserToken(token);
                return;
            }

            // 🔁 2) 네트워크/서버 오류면 재시도 (최대 3회)
            if (retryCount < 3 && shouldRetry(errorCode)) {
                int nextAttempt = retryCount + 1;
                long delayMs = (long) (Math.pow(2, nextAttempt) * 500L); // 0.5s → 1s → 2s → 4s
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ignored) {}
                sendFcmWithRetry(event, token, nextAttempt);
            } else {
                log.error("❌ FCM send failed permanently: code={}, message={}", errorCode, e.getMessage());
            }

        } catch (Exception e) {
            log.error("❌ Unexpected FCM send error", e);
        }
    }

    /**
     * 💡 재시도 가능한 에러코드만 골라냄
     */
    private boolean shouldRetry(String errorCode) {
        return errorCode != null && (
                errorCode.contains("internal") ||
                        errorCode.contains("unavailable") ||
                        errorCode.contains("server") ||
                        errorCode.contains("timeout")
        );
    }

    /**
     * 🚫 토큰 무효화 (DB에서 제거)
     */
    private void invalidateUserToken(String token) {
        try {
            User user = userRepository.findByFcmToken(token).orElse(null);
            if (user != null) {
                user.setFcmToken(null);
                userRepository.save(user);
                log.info("🧹 Invalid FCM token removed for userId={}", user.getId());
            }
        } catch (Exception e) {
            log.error("❌ Failed to remove invalid FCM token: {}", token, e);
        }
    }

    /**
     * 🔸 알림 제목 매핑
     */
    private String getTitle(NotificationType type) {
        return switch (type) {
            case RESERVATION -> "예약 완료";
            case CANCEL  -> "예약 취소";
            case COMPLETE -> "상담 완료";
            case PAYMENT -> "결제 완료";
            case REFUND -> "환불 완료";
        };
    }


    /** 📩 이메일 발송 */
    private void sendEmail(NotificationEvent event, User user) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(user.getEmail());
            helper.setSubject(getTitle(event.getType()) + " 알림");
            helper.setText("""
                    안녕하세요, %s님.<br><br>
                    %s<br><br>
                    - 사주타로 서비스 드림 -
                    """.formatted(user.getName(), event.getMessage()), true);

            mailSender.send(message);
            log.info("📧 Email sent to {}", user.getEmail());

        } catch (MessagingException e) {
            log.error("❌ Email send failed: {}", e.getMessage());
        }
    }
}
