package service.saju_taro_service.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import service.saju_taro_service.global.exception.CustomException;
import service.saju_taro_service.global.exception.ErrorCode;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final long REFRESH_TOKEN_EXPIRY = 7L; // 7일

    // Redis Key 생성 헬퍼 메서드 저장
    private String getUserTokenKey(Long userId) {
        return "refresh_token:" + userId;
    }

    private String getTokenToUserKey(String token) {
        return "token_to_user:" + token;
    }

    /**
     * RefreshToken 저장 (userId -> token, token -> userId 양방향 저장)
     */
    public void saveRefreshToken(Long userId, String token) {
        // 1. userId -> token 매핑
        redisTemplate.opsForValue().set(
                getUserTokenKey(userId), 
                token, 
                REFRESH_TOKEN_EXPIRY, 
                TimeUnit.DAYS
        );
        
        // 2. token -> userId 역매핑 (토큰으로 userId 조회 가능하도록)
        redisTemplate.opsForValue().set(
                getTokenToUserKey(token), 
                userId.toString(), 
                REFRESH_TOKEN_EXPIRY, 
                TimeUnit.DAYS
        );
    }

    /**
     * userId로 RefreshToken 조회
     */
    public String getRefreshToken(Long userId) {
        return (String) redisTemplate.opsForValue().get(getUserTokenKey(userId));
    }

    /**
     * RefreshToken 삭제 (로그아웃 시)
     */
    public void deleteRefreshToken(Long userId) {
        String token = getRefreshToken(userId);
        
        // 1. userId -> token 삭제
        redisTemplate.delete(getUserTokenKey(userId));
        
        // 2. token -> userId 역매핑도 삭제
        if (token != null) {
            redisTemplate.delete(getTokenToUserKey(token));
        }
    }

    /**
     * RefreshToken 검증 (userId와 token이 일치하는지)
     */
    public boolean validateRefreshToken(Long userId, String token) {
        String saved = getRefreshToken(userId);
        return saved != null && saved.equals(token);
    }

    /**
     * RefreshToken으로 userId 추출 및 검증
     * @param token RefreshToken 문자열
     * @return userId
     * @throws CustomException 유효하지 않은 토큰일 경우
     */
    public Long validateAndGetUserId(String token) {
        String userIdStr = (String) redisTemplate.opsForValue().get(getTokenToUserKey(token));
        
        if (userIdStr == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "유효하지 않거나 만료된 Refresh Token입니다.");
        }
        
        return Long.parseLong(userIdStr);
    }
}
