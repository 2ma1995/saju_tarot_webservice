package service.saju_taro_service.controller.notification;

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
import service.saju_taro_service.service.notification.NotificationService;

import java.util.Map;
@Tag(name = "Notification API", description = "알림 목록 조회, 읽음 처리, 전체 읽음 및 미확인 알림 개수 조회 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    // 내 알림 목록(최신순) - 기본 20건
    @Operation(
            summary = "내 알림 목록 조회",
            description = """
                    로그인한 사용자의 알림 목록을 최신순으로 조회합니다.  
                    기본 20건이며, 페이지네이션(page, size)으로 추가 조회가 가능합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "content": [
                                        {
                                          "id": 101,
                                          "message": "예약이 확정되었습니다.",
                                          "type": "RESERVATION",
                                          "isRead": false,
                                          "createdAt": "2025-10-26T14:00:00"
                                        },
                                        {
                                          "id": 102,
                                          "message": "후기 작성 요청 알림",
                                          "type": "REVIEW",
                                          "isRead": true,
                                          "createdAt": "2025-10-25T09:30:00"
                                        }
                                      ],
                                      "page": 0,
                                      "size": 20,
                                      "totalElements": 48,
                                      "totalPages": 3
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "로그인이 필요함", content = @Content)
    })
    @GetMapping("/my")
    public ResponseEntity<?> getMy(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지당 알림 개수", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = SecurityUtil.currentUserId();
        return ResponseEntity.ok(notificationService.getMy(userId, page, size));
    }

    // 읽음 처리 (단건)
    @Operation(
            summary = "단일 알림 읽음 처리",
            description = "특정 알림 ID를 읽음 상태로 변경합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "읽음 처리 완료 (본문 없음)"),
            @ApiResponse(responseCode = "404", description = "알림 ID를 찾을 수 없음", content = @Content)
    })
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(
            @Parameter(description = "읽음 처리할 알림 ID", example = "101")
            @PathVariable Long id) {
        Long userId = SecurityUtil.currentUserId();
        notificationService.markRead(id, userId);
        return ResponseEntity.noContent().build();
    }

    // 모두 읽음 처리
    @Operation(
            summary = "전체 읽음 처리",
            description = """
                    로그인한 사용자의 모든 안읽은 알림을 읽음 상태로 변경합니다.  
                    변경된(읽음 처리된) 알림의 개수를 반환합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "전체 읽음 처리 완료",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "updated": 15
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "로그인이 필요함", content = @Content)
    })
    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllRead() {
        Long userId = SecurityUtil.currentUserId();
        int changed = notificationService.markAllRead(userId);
        return ResponseEntity.ok(Map.of("updated", changed));
    }

    // 안읽은 개수
    @Operation(
            summary = "안읽은 알림 개수 조회",
            description = "현재 로그인한 사용자의 읽지 않은 알림 개수를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "count": 3
                                    }
                                    """))),
            @ApiResponse(responseCode = "401", description = "로그인이 필요함", content = @Content)
    })
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount() {
        Long userId = SecurityUtil.currentUserId();
        return ResponseEntity.ok(Map.of("count", notificationService.unreadCount(userId)));
    }

}
