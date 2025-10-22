package service.saju_taro_service.service.role;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.saju_taro_service.domain.role.RoleRequest;
import service.saju_taro_service.domain.user.User;
import service.saju_taro_service.domain.user.UserRole;
import service.saju_taro_service.repository.RoleRequestRepository;
import service.saju_taro_service.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleRequestService {
    private final RoleRequestRepository roleRequestRepository;
    private final UserRepository userRepository;

    // 사용자가 상담사로 요청
    @Transactional
    public void requestCounselorRole(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        if (user.getUserRole() != UserRole.USER) {
            throw new IllegalStateException("USER만 신청할 수 있습니다.");
        }
        //중복 요청 방지
        boolean alreadyRequested = roleRequestRepository.findByUserId(userId).stream()
                .anyMatch(r -> r.getStatus() == RoleRequest.RequestStatus.PENDING);
        if (alreadyRequested) {
            throw new IllegalStateException("이미 대기 중인 요청이 있습니다.");
        }
        RoleRequest req = RoleRequest.builder()
                .userId(userId)
                .requestedRole(UserRole.COUNSELOR)
                .status(RoleRequest.RequestStatus.PENDING)
                .build();
        roleRequestRepository.save(req);
    }

    /** 관리자: 모든 대기 요청 조회 */
    @Transactional(readOnly = true)
    public List<RoleRequest> getPendingRequests() {
        return roleRequestRepository.findByStatus(RoleRequest.RequestStatus.PENDING);
    }

    /** 관리자: 요청 승인 */
    @Transactional
    public void approveRequest(Long requestId) {
        RoleRequest req = roleRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("요청 없음"));

        if (req.getStatus() != RoleRequest.RequestStatus.PENDING)
            throw new IllegalStateException("이미 처리된 요청입니다.");

        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        user.setUserRole(req.getRequestedRole());
        req.setStatus(RoleRequest.RequestStatus.APPROVED);
    }

    /** 관리자: 요청 거절 */
    @Transactional
    public void rejectRequest(Long requestId) {
        RoleRequest req = roleRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("요청 없음"));

        if (req.getStatus() != RoleRequest.RequestStatus.PENDING)
            throw new IllegalStateException("이미 처리된 요청입니다.");

        req.setStatus(RoleRequest.RequestStatus.REJECTED);
    }


}
