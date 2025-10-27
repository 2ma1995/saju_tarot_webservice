package service.saju_taro_service.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@OpenAPIDefinition(
        servers = {
                @Server(url = "http://localhost:8080", description = "로컬 서버")
        }
)
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI sajuTarotOpenAPI() {
        return new OpenAPI().addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .info(new Info()
                        .title("🔮 사주·타로 예약 서비스 API 문서")
                        .description("JWT 인증 기반 REST API Swagger 문서")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("소지원")
                                .email("vthquddbsv@gmail.com")
                                .url("https://github.com/sojiwon-dev"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .tags(List.of(
                        new Tag().name("Admin API").description("관리자 전용 기능(결제, 승인, 통계 등)"),
                        new Tag().name("Counselor API").description("상담사 전용 기능(예약, 스케줄, 리뷰관리 등)"),
                        new Tag().name("User API").description("사용자 전용 기능(회원가입, 예약, 결제 등)")
                ));
    }
}