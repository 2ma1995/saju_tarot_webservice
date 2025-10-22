package service.saju_taro_service.global.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {
    private SecurityUtil() {}

    public static Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof JwtPrincipal jwtPrincipal){
            return jwtPrincipal.getUserId();
        }
        if (principal instanceof org.springframework.security.core.userdetails.User u) {
            return Long.valueOf(u.getUsername());
        }
        return null;
    }

    public static String currentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {return null;}

        Object principal = auth.getPrincipal();
        if (principal instanceof JwtPrincipal jwtPrincipal){
            return jwtPrincipal.getRole();
        }

        return null;
    }
}
