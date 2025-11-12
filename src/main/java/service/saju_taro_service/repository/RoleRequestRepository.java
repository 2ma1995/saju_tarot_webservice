package service.saju_taro_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import service.saju_taro_service.domain.role.RequestStatus;
import service.saju_taro_service.domain.role.RoleRequest;

import java.util.List;

public interface RoleRequestRepository extends JpaRepository<RoleRequest, Long> {
    List<RoleRequest> findByStatus(RequestStatus status);
    List<RoleRequest> findByUserId(Long userId);
}
