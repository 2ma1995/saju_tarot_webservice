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
import org.springframework.web.bind.annotation.*;
import service.saju_taro_service.global.exception.CustomException;
import service.saju_taro_service.global.exception.ErrorCode;
import service.saju_taro_service.global.util.SecurityUtil;
import service.saju_taro_service.service.counselor.CounselorCalendarService;

@Tag(name = "Counselor Calendar API", description = "상담사 달력 조회 (월 단위 일정/예약 현황)")
@RestController
@RequestMapping("/api/counselors")
@RequiredArgsConstructor
public class CounselorCalendarController {
    private final CounselorCalendarService counselorCalendarService;

    /** ✅ 달력 조회 (월 단위) */
    @Operation(
            summary = "상담사 달력 조회 (월 단위)",
            description = """
                    상담사 ID, 연도, 월을 기준으로 해당 달의 일정(예약/가능시간)을 조회합니다.  
                    - 상담사는 자신의 일정 관리용으로 조회  
                    - 사용자는 상담 가능 시간 확인용으로 조회  
                    반환 데이터는 달력 형태의 리스트로 구성됩니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "counselorId": 5,
                                      "year": 2025,
                                      "month": 10,
                                      "calendar": [
                                        {
                                          "date": "2025-10-01",
                                          "availableSlots": [
                                            { "startTime": "10:00", "endTime": "11:00", "status": "AVAILABLE" },
                                            { "startTime": "13:00", "endTime": "14:00", "status": "BOOKED" }
                                          ]
                                        },
                                        {
                                          "date": "2025-10-02",
                                          "availableSlots": []
                                        }
                                      ]
                                    }
                                    """))),
            @ApiResponse(responseCode = "403", description = "로그인이 필요하거나 권한 없음", content = @Content)
    })
    @GetMapping("/{counselorId}/calendar")
    public ResponseEntity<?> getCalendar(
            @Parameter(description = "조회할 상담사 ID", example = "5")
            @PathVariable Long counselorId,
            @Parameter(description = "조회할 연도", example = "2025")
            @RequestParam int year,
            @Parameter(description = "조회할 월 (1~12)", example = "10")
            @RequestParam int month
    ) {
        String role = SecurityUtil.currentRole();
        if (!"COUNSELOR".equals(role) && !"USER".equals(role)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "로그인이 필요합니다.");
        }
        return ResponseEntity.ok(counselorCalendarService.getCalendar(counselorId, year, month, role));
    }

}


