package service.saju_taro_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import service.saju_taro_service.domain.user.User;
import service.saju_taro_service.domain.user.UserRole;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndIsActiveTrue(String email);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    Page<User> findByUserRole(UserRole userRole, Pageable pageable);
    Page<User> findByIsActive(Boolean isActive, Pageable pageable);
    Page<User> findByUserRoleAndIsActive(UserRole userRole, Boolean isActive, Pageable pageable);

    /** ✅ 상담사 목록 (활성화된 사용자만) */
    @Query("SELECT u FROM User u WHERE u.userRole = 'COUNSELOR' AND u.isActive = true ORDER BY u.averageRating DESC")
    List<User> findAllActiveCounselorsOrderByRatingDesc();

    /** ✅ 상담사 목록 정렬 옵션 포함 */
    @Query("""
        SELECT u FROM User u 
        WHERE u.userRole = 'COUNSELOR' AND u.isActive = true
        ORDER BY 
            CASE WHEN :sort = 'rating' THEN u.averageRating END DESC,
            CASE WHEN :sort = 'reviews' THEN u.reviewCount END DESC,
            CASE WHEN :sort = 'recent' THEN u.updatedAt END DESC
    """)
    List<User> findAllCounselorsSorted(String sort);

    /** ✅ 상담사 목록 (활성 + 페이징 + 동적 정렬) */
    @Query("""
        SELECT u FROM User u 
        WHERE u.userRole = 'COUNSELOR' AND u.isActive = true
    """)
    Page<User> findAllActiveCounselors(Pageable pageable);
}
