package service.saju_taro_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import service.saju_taro_service.domain.favorite.FavoriteCounselor;

import java.util.List;
import java.util.Optional;

public interface FavoriteCounselorRepository extends JpaRepository<FavoriteCounselor, Long> {
    Optional<FavoriteCounselor> findByUserIdAndCounselorId(Long userId, Long counselorId);
    List<FavoriteCounselor> findByUserIdOrderByCreatedAtDesc(Long userId);
    boolean existsByUserIdAndCounselorId(Long userId, Long counselorId);
}
