package service.saju_taro_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.saju_taro_service.global.exception.CustomException;
import service.saju_taro_service.global.exception.ErrorCode;
import service.saju_taro_service.global.util.SecurityUtil;
import service.saju_taro_service.service.role.RoleRequestService;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleRequestController {
    private final RoleRequestService roleRequestService;

    /**
     * 사용자 → 상담사 신청
     */
    @PostMapping("/request")
    public ResponseEntity<?> requestCounselorRole() {
        Long userId = SecurityUtil.currentUserId();
        roleRequestService.requestCounselorRole(userId);
        return ResponseEntity.ok("상담사 신청이 접수되었습니다.");
    }

    /** 관리자 → 대기 요청 조회 */
    @GetMapping("/requests")
    public ResponseEntity<?> getPendingRequests() {
        String role = SecurityUtil.currentRole();
        validateAdmin();
        return ResponseEntity.ok(roleRequestService.getPendingRequests());
    }

    /** 관리자 → 승인 */
    @PostMapping("/approve/{requestId}")
    public ResponseEntity<?> approve(@PathVariable Long requestId) {
        validateAdmin();
        roleRequestService.approveRequest(requestId);
        return ResponseEntity.ok("요청이 승인되었습니다.");
    }

    /**
     * 관리자 → 거절
     */
    @PostMapping("/reject/{requestId}")
    public ResponseEntity<?> reject(@PathVariable Long requestId) {
        validateAdmin();
        roleRequestService.rejectRequest(requestId);
        return ResponseEntity.ok("요청이 거절되었습니다.");
    }

    private void validateAdmin() {
        String role = SecurityUtil.currentRole();
        if (!"ADMIN".equals(role)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

}
