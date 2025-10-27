package service.saju_taro_service.service.favorite;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.saju_taro_service.domain.favorite.FavoriteCounselor;
import service.saju_taro_service.dto.profile.ProfileResponse;
import service.saju_taro_service.global.exception.CustomException;
import service.saju_taro_service.global.exception.ErrorCode;
import service.saju_taro_service.repository.FavoriteCounselorRepository;
import service.saju_taro_service.repository.ProfileRepository;
import service.saju_taro_service.repository.UserRepository;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteCounselorRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    /** ✅ 즐겨찾기 추가 */
    @Transactional
    public void addFavorite(Long userId, Long counselorId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        var counselor = userRepository.findById(counselorId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "상담사를 찾을 수 없습니다."));


        if (favoriteRepository.existsByUserIdAndCounselorId(userId, counselorId)) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "이미 즐겨찾기에 등록된 상담사입니다.");
        }

        favoriteRepository.save(FavoriteCounselor.builder()
                .user(user)
                .counselor(counselor)
                .build());
    }

    /** ✅ 즐겨찾기 해제 */
    @Transactional
    public void removeFavorite(Long userId, Long counselorId) {
        FavoriteCounselor fav = favoriteRepository.findByUserIdAndCounselorId(userId, counselorId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "즐겨찾기 내역이 없습니다."));
        favoriteRepository.delete(fav);
    }

    /** ✅ 내 즐겨찾기 목록 */
    @Transactional(readOnly = true)
    public List<ProfileResponse> getMyFavorites(Long userId) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(f -> {
                    return profileRepository.findByCounselorId(f.getCounselor().getId())
                            .map(p -> ProfileResponse.fromEntity(p,
                                    p.getCounselor().getAverageRating(),
                                    p.getCounselor().getReviewCount(),
                                    null))
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
