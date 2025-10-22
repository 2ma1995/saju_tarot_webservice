package service.saju_taro_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.saju_taro_service.dto.user.UserResponse;
import service.saju_taro_service.dto.user.UserSignupRequest;
import service.saju_taro_service.dto.user.UserUpdateRequest;
import service.saju_taro_service.global.util.SecurityUtil;
import service.saju_taro_service.service.user.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * 회원가입
     **/
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserSignupRequest req) {
        return ResponseEntity.ok(userService.signup(req));
    }

    /**
     * 사용자 조회
     **/
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    /**
     * 전체 사용자 조회
     **/
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        String role = SecurityUtil.currentRole();
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * 사용자 정보 수정
     **/
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest req) {
        Long tokenUserId = SecurityUtil.currentUserId();
        String tokenRole = SecurityUtil.currentRole();
        return ResponseEntity.ok(userService.updateUser(id, req, tokenUserId, tokenRole));
    }

    /**
     * 사용자 비활성화(소프트 딜리트)
     **/
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        Long tokenUserId = SecurityUtil.currentUserId();
        String tokenRole = SecurityUtil.currentRole();
        userService.deactivateUser(id, tokenUserId, tokenRole);
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }

}
