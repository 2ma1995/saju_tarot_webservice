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
                        // swagger 접근 허용
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**","/swagger-resources/**","/swagger-ui.html").permitAll()
                        // 인증/회원가입은 비로그인 접근 허용
                        .requestMatchers("/api/auth/**", "/api/users/signup", "/api/users/login").permitAll()
                        // 권한별 접근 제어
                        .requestMatchers("/api/reservations/my").hasRole("USER")
                        .requestMatchers("/api/reservations/counselor").hasRole("COUNSELOR")
                        .requestMatchers("/api/reservations/*/status").hasAnyRole("COUNSELOR", "ADMIN")
                        .requestMatchers("/api/notifications/**").authenticated()
                        // 나머지 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
