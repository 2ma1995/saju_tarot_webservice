package service.saju_taro_service.service.scheduleService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.saju_taro_service.domain.schedule.Schedule;
import service.saju_taro_service.dto.schedule.ScheduleRequest;
import service.saju_taro_service.dto.schedule.ScheduleResponse;
import service.saju_taro_service.global.exception.CustomException;
import service.saju_taro_service.global.exception.ErrorCode;
import service.saju_taro_service.global.util.SecurityUtil;
import service.saju_taro_service.repository.ScheduleRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;

    // 상담사 스케줄 등록
    @Transactional
    public ScheduleResponse createSchedule(ScheduleRequest req) {
        Long counselorId = SecurityUtil.currentUserId();
        String role = SecurityUtil.currentRole();
        if (!"COUNSELOR".equals(role)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "상담사만 등록 가능합니다.");
        }
        // 중복 시간 검증
        boolean conflict = scheduleRepository.existsByCounselorIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                counselorId, req.getEndTime(), req.getStartTime()
        );
        if (conflict) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "이미 등록된 시간대와 겹칩니다.");
        }
        Schedule schedule = Schedule.builder()
                .counselorId(counselorId)
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .isAvailable(true)
                .build();
        return ScheduleResponse.fromEntity(scheduleRepository.save(schedule));
    }

    // 상담사별 스케줄 조회
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getCounselorSchedules(Long counselorId) {
        return scheduleRepository.findByCounselorIdAndIsAvailableTrueOrderByStartTime(counselorId)
                .stream().map(ScheduleResponse::fromEntity).toList();
    }

    // 스케줄 삭제 (본인만 삭제 가능)
    @Transactional
    public void deleteSchedule(Long scheduleId) {
        Long counselorId = SecurityUtil.currentUserId();
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "스케줄을 찾을 수 없습니다."));
        if (!schedule.getCounselorId().equals(counselorId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "본인 스케줄만 삭제할 수 있습니다.");
        }
        scheduleRepository.delete(schedule);
    }
}
