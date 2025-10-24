package service.saju_taro_service.controller.counselor;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.saju_taro_service.global.exception.CustomException;
import service.saju_taro_service.global.exception.ErrorCode;
import service.saju_taro_service.global.util.SecurityUtil;
import service.saju_taro_service.service.counselor.CounselorCalendarService;

@RestController
@RequestMapping("/api/counselors")
@RequiredArgsConstructor
public class CounselorCalendarController {
    private final CounselorCalendarService counselorCalendarService;

    /** ✅ 달력 조회 (월 단위) */
    @GetMapping("/{counselorId}/calendar")
    public ResponseEntity<?> getCalendar(
            @PathVariable Long counselorId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        String role = SecurityUtil.currentRole();
        if (!"COUNSELOR".equals(role) && !"USER".equals(role)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "로그인이 필요합니다.");
        }
        return ResponseEntity.ok(counselorCalendarService.getCalendar(counselorId, year, month, role));
    }

}


