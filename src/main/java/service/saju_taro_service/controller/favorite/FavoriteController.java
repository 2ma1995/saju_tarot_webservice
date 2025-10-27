package service.saju_taro_service.controller.favorite;

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
import service.saju_taro_service.global.util.SecurityUtil;
import service.saju_taro_service.service.favorite.FavoriteService;

@Tag(name = "Favorite API", description = "상담사 즐겨찾기 추가 / 해제 / 내 즐겨찾기 목록 조회 기능")
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    private final FavoriteService favoriteService;

    /** ✅ 즐겨찾기 추가 */
    @Operation(
            summary = "즐겨찾기 추가",
            description = """
                    사용자가 특정 상담사를 즐겨찾기에 추가합니다.  
                    동일 상담사를 중복으로 추가할 수 없습니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "즐겨찾기 추가 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "\"즐겨찾기에 추가되었습니다.\""))),
            @ApiResponse(responseCode = "400", description = "이미 즐겨찾기에 존재함", content = @Content),
            @ApiResponse(responseCode = "401", description = "로그인이 필요함", content = @Content)
    })
    @PostMapping("/{counselorId}")
    public ResponseEntity<?> addFavorite(
            @Parameter(description = "즐겨찾기에 추가할 상담사 ID", example = "3")
            @PathVariable Long counselorId) {
        Long userId = SecurityUtil.currentUserId();
        favoriteService.addFavorite(userId, counselorId);
        return ResponseEntity.ok("즐겨찾기에 추가되었습니다.");
    }

    /** ✅ 즐겨찾기 해제 */
    @Operation(
            summary = "즐겨찾기 해제",
            description = """
                    사용자가 기존에 즐겨찾기한 상담사를 삭제합니다.  
                    존재하지 않는 경우 오류가 발생하지 않습니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "즐겨찾기 삭제 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "\"즐겨찾기에서 삭제되었습니다.\""))),
            @ApiResponse(responseCode = "401", description = "로그인이 필요함", content = @Content)
    })
    @DeleteMapping("/{counselorId}")
    public ResponseEntity<?> removeFavorite(
            @Parameter(description = "즐겨찾기 해제할 상담사 ID", example = "3")
            @PathVariable Long counselorId) {
        Long userId = SecurityUtil.currentUserId();
        favoriteService.removeFavorite(userId, counselorId);
        return ResponseEntity.ok("즐겨찾기에서 삭제되었습니다.");
    }

    /** ✅ 내 즐겨찾기 목록 */
    @Operation(
            summary = "내 즐겨찾기 목록 조회",
            description = """
                    현재 로그인한 사용자가 즐겨찾기한 상담사 목록을 조회합니다.  
                    상담사 프로필 요약 정보(이름, 평점, 태그 등)를 함께 반환합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    [
                                      {
                                        "counselorId": 1,
                                        "name": "너는 상담가",
                                        "bio": "타로/사주 전문 상담가",
                                        "rating": 4.9,
                                        "tags": ["연애", "진로"]
                                      },
                                      {
                                        "counselorId": 2,
                                        "name": "나는 상담가",
                                        "bio": "심리 타로 전문가",
                                        "rating": 4.8,
                                        "tags": ["심리", "타로"]
                                      }
                                    ]
                                    """))),
            @ApiResponse(responseCode = "401", description = "로그인이 필요함", content = @Content)
    })
    @GetMapping
    public ResponseEntity<?> getMyFavorites() {
        Long userId = SecurityUtil.currentUserId();
        return ResponseEntity.ok(favoriteService.getMyFavorites(userId));
    }
}
