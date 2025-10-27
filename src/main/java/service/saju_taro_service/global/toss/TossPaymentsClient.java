package service.saju_taro_service.global.toss;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import service.saju_taro_service.global.config.TossConfig;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentsClient {

    private final TossConfig tossConfig;

    /**
     * ✅ 결제 승인 요청
     */

    public Map<String, Object> confirmPayment(String paymentKey, String orderId, int amount) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(tossConfig.getSecretKey(), ""); // Basic Auth 자동 처리
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );
        ResponseEntity<Map> response = restTemplate.exchange(
                tossConfig.getConfirmUrl(),
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("결제 승인 실패: " + response.getStatusCode());
        }

        return response.getBody();
    }


    /** ✅ 환불 API */
    public void cancelPayment(String paymentKey, String reason) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(tossConfig.getSecretKey(), "");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = Map.of("cancelReason", reason);

        ResponseEntity<Map> response = restTemplate.exchange(
                tossConfig.getCancelUrl(paymentKey),
                HttpMethod.POST,
                new HttpEntity<>(payload, headers),
                Map.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("환불 실패: " + response.getStatusCode());
        }
    }
}