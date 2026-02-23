package service.saju_taro_service.global.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import service.saju_taro_service.global.exception.CustomException;
import service.saju_taro_service.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * ✅ CustomException (우리 정의한 예외)
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> handleCustomException(CustomException e) {
        ErrorCode code = e.getErrorCode();
        log.error("[CustomException] {} - {}", code.name(), e.getMessage());
        return buildResponse(code, e.getMessage());
    }

    /**
     * ✅ Spring Security AccessDeniedException (권한 없음)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException e) {
        log.error("[AccessDeniedException] {}", e.getMessage());
        return buildResponse(ErrorCode.ACCESS_DENIED, "접근 권한이 없습니다.");
    }

    /**
     * ✅ Spring Security AuthenticationException (인증 실패)
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthenticationException(AuthenticationException e) {
        log.error("[AuthenticationException] {}", e.getMessage());
        return buildResponse(ErrorCode.UNAUTHORIZED, "인증에 실패했습니다.");
    }

    /**
     * ✅ 405 Method Not Allowed (예: GET으로 POST 전용 엔드포인트 접근)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("[MethodNotSupported] {} - 허용 메서드: {}", e.getMessage(), e.getSupportedHttpMethods());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(java.util.Map.of(
                "status", 405,
                "error", "METHOD_NOT_ALLOWED",
                "message", "지원하지 않는 HTTP 메서드입니다: " + e.getMethod(),
                "timestamp", java.time.LocalDateTime.now().toString()));
    }

    /**
     * ✅ 404 Not Found (존재하지 않는 경로)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<?> handleNoHandlerFound(NoHandlerFoundException e) {
        log.warn("[NoHandlerFound] {}", e.getMessage());
        return buildResponse(ErrorCode.NOT_FOUND, "존재하지 않는 API 경로입니다: " + e.getRequestURL());
    }

    /**
     * ✅ 415 Unsupported Media Type (Content-Type 불일치)
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<?> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        log.warn("[MediaTypeNotSupported] {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(java.util.Map.of(
                "status", 415,
                "error", "UNSUPPORTED_MEDIA_TYPE",
                "message", "Content-Type이 올바르지 않습니다. application/json을 사용하세요.",
                "timestamp", java.time.LocalDateTime.now().toString()));
    }

    /**
     * ✅ Validation 예외 (@Valid 검증 실패 시)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });

        log.error("[ValidationException] {}", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "VALIDATION_ERROR",
                "message", "입력값 검증에 실패했습니다.",
                "errors", errors,
                "timestamp", LocalDateTime.now().toString()));
    }

    /**
     * ✅ IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("[IllegalArgumentException] {}", e.getMessage());
        return buildResponse(ErrorCode.BAD_REQUEST, e.getMessage());
    }

    /**
     * ✅ IllegalStateException
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalStateException(IllegalStateException e) {
        log.error("[IllegalStateException] {}", e.getMessage());
        return buildResponse(ErrorCode.BAD_REQUEST, e.getMessage());
    }

    /**
     * ✅ NullPointerException
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<?> handleNullPointerException(NullPointerException e) {
        log.error("[NullPointerException] {}", e.getMessage(), e);
        return buildResponse(ErrorCode.INTERNAL_SERVER_ERROR, "시스템 오류가 발생했습니다.");
    }

    /**
     * ✅ 기타 모든 예외
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        log.error("[Exception] {}", e.getMessage(), e);
        return buildResponse(ErrorCode.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
    }

    /**
     * ✅ 공통 응답 포맷
     */
    private ResponseEntity<Map<String, Object>> buildResponse(ErrorCode code, String message) {
        return ResponseEntity.status(code.getStatus()).body(Map.of(
                "status", code.getStatus().value(),
                "error", code.name(),
                "message", message,
                "timestamp", LocalDateTime.now().toString()));
    }
}
