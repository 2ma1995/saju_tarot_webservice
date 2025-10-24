package service.saju_taro_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import service.saju_taro_service.domain.review.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Review> findByIsActiveTrueOrderByCreatedAtDesc();

    Optional<Review> findByReservationId(Long reservationId);

    //평점 계산
    @Query("SELECT coalesce(avg(r.rating),0)from Review r where r.counselorId = :counselorId and r.isActive=true ")
    Double getAverageRatingByCounselor(@Param("counselorId") Long counselorId);

    Long countByCounselorIdAndIsActiveTrue(Long counselorId);

    List<Review> findByCounselorIdAndIsActiveTrueOrderByCreatedAtDesc(Long counselorId);

    Page<Review> findByCounselorIdAndIsActiveTrue(Long counselorId, Pageable pageable);

}
