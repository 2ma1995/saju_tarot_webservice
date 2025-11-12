package service.saju_taro_service.service.role;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.saju_taro_service.domain.role.RequestStatus;
import service.saju_taro_service.domain.role.RoleRequest;
import service.saju_taro_service.domain.user.User;
import service.saju_taro_service.domain.user.UserRole;
import service.saju_taro_service.global.exception.CustomException;
import service.saju_taro_service.global.exception.ErrorCode;
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
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND,"사용자를 찾을수 없습니다."));

        if (user.getUserRole() != UserRole.USER) {
            throw new CustomException(ErrorCode.BAD_REQUEST,"일반 사용자(USER)만 신청할 수 있습니다.");
        }

        //중복 요청 방지
        boolean alreadyRequested = roleRequestRepository.findByUserId(userId).stream()
                .anyMatch(r -> r.getStatus() == RequestStatus.PENDING);
        if (alreadyRequested) {
            throw new CustomException(ErrorCode.BAD_REQUEST,"이미 대기 중인 요청이 있습니다.");
        }

        RoleRequest req = RoleRequest.builder()
                .user(user)
                .requestedRole(UserRole.COUNSELOR)
                .status(RequestStatus.PENDING)
                .build();

        roleRequestRepository.save(req);
    }

    /** 관리자: 모든 대기 요청 조회 */
    @Transactional(readOnly = true)
    public List<RoleRequest> getPendingRequests() {
        return roleRequestRepository.findByStatus(RequestStatus.PENDING);
    }

    /** 관리자: 요청 승인 */
    @Transactional
    public void approveRequest(Long requestId) {
        RoleRequest req = roleRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND,"요청 없음"));

        if (req.getStatus() != RequestStatus.PENDING)
            throw new CustomException(ErrorCode.RESERVATION_ALREADY_COMPLETED,"이미 처리된 요청입니다.");

        User user = req.getUser();
        if (user == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND, "요청에 연결된 사용자가 없습니다.");
        }

        user.setUserRole(req.getRequestedRole());
        userRepository.save(user);

        req.setStatus(RequestStatus.APPROVED);
        roleRequestRepository.save(req);
    }

    /** 관리자: 요청 거절 */
    @Transactional
    public void rejectRequest(Long requestId) {
        RoleRequest req = roleRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND,"요청 없음"));

        if (req.getStatus() != RequestStatus.PENDING)
            throw new CustomException(ErrorCode.RESERVATION_ALREADY_COMPLETED,"이미 처리된 요청입니다.");

        req.setStatus(RequestStatus.REJECTED);
        roleRequestRepository.save(req);
    }


}
