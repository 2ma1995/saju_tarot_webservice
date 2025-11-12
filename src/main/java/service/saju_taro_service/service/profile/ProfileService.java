package service.saju_taro_service.service.profile;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.saju_taro_service.domain.profile.Profile;
import service.saju_taro_service.domain.user.User;
import service.saju_taro_service.dto.profile.ProfileRequest;
import service.saju_taro_service.dto.profile.ProfileResponse;
import service.saju_taro_service.dto.review.ReviewResponse;
import service.saju_taro_service.global.exception.CustomException;
import service.saju_taro_service.global.exception.ErrorCode;
import service.saju_taro_service.repository.ProfileRepository;
import service.saju_taro_service.repository.ReviewRepository;
import service.saju_taro_service.repository.UserRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    /** ✅ 프로필 등록/수정 */
    @Transactional
    public ProfileResponse saveOrUpdate(Long userId, ProfileRequest req) {
        User counselor = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "상담사를 찾을 수 없습니다."));

        Profile profile = profileRepository.findByCounselorId(userId)
                .orElse(Profile.builder().counselor(counselor).build());

        profile.setBio(req.getBio());
        profile.setExperience(req.getExperience());

        if (req.getTags() != null && !req.getTags().isEmpty()) {
            profile.setTags(String.join(",", req.getTags()));
        }

        profile.setImageUrl(req.getImageUrl());

        Profile saved = profileRepository.save(profile);
        return ProfileResponse.fromEntity(saved);
    }

    /** ✅ 상담사 상세조회 (후기 포함) */
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long counselorId) {
        Profile profile = profileRepository.findByCounselorId(counselorId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "프로필이 존재하지 않습니다."));

        Double avg = reviewRepository.getAverageRatingByCounselor(counselorId);
        Long count = reviewRepository.countByCounselorIdAndIsActiveTrue(counselorId);

        List<ReviewResponse> reviewList = reviewRepository
                .findByCounselorIdAndIsActiveTrueOrderByCreatedAtDesc(counselorId)
                .stream()
                .map(r -> {
                    User u = userRepository.findById(r.getUser().getId()).orElse(null);
                    return ReviewResponse.fromEntity(r, u);
                })
                .toList();

        return ProfileResponse.fromEntity(profile, avg, count.intValue(), reviewList);
    }

    /** ✅ 상담사 목록 조회 (태그 + 정렬 + 페이징) */
    @Transactional(readOnly = true)
    public Page<ProfileResponse> getCounselorList(String tag, String sort, int page, int size) {
        // 1) 데이터 로드
        List<Profile> profiles = (tag != null && !tag.isBlank())
                ? profileRepository.searchByTag(tag)
                : profileRepository.findAll();

        // 2) 정렬 기준
        Comparator<Profile> comparator;
        if (sort == null) sort = "rating";

        if ("reviews".equalsIgnoreCase(sort)) {
            comparator = Comparator.comparingInt((service.saju_taro_service.domain.profile.Profile p) -> {
                User c = p.getCounselor();
                return (c != null && c.getReviewCount() != null) ? c.getReviewCount() : 0;
            }).reversed();
        } else if ("latest".equalsIgnoreCase(sort)) {
            comparator = Comparator.comparing(Profile::getCreatedAt).reversed();
        } else { // rating 기본
            comparator = Comparator.comparingDouble((service.saju_taro_service.domain.profile.Profile p) -> {
                User c = p.getCounselor();
                return (c != null && c.getAverageRating() != null) ? c.getAverageRating() : 0.0;
            }).reversed();
        }

        // ✅ DTO 변환
        List<ProfileResponse> result = profiles.stream()
                .sorted(comparator)
                .skip((long) page * size)
                .limit(size)
                .map(profile -> {
                    User c = profile.getCounselor();
                    Double avg = (c != null && c.getAverageRating() != null) ? c.getAverageRating() : 0.0;
                    Integer count = (c != null && c.getReviewCount() != null) ? c.getReviewCount() : 0;
                    return ProfileResponse.fromEntity(profile, avg, count, null);
                })
                .toList();

        return new PageImpl<>(result, PageRequest.of(page, size), profiles.size());
    }

    /** ✅ 프로필 이미지만 업데이트 */
    @Transactional
    public String updateProfileImage(Long userId, String imageUrl) {
        Profile profile = profileRepository.findByCounselorId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "프로필을 찾을 수 없습니다."));

        profile.setImageUrl(imageUrl);
        profileRepository.save(profile);

        return imageUrl;
    }

    /** ✅ 태그 전용 검색 */
    @Transactional(readOnly = true)
    public List<ProfileResponse> searchByTag(String tag) {
        List<Profile> list = profileRepository.searchByTag(tag);
        return list.stream()
                .map(p -> ProfileResponse.fromEntity(p, null, null, null))
                .toList();
    }
}
