package service.saju_taro_service.controller.payment;

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
import service.saju_taro_service.domain.payment.Payment;
import service.saju_taro_service.global.config.TossConfig;
import service.saju_taro_service.global.util.SecurityUtil;
import service.saju_taro_service.service.payment.PaymentService;

import java.util.Map;
@Tag(name = "Payment API", description = "Toss 결제 요청, 승인, 실패 처리, 환불 및 결제 내역 조회 기능")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final TossConfig tossConfig;

    /** ✅ 결제 요청 생성 */
    @Operation(
            summary = "결제 요청 생성",
            description = """
                    사용자가 상담 예약 건에 대해 결제를 요청합니다.  
                    Toss Payments SDK에 전달할 결제 정보(orderId, amount, successUrl 등)를 반환합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결제 요청 생성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "orderId": "ORD-20251026-00001",
                                      "amount": 50000,
                                      "orderName": "사주타로 상담",
                                      "clientKey": "test_ck_D5...",
                                      "successUrl": "http://localhost:8080/api/payments/success",
                                      "failUrl": "http://localhost:8080/api/payments/fail"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "요청 데이터 오류", content = @Content)
    })
    @PostMapping("/request")
    public ResponseEntity<?> requestPayment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "결제 요청 데이터 (reservationId, amount, orderName)",
                    required = true,
                    content = @Content(schema = @Schema(example = """
                            {
                              "reservationId": 12,
                              "amount": 50000,
                              "orderName": "사주타로 상담"
                            }
                            """))
            )
            @RequestBody Map<String, Object> payload) {
        Long reservationId = Long.valueOf(payload.get("reservationId").toString());
        int amount = Integer.parseInt(payload.get("amount").toString());
        String orderName = payload.getOrDefault("orderName", "사주타로 상담").toString();
        Payment payment = paymentService.createPayment(reservationId, amount);

        return ResponseEntity.ok(Map.of(
                "orderId", payment.getTransactionId(),
                "amount", payment.getAmount(),
                "orderName", orderName,
                "clientKey", tossConfig.getClientKey(),
                "successUrl", tossConfig.getSuccessUrl(),
                "failUrl", tossConfig.getFailUrl()
        ));
    }

    /** ✅ 결제 완료 처리 */
    @Operation(
            summary = "결제 성공 처리",
            description = """
                    Toss Payments에서 결제 성공 시 호출되는 콜백입니다.  
                    결제키(paymentKey), 주문번호(orderId), 결제금액(amount)을 검증 후 결제 완료 상태로 업데이트합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결제 성공 처리 완료",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "message": "결제가 성공적으로 완료되었습니다.",
                                      "orderId": "ORD-20251026-00001",
                                      "paymentKey": "pay_1234567890abcdef"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "결제 검증 실패 또는 금액 불일치", content = @Content)
    })
    @PostMapping("/success")
    public ResponseEntity<?> tossPaymentSuccess(
            @Parameter(description = "Toss 결제 고유키", example = "pay_1234567890abcdef") @RequestParam String paymentKey,
            @Parameter(description = "주문번호 (orderId)", example = "ORD-20251026-00001") @RequestParam String orderId,
            @Parameter(description = "결제 금액", example = "50000") @RequestParam int amount
    ) {

        try {
            paymentService.confirmTossPayment(paymentKey, orderId, amount);
            return ResponseEntity.ok(Map.of(
                    "message", "결제가 성공적으로 완료되었습니다.",
                    "orderId", orderId,
                    "paymentKey", paymentKey
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ✅ Toss Payments 결제 실패 시 리다이렉트
     */
    @Operation(
            summary = "결제 실패 처리",
            description = """
                    Toss 결제 중 사용자가 결제를 취소하거나 오류가 발생한 경우  
                    Toss에서 redirect되는 실패 콜백 엔드포인트입니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "결제 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    {
                                      "error": "결제 실패",
                                      "code": "USER_CANCEL",
                                      "message": "사용자가 결제를 취소했습니다.",
                                      "orderId": "ORD-20251026-00001"
                                    }
                                    """)))
    })
    @GetMapping("/fail")
    public ResponseEntity<?> tossPaymentFail(
            @Parameter(description = "에러 코드", example = "USER_CANCEL") @RequestParam String code,
            @Parameter(description = "에러 메시지", example = "사용자가 결제를 취소했습니다.") @RequestParam String message,
            @Parameter(description = "주문번호", example = "ORD-20251026-00001") @RequestParam String orderId
    ) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "결제 실패",
                "code", code,
                "message", message,
                "orderId", orderId
        ));
    }

    /**
     * ✅ 결제 환불 처리 (기존 방식)
     */
    @Operation(
            summary = "결제 환불 요청",
            description = """
                    사용자가 이미 결제한 금액을 환불 요청합니다.  
                    transactionId를 기반으로 Toss API를 통해 환불이 처리됩니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "환불 처리 완료",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "\"결제가 환불되었습니다.\""))),
            @ApiResponse(responseCode = "400", description = "환불 실패 또는 유효하지 않은 transactionId", content = @Content)
    })
    @PostMapping("/refund")
    public ResponseEntity<?> refundPayment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "환불 요청 데이터 (transactionId)",
                    required = true,
                    content = @Content(schema = @Schema(example = """
                            {
                              "transactionId": "ORD-20251026-00001"
                            }
                            """))
            )
            @RequestBody Map<String, String> payload) {
        String txId = payload.get("transactionId");
        paymentService.refundPayment(txId);
        return ResponseEntity.ok("결제가 환불되었습니다.");
    }

    /** ✅ 사용자 결제 내역 */
    @Operation(
            summary = "내 결제 내역 조회",
            description = """
                    로그인한 사용자의 모든 결제 내역을 조회합니다.  
                    결제 상태(PAID, REFUNDED 등)를 함께 반환합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = """
                                    [
                                      {
                                        "paymentId": 1,
                                        "orderId": "ORD-20251026-00001",
                                        "amount": 50000,
                                        "status": "PAID",
                                        "createdAt": "2025-10-26T15:00:00"
                                      },
                                      {
                                        "paymentId": 2,
                                        "orderId": "ORD-20251020-00009",
                                        "amount": 30000,
                                        "status": "REFUNDED",
                                        "createdAt": "2025-10-20T10:30:00"
                                      }
                                    ]
                                    """))),
            @ApiResponse(responseCode = "401", description = "로그인이 필요함", content = @Content)
    })
    @GetMapping("/my")
    public ResponseEntity<?> getMyPayments() {
        Long userId = SecurityUtil.currentUserId();
        return ResponseEntity.ok(paymentService.getMyPayments(userId));
    }

}
