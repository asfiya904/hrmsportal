package com.hireconnect.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.hireconnect.security.CustomUserDetails;

public final class SecurityUtil {

    // Prevent object creation
    private SecurityUtil() {}

    /**
     * Returns the employeeId of the currently logged-in user.
     * Uses Spring SecurityContext populated via HttpOnly cookie + JWT.
     */
    public static Long getLoggedInEmployeeId() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails)) {
            throw new IllegalStateException("Invalid authentication principal");
        }

        return ((CustomUserDetails) principal).getEmployeeId();
    }
}
