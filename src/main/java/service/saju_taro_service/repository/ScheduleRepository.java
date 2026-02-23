package service.saju_taro_service.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import service.saju_taro_service.domain.schedule.Schedule;
import service.saju_taro_service.domain.user.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    // ✅ 비관적 락 (동시 예약 방지)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Schedule s WHERE s.id = :id")
    Optional<Schedule> findByIdWithLock(@Param("id") Long id);

}
