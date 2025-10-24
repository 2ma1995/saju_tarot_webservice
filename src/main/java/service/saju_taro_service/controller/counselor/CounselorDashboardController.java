package service.saju_taro_service.controller.counselor;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import service.saju_taro_service.global.exception.CustomException;
import service.saju_taro_service.global.exception.ErrorCode;
import service.saju_taro_service.global.util.SecurityUtil;
import service.saju_taro_service.service.counselor.CounselorDashboardService;

@RestController
@RequestMapping("/api/counselors/dashboard")
@RequiredArgsConstructor
public class CounselorDashboardController {

    private final CounselorDashboardService counselorDashboardService;

    // 오늘 일정 + 예약 통합
    @GetMapping
    public ResponseEntity<?> getDashboard() {
        String role = SecurityUtil.currentRole();
        if (!"COUNSELOR".equals(role)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "상담사만 접근 가능합니다.");
        }
        return ResponseEntity.ok(counselorDashboardService.getDashboard());
    }
    
    // 월간 예약 통합
    @GetMapping
    public ResponseEntity<?> getDashboard(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        String role = SecurityUtil.currentRole();
        if (!"COUNSELOR".equals(role)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "상담사만 접근 가능합니다.");
        }
        return ResponseEntity.ok(counselorDashboardService.getMonthlyDashboard(year, month));
    }
}


