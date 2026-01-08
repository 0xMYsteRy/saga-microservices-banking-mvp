package com.mystery.sagaorchestrator.util;

import com.mystery.common.entity.User;
import com.mystery.common.util.SecurityUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility helpers related to security and user extraction.
 */
public final class SecurityUserUtil {

    private SecurityUserUtil() {
        // utility
    }

    public static User buildUserFromSecurityContext() {
        User user = new User();
        user.setUsername(SecurityUtil.getCurrentUsername());
        user.setRoles(SecurityUtil.extractRolesFromJwt());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            user.setEmail(jwt.getClaimAsString("email"));
            user.setFullName(jwt.getClaimAsString("name"));
        }

        return user;
    }
}

