package service.saju_taro_service.controller.counselor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Counselor Dashboard API", description = "상담사 전용 대시보드 (오늘 일정, 월간 예약 통계 등)")
@RestController
@RequestMapping("/api/counselors/dashboard")
@RequiredArgsConstructor
public class CounselorDashboardController {

    private final CounselorDashboardService counselorDashboardService;

    // 오늘 일정 + 예약 통합
    @Operation(
            summary = "상담사 오늘 일정 대시보드",
            description = """
                    상담사가 오늘 진행할 예약 일정 및 상태 요약 정보를 조회합니다.  
                    예약 수, 완료 건수, 신규 예약 등 주요 정보를 포함합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "date": "2025-10-26",
                                      "totalReservations": 6,
                                      "completed": 3,
                                      "pending": 2,
                                      "cancelled": 1,
                                      "todaySchedules": [
                                        {
                                          "reservationId": 12,
                                          "userName": "김민수",
                                          "startTime": "10:00",
                                          "endTime": "11:00",
                                          "status": "CONFIRMED"
                                        },
                                        {
                                          "reservationId": 13,
                                          "userName": "박지현",
                                          "startTime": "13:00",
                                          "endTime": "14:00",
                                          "status": "PENDING"
                                        }
                                      ]
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "상담사만 접근 가능", content = @Content)
    })
    @GetMapping("/today")
    public ResponseEntity<?> getTodayDashboard() {
        validateCounselorRole();
        return ResponseEntity.ok(counselorDashboardService.getDashboard());
    }
    
    // 월간 예약 통합
    @Operation(
            summary = "상담사 월간 예약 대시보드",
            description = """
                    상담사가 특정 연도/월 기준으로 전체 예약 현황을 조회합니다.  
                    일별 예약 수, 완료율, 취소율 등의 통계를 포함합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "year": 2025,
                                      "month": 10,
                                      "totalReservations": 120,
                                      "completed": 98,
                                      "cancelled": 15,
                                      "dailyStats": [
                                        { "date": "2025-10-01", "count": 4 },
                                        { "date": "2025-10-02", "count": 7 },
                                        { "date": "2025-10-03", "count": 5 }
                                      ]
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "상담사만 접근 가능", content = @Content)
    })
    @GetMapping
    public ResponseEntity<?> getDashboard(
            @Parameter(description = "조회할 연도 (기본값: 현재 연도)", example = "2025")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "조회할 월 (기본값: 현재 월)", example = "10")
            @RequestParam(required = false) Integer month
    ) {
        validateCounselorRole();
        return ResponseEntity.ok(counselorDashboardService.getMonthlyDashboard(year, month));
    }

    // 상담사 권한 검증
    private static void validateCounselorRole() {
        String role = SecurityUtil.currentRole();
        if (!"COUNSELOR".equals(role)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "상담사만 접근 가능합니다.");
        }
    }
}


