package service.saju_taro_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import service.saju_taro_service.domain.review.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Review> findByIsActiveTrueOrderByCreatedAtDesc();
    Optional<Review> findByReservationId(Long reservationId);
}
