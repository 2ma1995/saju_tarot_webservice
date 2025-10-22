package service.saju_taro_service.service.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.saju_taro_service.domain.reservation.Reservation;
import service.saju_taro_service.domain.reservation.ReservationStatus;
import service.saju_taro_service.domain.review.Review;
import service.saju_taro_service.dto.review.ReviewRequest;
import service.saju_taro_service.dto.review.ReviewResponse;
import service.saju_taro_service.global.util.SecurityUtil;
import service.saju_taro_service.repository.ReservationRepository;
import service.saju_taro_service.repository.ReviewRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;

    // 후기 작성
    @Transactional
    public ReviewResponse createReview(ReviewRequest req) {
        Long userId = SecurityUtil.currentUserId();

        // 예약 존재 여부 확인
        Reservation reservation = reservationRepository.findById(req.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
        // 본인 예약 여부 확인
        if (!reservation.getUserId().equals(userId)) {
            throw new SecurityException("본인 예약에 대해서만 후기를 작성할 수 있습니다.");
        }
        // 완료된 예약인지 확인
        if (reservation.getReservationStatus() != ReservationStatus.COMPLETED) {
            throw new IllegalStateException("완료된 상담만 후기를 작성할 수 있습니다.");
        }
        // 중복 작성 방지
        reviewRepository.findByReservationId(req.getReservationId())
                .ifPresent(r->{
                    throw new IllegalStateException("이미 후기를 작성한 예약입니다.");});
        //저장
        Review review = Review.builder()
                .userId(userId)
                .reservationId(req.getReservationId())
                .rating(req.getRating())
                .comment(req.getComment())
                .build();
        return ReviewResponse.fromEntity(reviewRepository.save(review));
    }

    // 내 후기 목록
    @Transactional(readOnly = true)
    public List<ReviewResponse> getMyReviews(Long userId) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(ReviewResponse::fromEntity).toList();
    }

    // 전체 후기 조회(관리자)
    @Transactional(readOnly = true)
    public List<ReviewResponse> getAllReviews(){
        return reviewRepository.findByIsActiveTrueOrderByCreatedAtDesc()
                .stream().map(ReviewResponse::fromEntity).toList();
    }

    // 후기 삭제 (admin only)
    @Transactional
    public void deactivateReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("후기를 찾을 수 없습니다."));
        review.setActive(false);
    }
}
