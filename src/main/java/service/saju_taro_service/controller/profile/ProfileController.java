package service.saju_taro_service.controller.profile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import service.saju_taro_service.dto.profile.ProfileRequest;
import service.saju_taro_service.global.util.SecurityUtil;
import service.saju_taro_service.service.profile.ProfileService;
import service.saju_taro_service.service.profile.UploadService;

import java.util.Map;

@Tag(name = "Profile API", description = "상담사 프로필 등록, 조회, 이미지 업로드 및 태그 검색 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/counselors/profile")
public class ProfileController {
    private final ProfileService profileService;
    private final UploadService uploadService;

    // ✅ 프로필 등록/수정 (상담사 본인 전용)
    @Operation(
            summary = "상담사 프로필 등록 또는 수정",
            description = """
                    상담사가 자신의 프로필 정보를 등록하거나 수정합니다.  
                    자기소개, 경력, 분야(태그), 이미지 URL 등을 포함합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "등록/수정 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "counselorId": 5,
                                      "bio": "심리상담과 타로리딩 전문가입니다.",
                                      "experience": "10년",
                                      "tags": ["타로", "연애", "진로"],
                                      "imageUrl": "https://example.com/profile5.jpg"
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "로그인이 필요함", content = @Content),
            @ApiResponse(responseCode = "400", description = "요청 데이터 오류", content = @Content)
    })
    @PutMapping
    public ResponseEntity<?> updateProfile(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "프로필 등록/수정 요청 DTO",
                    required = true,
                    content = @Content(schema = @Schema(example = """
                {
                  "bio": "타로와 사주를 결합한 맞춤형 상담을 제공합니다.",
                  "experience": "8년",
                  "tags": ["타로", "연애", "진로"],
                  "imageUrl": "https://cdn.saju-taro.com/profile123.jpg"
                }
                """))
            )
            @RequestBody ProfileRequest req) {
        Long userId = SecurityUtil.currentUserId();
        return ResponseEntity.ok(profileService.saveOrUpdate(userId, req));
    }

    // ✅ 상담사 프로필 조회 (상세 페이지용)
    @Operation(
            summary = "상담사 프로필 조회",
            description = """
                    상담사 ID를 기준으로 프로필 상세 정보를 조회합니다.  
                    자기소개, 태그, 후기, 평점 등을 함께 반환합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "id": 5,
                                      "name": "타로마스터",
                                      "bio": "마음이 편안해지는 상담을 제공합니다.",
                                      "experience": "8년",
                                      "tags": ["타로", "연애", "심리"],
                                      "rating": 4.9,
                                      "reviewCount": 120,
                                      "reviews": [
                                        {
                                          "reviewId": 101,
                                          "userName": "김민수",
                                          "score": 5,
                                          "comment": "정확하고 따뜻한 상담이었어요!"
                                        }
                                      ]
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "해당 상담사를 찾을 수 없음", content = @Content)
    })
    @GetMapping("/{counselorId}")
    public ResponseEntity<?> getProfile(
            @Parameter(description = "상담사 ID", example = "5")
            @PathVariable Long counselorId) {
        return ResponseEntity.ok(profileService.getProfile(counselorId));
    }

    // ✅ 프로필 이미지
    @Operation(
            summary = "프로필 이미지 업로드",
            description = """
                    상담사 프로필 이미지를 업로드하고,  
                    업로드된 이미지의 접근 가능한 URL을 반환합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "업로드 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "imageUrl": "https://cdn.saju-taro.com/uploads/profile-12345.jpg"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "파일이 비어있거나 손상됨", content = @Content)
    })
    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadImage(
            @Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestParam("file") MultipartFile file) {
        String imageUrl = uploadService.uploadProfileImage(file);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }

    // ✅ 태그 검색
    @Operation(
            summary = "태그 기반 상담사 검색",
            description = """
                    특정 태그(예: '연애', '심리', '진로')로 상담사를 검색합니다.  
                    해당 태그를 가진 상담사 리스트를 반환합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    [
                                      {
                                        "id": 1,
                                        "name": "타로상담가 지원",
                                        "bio": "심리 타로 전문상담사",
                                        "tags": ["타로", "연애"],
                                        "rating": 4.8
                                      },
                                      {
                                        "id": 2,
                                        "name": "혜림 상담가",
                                        "bio": "진로 타로 / 사주 병행 상담",
                                        "tags": ["진로", "사주"],
                                        "rating": 4.7
                                      }
                                    ]
                                    """))),
            @ApiResponse(responseCode = "404", description = "해당 태그로 검색된 상담사 없음", content = @Content)
    })
    @GetMapping("/search")
    public ResponseEntity<?> searchByTag(
            @Parameter(description = "검색할 태그명", example = "연애")
            @RequestParam String tag) {
        return ResponseEntity.ok(profileService.searchByTag(tag));
    }

}
