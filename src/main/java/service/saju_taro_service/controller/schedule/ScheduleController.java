package service.saju_taro_service.controller.schedule;

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
import service.saju_taro_service.dto.schedule.ScheduleRequest;
import service.saju_taro_service.service.scheduleService.ScheduleService;

@Tag(name = "Schedule API", description = "상담사 스케줄 등록, 조회, 삭제 기능")
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;

    /** ✅ 상담사 스케줄 등록 */
    @Operation(
            summary = "상담사 스케줄 등록",
            description = """
                    상담사가 자신의 예약 가능 일정을 등록합니다.  
                    날짜와 시간대를 지정하여 상담 가능 시간을 추가할 수 있습니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스케줄 등록 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "scheduleId": 101,
                                      "counselorId": 5,
                                      "date": "2025-10-28",
                                      "startTime": "14:00",
                                      "endTime": "15:00",
                                      "status": "AVAILABLE"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
            @ApiResponse(responseCode = "401", description = "로그인이 필요함", content = @Content)
    })
    @PostMapping
    public ResponseEntity<?> createSchedule(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "스케줄 등록 요청 DTO",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ScheduleRequest.class))
            )
            @RequestBody ScheduleRequest req) {
        return ResponseEntity.ok(scheduleService.createSchedule(req));
    }

    /** ✅ 상담사별 스케줄 조회 */
    @Operation(
            summary = "상담사 스케줄 조회",
            description = """
                    상담사 ID를 기준으로 해당 상담사의 전체 스케줄을 조회합니다.  
                    예약 가능 / 예약 완료 상태를 함께 반환합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    [
                                      {
                                        "scheduleId": 101,
                                        "date": "2025-10-28",
                                        "startTime": "14:00",
                                        "endTime": "15:00",
                                        "status": "AVAILABLE"
                                      },
                                      {
                                        "scheduleId": 102,
                                        "date": "2025-10-28",
                                        "startTime": "15:00",
                                        "endTime": "16:00",
                                        "status": "BOOKED"
                                      }
                                    ]
                                    """))),
            @ApiResponse(responseCode = "404", description = "상담사 정보를 찾을 수 없음", content = @Content)
    })
    @GetMapping("/counselor/{counselorId}")
    public ResponseEntity<?> getCounselorSchedules(
            @Parameter(description = "조회할 상담사 ID",example = "5")
            @PathVariable Long counselorId) {
        return ResponseEntity.ok(scheduleService.getCounselorSchedules(counselorId));
    }

    /** ✅ 스케줄 삭제 */
    @Operation(
            summary = "스케줄 삭제",
            description = """
                    상담사가 자신의 등록된 스케줄 중 특정 일정을 삭제합니다.  
                    이미 예약된 스케줄은 삭제할 수 없습니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스케줄 삭제 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "\"스케줄이 삭제되었습니다.\""))),
            @ApiResponse(responseCode = "403", description = "본인 스케줄이 아니거나 예약된 일정", content = @Content),
            @ApiResponse(responseCode = "404", description = "스케줄 ID를 찾을 수 없음", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSchedule(
            @Parameter(description = "삭제할 스케줄 ID",example = "101")
            @PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.ok("스케줄이 삭제되었습니다.");
    }
}
