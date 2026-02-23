package service.saju_taro_service.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import service.saju_taro_service.service.admin.AdminDashBoardService;
import service.saju_taro_service.service.payment.PaymentService;

@Tag(name = "Admin API", description = "관리자 전용 기능 (결제, 승인, 통계 등)")
@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {
        private final PaymentService paymentService;
        private final AdminDashBoardService dashBoardService;

        /** ✅ 전체 결제내역 조회 */
        @Operation(summary = "전체 결제 내역 조회", description = """
                        관리자 전용 API로, 모든 결제 내역을 조회합니다.
                        상태(status) 파라미터로 필터링 가능 (예: PAID, REFUND, PENDING).
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(example = """
                                        [
                                          {
                                            "paymentId": 1,
                                            "orderId": "ORD-20251026-00001",
                                            "userName": "김민수",
                                            "amount": 50000,
                                            "status": "PAID",
                                            "createdAt": "2025-10-26T14:00:00"
                                          },
                                          {
                                            "paymentId": 2,
                                            "orderId": "ORD-20251020-00003",
                                            "userName": "이혜림",
                                            "amount": 30000,
                                            "status": "REFUND",
                                            "createdAt": "2025-10-20T09:30:00"
                                          }
                                        ]
                                        """))),
                        @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content)
        })
        @GetMapping
        public ResponseEntity<?> getAllPayments(
                        @Parameter(description = "결제 상태 필터 (PAID, REFUND, PENDING)", example = "PAID") @RequestParam(required = false) String status) {
                return ResponseEntity.ok(paymentService.getAllPayments(status));
        }

        /** ✅ 월별 매출 통계 */
        @Operation(summary = "월별 매출 통계 조회", description = """
                        특정 연도의 월별 매출 통계를 조회합니다.
                        매출 합계, 결제 건수, 환불 건수 등의 통계 데이터를 반환합니다.
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(example = """
                                        {
                                          "year": 2025,
                                          "monthlyStats": [
                                            { "month": 1, "revenue": 5200000, "transactions": 103 },
                                            { "month": 2, "revenue": 4800000, "transactions": 95 },
                                            { "month": 3, "revenue": 5600000, "transactions": 110 }
                                          ],
                                          "totalRevenue": 15600000
                                        }
                                        """))),
                        @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content)
        })
        @GetMapping("/stats/monthly")
        public ResponseEntity<?> getMonthlyStats(
                        @Parameter(description = "조회할 연도", example = "2025") @RequestParam int year) {
                return ResponseEntity.ok(dashBoardService.getMonthlyRevenueStats(year));
        }

        /** ✅ 일별 예약/평점/매출 통계 */
        @Operation(summary = "일별 통계 조회 (예약 / 평점 / 매출)", description = """
                        특정 기간(startDate ~ endDate)에 대한 일별 통계를 조회합니다.
                        예약 건수, 평균 평점, 일별 매출액을 포함한 관리자 대시보드용 데이터입니다.
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(example = """
                                        {
                                          "startDate": "2025-10-01",
                                          "endDate": "2025-10-26",
                                          "dailyStats": [
                                            { "date": "2025-10-01", "reservations": 12, "averageRating": 4.8, "revenue": 450000 },
                                            { "date": "2025-10-02", "reservations": 8,  "averageRating": 4.6, "revenue": 360000 },
                                            { "date": "2025-10-03", "reservations": 15, "averageRating": 4.9, "revenue": 560000 }
                                          ]
                                        }
                                        """))),
                        @ApiResponse(responseCode = "400", description = "유효하지 않은 날짜 형식", content = @Content),
                        @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content)
        })
        @GetMapping("/stats/daily")
        public ResponseEntity<?> getDailyStats(
                        @Parameter(description = "조회 시작일 (YYYY-MM-DD)", example = "2025-10-01") @RequestParam String startDate,
                        @Parameter(description = "조회 종료일 (YYYY-MM-DD)", example = "2025-10-26") @RequestParam String endDate) {
                return ResponseEntity.ok(
                                dashBoardService.getDashboardStats(
                                                java.time.LocalDate.parse(startDate),
                                                java.time.LocalDate.parse(endDate)));
        }

}
