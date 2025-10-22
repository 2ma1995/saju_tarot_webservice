package service.saju_taro_service.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.saju_taro_service.domain.user.UserRole;
import service.saju_taro_service.dto.user.UserResponse;
import service.saju_taro_service.global.util.SecurityUtil;
import service.saju_taro_service.service.admin.AdminUserService;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final AdminUserService adminUserService;

    /** ✅ 관리자: 사용자 목록 조회 */
    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String currentRole = SecurityUtil.currentRole();
        if (!"ADMIN".equals(currentRole)) {
            return ResponseEntity.status(403).body("관리자만 접근할 수 있습니다.");
        }

        Page<UserResponse> users = adminUserService.getAllUsers(role, active, page, size);
        return ResponseEntity.ok(users);
    }

    /** ✅ 관리자: 사용자 역할 변경 */
    @PutMapping("/{userId}/role")
    public ResponseEntity<?> changeRole(@PathVariable Long userId,
                                        @RequestParam String newRole) {
        String currentRole = SecurityUtil.currentRole();
        if (!"ADMIN".equals(currentRole)) {
            return ResponseEntity.status(403).body("관리자만 접근할 수 있습니다.");
        }
        adminUserService.changeUserRole(userId, UserRole.valueOf(newRole));
        return ResponseEntity.ok("사용자 역할이 변경되었습니다.");
    }

    /** ✅ 관리자: 사용자 비활성화 */
    @PutMapping("/{userId}/deactivate")
    public ResponseEntity<?> deactivate(@PathVariable Long userId) {
        String currentRole = SecurityUtil.currentRole();
        if (!"ADMIN".equals(currentRole)) {
            return ResponseEntity.status(403).body("관리자만 접근할 수 있습니다.");
        }

        adminUserService.deactivateUser(userId);
        return ResponseEntity.ok("사용자가 비활성화되었습니다.");
    }

    /** ✅ 관리자: 다른 사용자에게 관리자 권한 부여 */
    @PostMapping("/{targetId}/promote")
    public ResponseEntity<?> promoteToAdmin(@PathVariable Long targetId) {
        Long requesterId = SecurityUtil.currentUserId();
        String requesterRole = SecurityUtil.currentRole();

        if (!"ADMIN".equals(requesterRole)) {
            return ResponseEntity.status(403).body("관리자만 권한을 부여할 수 있습니다.");
        }

        adminUserService.promoteToAdmin(targetId, requesterId);
        return ResponseEntity.ok("해당 사용자에게 관리자 권한이 부여되었습니다.");
    }
}
