package service.saju_taro_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
import service.saju_taro_service.domain.reservation.Reservation;
import service.saju_taro_service.domain.reservation.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
//    @Query("SELECT r FROM Reservation r WHERE r.reservationStatus = 'CONFIRMED' " +
//            "AND r.reservationTime BETWEEN :from AND :to")
//    List<Reservation> findActiveBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    List<Reservation> findByUserIdOrderByReservationTimeDesc(Long userId);

    List<Reservation> findByCounselorIdOrderByReservationTimeDesc(Long counselorId);

    List<Reservation> findByUserIdAndReservationStatusOrderByReservationTimeDesc(Long userId, ReservationStatus status);


    List<Reservation> findByCounselorIdAndReservationTimeAfterAndReservationStatus(
            Long counselorId,
            LocalDateTime now,
            ReservationStatus status
    );

    List<Reservation> findTop5ByCounselorIdAndReservationStatusOrderByReservationTimeDesc(
            Long counselorId,
            ReservationStatus status
    );

}
