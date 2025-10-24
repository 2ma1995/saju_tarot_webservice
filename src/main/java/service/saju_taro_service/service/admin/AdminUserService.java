package service.saju_taro_service.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.saju_taro_service.domain.user.User;
import service.saju_taro_service.domain.user.UserRole;
import service.saju_taro_service.dto.user.UserResponse;
import service.saju_taro_service.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AdminUserService {
    private final UserRepository userRepository;

    /**
     * 사용자 목록 조회
     **/
    public Page<UserResponse> getAllUsers(String role, Boolean active, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        // 필터링 조합 처리
        Page<User> result;

        if (role != null && active != null) {
            result = userRepository.findByUserRoleAndIsActive(UserRole.valueOf(role), active, pageRequest);
        } else if (role != null) {
            result = userRepository.findByUserRole(UserRole.valueOf(role), pageRequest);
        } else if (active != null) {
            result = userRepository.findByIsActive(active, pageRequest);
        } else {
            result = userRepository.findAll(pageRequest);
        }
        return result.map(UserResponse::fromEntity);
    }

    /** ✅ 관리자: 사용자 역할 변경 */
    @Transactional
    public void changeUserRole(Long userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setUserRole(newRole);
    }

    /** ✅ 관리자: 사용자 비활성화 */
    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setActive(false);
    }
    /** 관리자: 관리자 권한 주기 **/
    @Transactional
    public void promoteToAdmin(Long adminId, Long targetUserId) {
        // 관리자 검증 로직
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("요청자 없음."));
        if (admin.getUserRole() != UserRole.ADMIN) {
            throw new IllegalStateException("관리자만 권한 부여가 가능합니다.");
        }
        // 대상 유저 확인
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("대상 유저 없음"));
        // 중복 방지
        if (target.getUserRole() == UserRole.ADMIN) {
            throw new IllegalStateException("이미 관리자입니다.");
        }
        // 권한 부여
        target.setUserRole(UserRole.ADMIN);
    }
}