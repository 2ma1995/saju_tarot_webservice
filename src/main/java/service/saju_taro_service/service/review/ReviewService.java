package service.saju_taro_service.service.review;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.saju_taro_service.domain.reservation.Reservation;
import service.saju_taro_service.domain.reservation.ReservationStatus;
import service.saju_taro_service.domain.review.Review;
import service.saju_taro_service.domain.user.User;
import service.saju_taro_service.dto.review.ReviewRequest;
import service.saju_taro_service.dto.review.ReviewResponse;
import service.saju_taro_service.global.exception.CustomException;
import service.saju_taro_service.global.exception.ErrorCode;
import service.saju_taro_service.global.util.SecurityUtil;
import service.saju_taro_service.repository.ReservationRepository;
import service.saju_taro_service.repository.ReviewRepository;
import service.saju_taro_service.repository.UserRepository;

import java.util.List;
// import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    // 후기 작성
    @Transactional
    public ReviewResponse createReview(ReviewRequest req) {
        Long userId = SecurityUtil.currentUserId();
        if (userId == null) throw new CustomException(ErrorCode.UNAUTHORIZED);

        // 예약 존재 여부 확인(예약 검증)
        Reservation reservation = reservationRepository.findById(req.getReservationId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        // 본인 예약 여부 확인
        if (!reservation.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "본인 예약에 대해서만 후기를 작성할 수 있습니다.");
        }

        // 완료된 예약인지 확인
        if (reservation.getReservationStatus() != ReservationStatus.COMPLETED) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "완료된 상담만 후기를 작성할 수 있습니다.");
        }

        // 중복 작성 방지
        reviewRepository.findByReservationId(req.getReservationId())
                .ifPresent(r -> {
                    throw new CustomException(ErrorCode.BAD_REQUEST, "이미 후기를 작성한 예약입니다.");
                });

        //저장
        Review review = Review.builder()
                .user(reservation.getUser())
                .reservation(reservation)
                .counselor(reservation.getCounselor())
                .rating(req.getRating())
                .comment(req.getComment())
                .isActive(true)
                .build();

        Review saved = reviewRepository.save(review);

        // 상담사 평균 평점 계산
        updateCounselorAverageRating(reservation.getCounselor().getId());

        return ReviewResponse.fromEntity(saved, reservation.getUser());
    }

    // 내 후기 목록
    @Transactional(readOnly = true)
    public List<ReviewResponse> getMyReviews(Long userId) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(r-> ReviewResponse.fromEntity(r,r.getUser())).toList();
    }

    // 상담사별 후기 조회( 페이징 )
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsByCounselor(Long counselorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewRepository.findByCounselorIdAndIsActiveTrue(counselorId, pageable);
        return reviews.map(r -> ReviewResponse.fromEntity(r, r.getUser()));
    }

    // 전체 후기 조회(관리자)
    @Transactional(readOnly = true)
    public List<ReviewResponse> getAllReviews() {
        String role = SecurityUtil.currentRole();
        if (!"ADMIN".equals(role)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        return reviewRepository.findByIsActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(r-> ReviewResponse.fromEntity(r, r.getUser()))
                .toList();
    }

    // 후기 삭제 (관리자)
    @Transactional
    public void deactivateReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "후기를 찾을 수 없습니다."));
        review.setActive(false);
        reviewRepository.save(review);

        // 상담사 평균 평점 다시 계산
        updateCounselorAverageRating(review.getCounselor().getId());
    }

    /** ✅ 상담사 평균 평점 및 후기 수 갱신 로직 */
    private void updateCounselorAverageRating(Long counselorId) {
        Double avg = reviewRepository.getAverageRatingByCounselor(counselorId);
        Long count = reviewRepository.countByCounselorIdAndIsActiveTrue(counselorId);

        User counselor = userRepository.findById(counselorId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "상담사를 찾을 수 없습니다."));
        counselor.setAverageRating(avg);
        counselor.setReviewCount(count.intValue());
        userRepository.save(counselor);
    }
}
