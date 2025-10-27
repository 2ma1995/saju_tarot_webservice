package service.saju_taro_service.controller.user;

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
import service.saju_taro_service.service.user.CounselorService;

@Tag(name = "Counselor API", description = "상담사 목록 및 상세 조회 기능 (사용자/관리자 공용)")
@RestController
@RequestMapping("/api/counselors")
@RequiredArgsConstructor
public class CounselorController {
    private final CounselorService counselorService;

    /** ✅ 상담사 목록 조회 */
    @Operation(
            summary = "상담사 목록 조회",
            description = "상담사 리스트를 페이지네이션 및 정렬 옵션(rating, reviewCount 등)에 따라 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "content": [
                                        {
                                          "id": 1,
                                          "name": "타로마스터",
                                          "specialty": "타로 / 사주 종합상담",
                                          "rating": 4.9,
                                          "reviewCount": 120,
                                          "imageUrl": "https://example.com/profile1.jpg"
                                        },
                                        {
                                          "id": 2,
                                          "name": "사주상담가",
                                          "specialty": "사주 전문 / 연애 상담",
                                          "rating": 4.8,
                                          "reviewCount": 98,
                                          "imageUrl": "https://example.com/profile2.jpg"
                                        }
                                      ],
                                      "page": 0,
                                      "size": 10,
                                      "totalPages": 3,
                                      "totalElements": 25
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "잘못된 정렬 기준 혹은 페이지 요청", content = @Content)
    })
    @GetMapping
    public ResponseEntity<?> getCounselors(
            @Parameter(description = "정렬 기준 (rating, reviewCount 등)", example = "rating")
            @RequestParam(defaultValue = "rating") String sort,
            @Parameter(description = "조회할 페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "한 페이지당 데이터 개수", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(counselorService.getCounselorList(sort,page,size));
    }

    /** ✅ 상담사 상세 조회 */
    @Operation(
            summary = "상담사 상세 조회",
            description = "상담사 ID로 상세 정보를 조회합니다. 프로필, 경력, 후기, 예약 가능 일정 등이 포함됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "id": 1,
                                      "name": "타로마스터",
                                      "bio": "심리상담 기반 타로 리딩 전문가",
                                      "specialty": "타로 / 사주 종합상담",
                                      "experience": "10년",
                                      "rating": 4.9,
                                      "reviewCount": 120,
                                      "reviews": [
                                        {
                                          "reviewId": 1,
                                          "userName": "user123",
                                          "score": 5,
                                          "comment": "정확하고 따뜻한 상담 감사합니다!"
                                        }
                                      ],
                                      "availableSchedules": [
                                        {
                                          "date": "2025-10-28",
                                          "startTime": "14:00",
                                          "endTime": "15:00",
                                          "isBooked": false
                                        }
                                      ]
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "해당 ID의 상담사를 찾을 수 없음", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getCounselorDetail(@Parameter(description = "조회할 상담사 ID",example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(counselorService.getCounselorDetail(id));
    }
}
