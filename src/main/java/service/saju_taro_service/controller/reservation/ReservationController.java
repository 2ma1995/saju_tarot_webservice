package service.saju_taro_service.controller.reservation;

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
import service.saju_taro_service.domain.reservation.ReservationStatus;
import service.saju_taro_service.dto.reservation.ReservationRequest;
import service.saju_taro_service.global.exception.CustomException;
import service.saju_taro_service.global.exception.ErrorCode;
import service.saju_taro_service.global.util.SecurityUtil;
import service.saju_taro_service.service.reservation.ReservationService;

import java.util.Map;

@Tag(name = "Reservation API", description = "상담 예약 생성, 조회, 상태변경, 취소 기능 (사용자/상담사 공용)")
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // 예약 생성
    @Operation(
            summary = "예약 생성",
            description = """
                    사용자가 상담 일정을 선택해 예약을 생성합니다.  
                    예약 생성 시 자동으로 상담사에게 알림이 발송됩니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "예약 생성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "reservationId": 12,
                                      "userId": 5,
                                      "counselorId": 2,
                                      "date": "2025-10-28",
                                      "startTime": "15:00",
                                      "endTime": "16:00",
                                      "status": "CONFIRMED"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
            @ApiResponse(responseCode = "401", description = "로그인이 필요함", content = @Content)
    })
    @PostMapping
    public ResponseEntity<?> createReservation(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "예약 요청 DTO (상담사, 날짜, 시간 포함)",
                    required = true,
                    content = @Content(schema = @Schema(example = """
                {
                  "counselorId": 5,
                  "date": "2025-10-30",
                  "startTime": "14:00",
                  "endTime": "15:00",
                  "paymentMethod": "CARD"
                }
                """))
            )
            @RequestBody ReservationRequest req) {
        Long userId = SecurityUtil.currentUserId();
        return ResponseEntity.ok(reservationService.createReservation(userId, req));
    }

    // 내 예약 목록 조회
    @GetMapping("/my")
    @Operation(
            summary = "내 예약 목록 조회",
            description = """
                    사용자가 자신의 예약 내역을 조회합니다.  
                    예약 상태(status)에 따라 필터링할 수 있습니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    [
                                      {
                                        "reservationId": 12,
                                        "counselorName": "혜림 상담사",
                                        "date": "2025-10-28",
                                        "time": "15:00 ~ 16:00",
                                        "status": "CONFIRMED"
                                      },
                                      {
                                        "reservationId": 13,
                                        "counselorName": "지원 상담사",
                                        "date": "2025-10-30",
                                        "time": "11:00 ~ 12:00",
                                        "status": "CANCELLED"
                                      }
                                    ]
                                    """))),
            @ApiResponse(responseCode = "403", description = "사용자만 접근 가능", content = @Content)
    })
    public ResponseEntity<?> getMyReservations(
            @Parameter(description = "필터링할 상태 (예: CONFIRMED, COMPLETED, CANCELLED)", example = "CONFIRMED")
            @RequestParam(required = false) String status) {
        String role = SecurityUtil.currentRole();
        if (!"USER".equals(role)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "사용자만 접근 가능합니다.");
        }

        Long userId = SecurityUtil.currentUserId();
        return ResponseEntity.ok(reservationService.getUserReservations(userId, status));
    }

    /** ✅ 상담사 – 특정 날짜 예약목록 조회 (달력 클릭 시 호출) */
    @Operation(
            summary = "상담사 예약 목록 조회 (특정 날짜)",
            description = """
                    상담사가 특정 날짜(date)에 등록된 예약 목록을 조회합니다.  
                    달력 클릭 시 해당 날짜의 예약 리스트를 반환합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    [
                                      {
                                        "reservationId": 10,
                                        "userName": "김민수",
                                        "date": "2025-10-28",
                                        "startTime": "14:00",
                                        "endTime": "15:00",
                                        "status": "CONFIRMED"
                                      }
                                    ]
                                    """))),
            @ApiResponse(responseCode = "403", description = "상담사만 접근 가능", content = @Content)
    })
    @GetMapping("/counselor/day")
    public ResponseEntity<?> getCounselorReservationsByDay(
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)", example = "2025-10-28")
            @RequestParam String date) {
        String role = SecurityUtil.currentRole();
        if (!"COUNSELOR".equals(role)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "상담사만 접근 가능합니다.");
        }

        Long counselorId = SecurityUtil.currentUserId();
        return ResponseEntity.ok(reservationService.getCounselorReservationsByDay(counselorId, date));
    }

    /**
     * ✅ 상담사 전용 - 예약 상태 업데이트
     * 예시: PUT /api/reservations/12/status
     * Body: { "status": "COMPLETED" }
     */
    @Operation(
            summary = "예약 상태 업데이트 (상담사 전용)",
            description = """
                    상담사가 특정 예약의 상태를 변경합니다.  
                    가능한 상태: CONFIRMED, COMPLETED, CANCELLED 등.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상태 변경 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "message": "예약 상태가 COMPLETED 으로 변경되었습니다."
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 상태값", content = @Content),
            @ApiResponse(responseCode = "403", description = "상담사만 상태 변경 가능", content = @Content)
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateReservationStatus(
            @Parameter(description = "예약 ID", example = "12") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "변경할 예약 상태 (예: { \"status\": \"COMPLETED\" })",
                    required = true,
                    content = @Content(schema = @Schema(example = "{\"status\": \"COMPLETED\"}"))
            )
            @RequestBody Map<String, String> body
    ) {
        String role = SecurityUtil.currentRole();
        if (!"COUNSELOR".equals(role)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED, "상담사만 상태를 변경할 수 있습니다.");
        }

        String statusStr = body.get("status");
        if (statusStr == null || statusStr.isBlank()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "status 값이 필요합니다.");
        }

        ReservationStatus newStatus;
        try {
            newStatus = ReservationStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "유효하지 않은 상태값입니다.");
        }

        reservationService.updateStatus(id, newStatus);
        return ResponseEntity.ok(Map.of(
                "message", "예약 상태가 " + newStatus.name() + " 으로 변경되었습니다."
        ));
    }


    // 예약 취소
    @Operation(
            summary = "예약 취소",
            description = """
                    사용자가 자신의 예약을 취소합니다.  
                    예약 상태가 CANCELLED로 변경됩니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "예약 취소 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "\"예약이 취소되었습니다.\""))),
            @ApiResponse(responseCode = "404", description = "예약 ID를 찾을 수 없음", content = @Content)
    })
    @PutMapping("/{reservationId}/cancel")
    public ResponseEntity<?> cancel(
            @Parameter(description = "취소할 예약 ID", example = "15")
            @PathVariable Long reservationId) {
        reservationService.cancelReservation(reservationId);
        return ResponseEntity.ok("예약이 취소되었습니다.");
    }

    // 관리자/상담사 - 예약 상태 직접 변경
    @Operation(
            summary = "예약 상태 변경 (관리자/상담사 공용)",
            description = """
                    관리자나 상담사가 예약 상태를 직접 변경합니다.  
                    상태값은 QueryParam으로 전달됩니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상태 변경 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "\"예약 상태가 변경되었습니다.\""))),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
    })
    @PutMapping("/{reservationId}/status")
    public ResponseEntity<?> updateStatus(@Parameter(description = "예약 ID", example = "15") @PathVariable Long reservationId,
                                          @Parameter(description = "변경할 상태 (예: CONFIRMED, CANCELLED, COMPLETED)", example = "CONFIRMED")
                                          @RequestParam ReservationStatus status) {
        String role = SecurityUtil.currentRole();
        if (!"COUNSELOR".equals(role) && !"ADMIN".equals(role)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        reservationService.updateStatus(reservationId, status);
        return ResponseEntity.ok("예약 상태가 변경되었습니다.");
    }
}
