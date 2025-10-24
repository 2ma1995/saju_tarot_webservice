package service.saju_taro_service.controller.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.saju_taro_service.dto.schedule.ScheduleRequest;
import service.saju_taro_service.service.scheduleService.ScheduleService;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;

    /** ✅ 상담사 스케줄 등록 */
    @PostMapping
    public ResponseEntity<?> createSchedule(@RequestBody ScheduleRequest req) {
        return ResponseEntity.ok(scheduleService.createSchedule(req));
    }

    /** ✅ 상담사별 스케줄 조회 */
    @GetMapping("/counselor/{counselorId}")
    public ResponseEntity<?> getCounselorSchedules(@PathVariable Long counselorId) {
        return ResponseEntity.ok(scheduleService.getCounselorSchedules(counselorId));
    }

    /** ✅ 스케줄 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.ok("스케줄이 삭제되었습니다.");
    }
}
