package service.saju_taro_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import service.saju_taro_service.domain.schedule.Schedule;
import service.saju_taro_service.domain.user.User;
import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    // 특정 기간 내 스케줄 조회
    List<Schedule> findByCounselorIdAndStartTimeBetweenOrderByStartTime(Long counselorId,
                                                                       LocalDateTime start,
                                                                       LocalDateTime end);
    // 중복 시간 검증
    boolean existsByCounselorIdAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(Long counselorId,
                                                                                   LocalDateTime end,
                                                                                   LocalDateTime start);

    // 단순 활성화된 전체 스케줄 조회
    List<Schedule> findByCounselorAndIsAvailableTrueOrderByStartTime(User counselorId);

}

