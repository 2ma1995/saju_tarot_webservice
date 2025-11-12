package service.saju_taro_service.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final Long accessTokenExpiration;
    private final Long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiry:3600000}") Long accessTokenExpiry,
            @Value("${jwt.refresh-token-expiry:604800000}") Long refreshTokenExpiry
    ) {
        // secret 길이 검증
        if (secret.length() < 32) {
            log.warn("⚠️ JWT Secret Key가 너무 짧습니다. 보안을 위해 최소 32자 이상 사용하세요.");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpiration = accessTokenExpiry;
        this.refreshTokenExpiration = refreshTokenExpiry;
    }

    /**
     * Access Token 생성 (userId, role 포함)
     */
    public String createToken(Long userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Refresh Token 생성 (정보 없이 순수 토큰만)
     */
    public String createRefreshToken() {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰에서 사용자 ID 추출
     */
    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        Object userIdObj = claims.get("userId");
        
        // Integer로 저장될 수 있으므로 처리
        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        }
        return (Long) userIdObj;
    }

    /**
     * 토큰에서 ROLE 추출
     */
    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * 토큰 유효성 검사
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException e) {
            log.debug("JWT 검증 실패: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.debug("JWT가 비어있거나 잘못된 형식: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰 파싱
     */
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
