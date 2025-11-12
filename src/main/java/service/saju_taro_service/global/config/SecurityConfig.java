package service.saju_taro_service.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import service.saju_taro_service.global.jwt.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ========== 공개 경로 (인증 불필요) ==========
                        // Swagger
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/swagger-ui.html").permitAll()
                        
                        // 인증 관련
                        .requestMatchers("/api/auth/**").permitAll()
                        
                        // 회원가입
                        .requestMatchers("/api/users/signup").permitAll()
                        
                        // 상담사 목록 조회 (비로그인도 가능)
                        .requestMatchers("/api/counselors/**", "/api/counselors/{id}").permitAll()
                        
                        // 프로필 조회 (비로그인도 가능)
                        .requestMatchers("/api/counselors/profile/{counselorId}", "/api/counselors/profile/search").permitAll()
                        
                        // ========== 사용자 전용 경로 ==========
                        .requestMatchers("/api/reservations/my").hasRole("USER")
                        .requestMatchers("/api/favorites/**").hasRole("USER")
                        
                        // ========== 상담사 전용 경로 ==========
                        .requestMatchers("/api/reservations/counselor/**").hasRole("COUNSELOR")
                        .requestMatchers("/api/counselors/profile").hasRole("COUNSELOR")
                        .requestMatchers("/api/counselors/calendar/**").hasRole("COUNSELOR")
                        .requestMatchers("/api/counselors/dashboard/**").hasRole("COUNSELOR")
                        .requestMatchers("/api/schedules/**").hasRole("COUNSELOR")
                        
                        // ========== 상담사 또는 관리자 경로 ==========
                        .requestMatchers("/api/reservations/*/status").hasAnyRole("COUNSELOR", "ADMIN")
                        
                        // ========== 관리자 전용 경로 ==========
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/roles/requests", "/api/roles/approve/**", "/api/roles/reject/**").hasRole("ADMIN")
                        
                        // ========== 인증 필요 경로 (역할 무관) ==========
                        .requestMatchers("/api/notifications/**").authenticated()
                        .requestMatchers("/api/reservations/**").authenticated()
                        .requestMatchers("/api/payments/**").authenticated()
                        .requestMatchers("/api/reviews/**").authenticated()
                        .requestMatchers("/api/users/**").authenticated()
                        .requestMatchers("/api/roles/request").authenticated()
                        
                        // 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
