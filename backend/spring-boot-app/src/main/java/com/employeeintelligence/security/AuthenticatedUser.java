package com.employeeintelligence.security;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Small application-facing seam over Spring Security/OIDC claims. */
public record AuthenticatedUser(String subject, Set<String> roles) {
    public static AuthenticatedUser current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new AuthenticatedUser("anonymous", Set.of());
        }
        return new AuthenticatedUser(authentication.getName(), authentication.getAuthorities().stream()
                .map(granted -> granted.getAuthority().replaceFirst("^ROLE_", ""))
                .collect(Collectors.toUnmodifiableSet()));
    }
}
