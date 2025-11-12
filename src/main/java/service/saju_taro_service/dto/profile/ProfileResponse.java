package service.saju_taro_service.dto.profile;

import lombok.*;
import service.saju_taro_service.domain.profile.Profile;
import service.saju_taro_service.dto.review.ReviewResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ProfileResponse {
    private Long id;
    private Long counselorId;
    private String counselorName;
    private String bio;
    private String experience;
    private List<String> tags;
    private String imageUrl;
    private String createdAt;
    private Double averageRating;   // ⭐ 평균 평점
    private Integer reviewCount;    // ⭐ 후기 수
    private List<ReviewResponse> reviews; // ⭐ 후기 리스트 추가


    public static ProfileResponse fromEntity(Profile p) {
        return fromEntity(p, null, null, null);
    }

    public static ProfileResponse fromEntity(Profile p, Double avgRating, Integer reviewCount, List<ReviewResponse> reviewList) {
        // ✅ DB의 "타로,연애,진로" → ["타로", "연애", "진로"] 변환
        List<String> tagList = new ArrayList<>();
        if (p.getTags() != null && !p.getTags().isBlank()) {
            tagList = Arrays.asList(p.getTags().split(","));
        }

        return ProfileResponse.builder()
                .id(p.getId())
                .counselorId(p.getCounselor().getId())
                .counselorName(p.getCounselor().getName())
                .bio(p.getBio())
                .experience(p.getExperience())
                .tags(tagList)
                .imageUrl(p.getImageUrl())
                .createdAt(p.getCreatedAt().toString())
                .averageRating(avgRating)
                .reviewCount(reviewCount)
                .reviews(reviewList)
                .build();
    }
}
