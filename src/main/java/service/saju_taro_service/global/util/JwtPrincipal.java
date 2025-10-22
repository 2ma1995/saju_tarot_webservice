package service.saju_taro_service.global.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtPrincipal {
    private final Long userId;
    private final String role;
}
