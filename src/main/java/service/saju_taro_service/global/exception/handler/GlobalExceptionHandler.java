package service.saju_taro_service.global.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import service.saju_taro_service.global.exception.CustomException;
import service.saju_taro_service.global.exception.ErrorCode;

import java.time.LocalDateTime;
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
     * ✅ NullPointerException, IllegalArgumentException 등 일반 예외
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        log.error("[Exception] {}", e.getMessage(), e);
        return buildResponse(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    /**
     * ✅ 공통 응답 포맷
     */
    private ResponseEntity<Map<String, Object>> buildResponse(ErrorCode code, String message) {
        return ResponseEntity.status(code.getStatus()).body(Map.of(
                "status", code.getStatus().value(),
                "error", code.name(),
                "message", message,
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
