package service.saju_taro_service.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "toss")
public class TossConfig {
    private String clientKey;
    private String secretKey;
    private String baseUrl;
    private String confirmEndpoint;
    private String cancelEndpoint;

    // 성공/실패 URL은 프론트엔드 URL 또는 백엔드 API URL
    public String getSuccessUrl() {
        return "http://localhost:8080/api/payments/success";
    }

    public String getFailUrl() {
        return "http://localhost:8080/api/payments/fail";
    }

    public String getConfirmUrl() {
        return baseUrl + confirmEndpoint;
    }

    public String getCancelUrl(String paymentKey) {
        return baseUrl + String.format(cancelEndpoint, paymentKey);
    }
}
