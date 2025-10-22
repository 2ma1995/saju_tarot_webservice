package service.saju_taro_service.controller.review;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.saju_taro_service.dto.review.ReviewRequest;
import service.saju_taro_service.global.util.SecurityUtil;
import service.saju_taro_service.service.review.ReviewService;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    // 후기 작성
    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest req) {
        return ResponseEntity.ok(reviewService.createReview(req));
    }

    // 내후기 조회
    @GetMapping("/my")
    public ResponseEntity<?> getMyReviews() {
        Long userId = SecurityUtil.currentUserId();
        return ResponseEntity.ok(reviewService.getMyReviews(userId));
    }

    // 전체 후기 조회
    @GetMapping
    public ResponseEntity<?> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    // 후기 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        String role = SecurityUtil.currentRole();
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("관리자만 삭제할 수 있습니다.");
        }
        reviewService.deactivateReview(id);
        return ResponseEntity.ok("후기가 삭제되었습니다.");
    }
}
