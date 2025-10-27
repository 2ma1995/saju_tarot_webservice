package service.saju_taro_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import service.saju_taro_service.domain.profile.Profile;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByCounselorId(Long counselorId);

    @Query("SELECT p FROM Profile p WHERE p.tags LIKE %:tag%")
    List<Profile> searchByTag(@Param("tag") String tag);
}
