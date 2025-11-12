package service.saju_taro_service.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // ✅ 공통
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근이 거부되었습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),

    // ✅ 인증 / 사용자 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    EMAIL_DUPLICATED(HttpStatus.BAD_REQUEST, "이미 등록된 이메일입니다."),
    NICKNAME_DUPLICATED(HttpStatus.BAD_REQUEST, "이미 사용 중인 닉네임입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호가 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    // ✅ 예약 관련
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."),
    RESERVATION_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "이미 완료된 예약입니다."),
    RESERVATION_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, "해당 예약은 취소할 수 없습니다."),
    SCHEDULE_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "해당 시간은 예약할 수 없습니다."),

    // ✅ 결제 관련
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 정보를 찾을 수 없습니다."),
    PAYMENT_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "이미 완료된 결제입니다."),
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "결제 처리에 실패했습니다."),
    REFUND_FAILED(HttpStatus.BAD_REQUEST, "환불 처리에 실패했습니다."),

    // ✅ 리뷰 관련
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "후기를 찾을 수 없습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 후기를 작성했습니다."),
    
    // ✅ 프로필 관련
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "프로필을 찾을 수 없습니다."),
    
    // ✅ 스케줄 관련
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "스케줄을 찾을 수 없습니다."),
    SCHEDULE_ALREADY_BOOKED(HttpStatus.BAD_REQUEST, "이미 예약된 스케줄입니다."),
    
    // ✅ 즐겨찾기 관련
    FAVORITE_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 즐겨찾기에 등록되어 있습니다."),
    FAVORITE_NOT_FOUND(HttpStatus.NOT_FOUND, "즐겨찾기 정보를 찾을 수 없습니다."),
    
    // ✅ 파일 업로드 관련
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}