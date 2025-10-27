package service.saju_taro_service.controller.review;

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
import service.saju_taro_service.dto.review.ReviewRequest;
import service.saju_taro_service.global.exception.CustomException;
import service.saju_taro_service.global.exception.ErrorCode;
import service.saju_taro_service.global.util.SecurityUtil;
import service.saju_taro_service.service.review.ReviewService;

@Tag(name = "Review API", description = "후기 작성, 조회, 삭제 기능 (사용자/관리자용)")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    // 후기 작성
    @Operation(
            summary = "후기 작성",
            description = """
                    상담이 완료된 예약 건에 대해 사용자가 후기를 작성합니다.  
                    점수(score)와 내용(comment)을 포함합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "후기 작성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "reviewId": 101,
                                      "reservationId": 12,
                                      "userId": 5,
                                      "counselorId": 2,
                                      "score": 5,
                                      "comment": "정확하고 따뜻한 상담 감사합니다!",
                                      "createdAt": "2025-10-26T15:30:00"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
            @ApiResponse(responseCode = "401", description = "로그인이 필요함", content = @Content)
    })
    @PostMapping
    public ResponseEntity<?> createReview(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "후기 작성 요청 DTO",
                    required = true,
                    content = @Content(schema = @Schema(example = """
                {
                  "reservationId": 15,
                  "counselorId": 5,
                  "score": 5,
                  "comment": "정확하고 따뜻한 상담 감사합니다!"
                }
                """))
            )
            @RequestBody ReviewRequest req) {
        return ResponseEntity.ok(reviewService.createReview(req));
    }

    // 내후기 조회
    @Operation(
            summary = "내 후기 조회",
            description = "현재 로그인한 사용자가 작성한 후기 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    [
                                      {
                                        "reviewId": 101,
                                        "counselorName": "혜림 상담사",
                                        "score": 5,
                                        "comment": "다시 찾고 싶은 상담이었어요!",
                                        "createdAt": "2025-10-26T15:30:00"
                                      },
                                      {
                                        "reviewId": 102,
                                        "counselorName": "지원 상담사",
                                        "score": 4,
                                        "comment": "조언이 현실적이었어요.",
                                        "createdAt": "2025-10-20T11:10:00"
                                      }
                                    ]
                                    """))),
            @ApiResponse(responseCode = "401", description = "로그인이 필요함", content = @Content)
    })
    @GetMapping("/my")
    public ResponseEntity<?> getMyReviews() {
        Long userId = SecurityUtil.currentUserId();
        return ResponseEntity.ok(reviewService.getMyReviews(userId));
    }

    // 전체 후기 조회
    @Operation(
            summary = "전체 후기 조회",
            description = "모든 후기 목록을 조회합니다. (관리자 또는 공개용 기능)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    [
                                      {
                                        "reviewId": 1,
                                        "counselorName": "혜림 상담사",
                                        "score": 5,
                                        "comment": "친절하고 정확한 상담!",
                                        "createdAt": "2025-10-26T14:00:00"
                                      }
                                    ]
                                    """))),
    })
    @GetMapping
    public ResponseEntity<?> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }
    // 특정 상담사 후기 조회
    @Operation(
            summary = "상담사 후기 조회",
            description = """
                    상담사 ID를 기준으로 후기 목록을 조회합니다.  
                    페이지네이션(page, size) 지원.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "content": [
                                        {
                                          "reviewId": 201,
                                          "userName": "김민수",
                                          "score": 5,
                                          "comment": "정확하고 공감되는 상담이었어요.",
                                          "createdAt": "2025-10-25T09:00:00"
                                        }
                                      ],
                                      "page": 0,
                                      "size": 10,
                                      "totalElements": 5,
                                      "totalPages": 1
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "상담사를 찾을 수 없음", content = @Content)
    })
    @GetMapping("/counselor/{counselorId}")
    public ResponseEntity<?> getReviewsByCounselor(
            @Parameter(description = "상담사 ID", example = "2") @PathVariable Long counselorId,
            @Parameter(description = "페이지 번호", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지당 후기 수", example = "10") @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(reviewService.getReviewsByCounselor(counselorId, page, size));
    }

    // 후기 삭제(관리자)
    @Operation(
            summary = "후기 삭제 (ADMIN 전용)",
            description = "관리자가 부적절한 후기를 삭제(비활성화)합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "\"후기가 삭제되었습니다.\""))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "후기 ID를 찾을 수 없음", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(
            @Parameter(description = "삭제할 후기 ID", example = "101") @PathVariable Long id
    ) {
        String role = SecurityUtil.currentRole();
        if (!"ADMIN".equals(role)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        reviewService.deactivateReview(id);
        return ResponseEntity.ok("후기가 삭제되었습니다.");
    }
}
