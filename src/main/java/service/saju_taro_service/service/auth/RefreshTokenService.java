package service.saju_taro_service.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final long REFRESH_TOKEN_EXPIRY = 7L; // 7일

    //저장
    public void saveRefreshToken(Long userId,String token) {
        String key = "refresh_token:" + userId;
        redisTemplate.opsForValue().set(key, token, REFRESH_TOKEN_EXPIRY, TimeUnit.DAYS);
    }

    // 조회
    public String getRefreshToken(Long userId) {
        return (String) redisTemplate.opsForValue().get("refresh:" + userId);
    }

    // 삭제
    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete("refresh:" + userId);
    }

    //검증
    public boolean validateRefreshToken(Long userId, String token) {
        String saved = getRefreshToken(userId);
        return saved != null && saved.equals(token);
    }
}
