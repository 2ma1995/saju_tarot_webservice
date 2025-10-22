package service.saju_taro_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import service.saju_taro_service.domain.reservation.Reservation;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findActiveBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    List<Reservation> findByUserIdOrderByReservationTimeDesc(Long userId);
    List<Reservation> findByCounselorIdOrderByReservationTimeDesc(Long counselorId);
}
