package service.saju_taro_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import service.saju_taro_service.domain.user.User;
import service.saju_taro_service.domain.user.UserRole;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndIsActiveTrue(String email);
    boolean existsByEmail(String email);

    Page<User> findByUserRole(UserRole userRole, Pageable pageable);
    Page<User> findByIsActive(Boolean isActive, Pageable pageable);
    Page<User> findByUserRoleAndIsActive(UserRole userRole, Boolean isActive, Pageable pageable);
}
