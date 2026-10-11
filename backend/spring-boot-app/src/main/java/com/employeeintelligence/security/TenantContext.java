package com.employeeintelligence.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/** Resolves the tenant from the authenticated token; request parameters are never consulted. */
public class TenantContext {
    public static final String DEMO_TENANT = "demo";
    private final boolean securityEnabled;

    public TenantContext(@Value("${security.auth.enabled:false}") boolean securityEnabled) {
        this.securityEnabled = securityEnabled;
    }

    public String currentTenantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof Jwt jwt) {
            for (String claim : new String[] {"tenant_id", "organization", "tenant"}) {
                Object value = jwt.getClaims().get(claim);
                if (value instanceof String id && !id.isBlank() && id.trim().length() <= 255) return id.trim();
                if (value != null && !(value instanceof String s && s.isBlank())) {
                    throw new TenantRequiredException("Invalid tenant claim");
                }
            }
        }
        if (!securityEnabled) return DEMO_TENANT;
        throw new TenantRequiredException("Authenticated token does not contain a tenant claim");
    }

    public boolean isSecurityEnabled() { return securityEnabled; }
}
