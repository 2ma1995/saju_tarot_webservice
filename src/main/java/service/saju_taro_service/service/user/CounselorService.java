package service.saju_taro_service.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.saju_taro_service.domain.review.Review;
import service.saju_taro_service.domain.user.User;
import service.saju_taro_service.dto.review.ReviewResponse;
import service.saju_taro_service.dto.user.CounselorDetailResponse;
import service.saju_taro_service.dto.user.CounselorResponse;
import service.saju_taro_service.global.exception.CustomException;
import service.saju_taro_service.global.exception.ErrorCode;
import service.saju_taro_service.repository.ReviewRepository;
import service.saju_taro_service.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CounselorService {
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    // 상담사 목록
    @Transactional(readOnly = true)
    public Page<CounselorResponse> getCounselorList(String sort, int page, int size) {
        // 정렬 기준 매핑
        Sort sortOption = switch (sort == null ? "" : sort) {
            case "reviews" -> Sort.by(Sort.Direction.DESC, "reviewCount");
            case "recent" -> Sort.by(Sort.Direction.DESC, "updatedAt");
            default -> Sort.by(Sort.Direction.DESC, "averageRating");
        };

        PageRequest pageable = PageRequest.of(page, size, sortOption);

        return userRepository.findAllActiveCounselors(pageable)
                .map(CounselorResponse::fromEntity);
    }

    /**
     * ✅ 상담사 상세 조회 (평점 + 후기)
     */
    @Transactional(readOnly = true)
    public CounselorDetailResponse getCounselorDetail(Long counselorId) {
        User counselor = userRepository.findById(counselorId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "상담사를 찾을 수 없습니다."));

        if (!counselor.isActive() || !"COUNSELOR".equals(counselor.getUserRole().name())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "활성화된 상담사가 아닙니다.");
        }
        // 최신 후기 3개만 조회
        PageRequest top3 = PageRequest.of(0, 3, Sort.by("createdAt").descending());
        List<ReviewResponse> reviews = reviewRepository
                .findByCounselorIdAndIsActiveTrue(counselorId, top3)
                .stream()
                .map(r -> {
                    User reviewer = userRepository.findById(r.getUser().getId()).orElse(null);
                    return ReviewResponse.fromEntity(r, reviewer);
                })
                .toList();
        return CounselorDetailResponse.fromEntity(counselor, reviews);
    }

    // 상담사 후기 전체 조회(페이징)
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByCounselor(Long counselorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewRepository.findByCounselorIdAndIsActiveTrue(counselorId, pageable);

        return reviews.map(r -> {
            User user = userRepository.findById(r.getUser().getId()).orElse(null);
            return ReviewResponse.fromEntity(r, user);
        });
    }
}
