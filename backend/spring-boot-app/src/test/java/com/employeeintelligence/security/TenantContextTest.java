package com.employeeintelligence.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import java.time.Instant;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class TenantContextTest {
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    @Test void prefersTenantIdThenOrganizationThenTenant() {
        SecurityContextHolder.getContext().setAuthentication(auth(Map.of("tenant_id", "acme", "organization", "other")));
        assertEquals("acme", new TenantContext(true).currentTenantId());
        SecurityContextHolder.getContext().setAuthentication(auth(Map.of("organization", "org")));
        assertEquals("org", new TenantContext(true).currentTenantId());
    }
    @Test void demoModeUsesSafeTenant() { assertEquals("demo", new TenantContext(false).currentTenantId()); }
    @Test void secureModeRejectsMissingTenant() {
        SecurityContextHolder.getContext().setAuthentication(auth(Map.of()));
        assertThrows(TenantRequiredException.class, () -> new TenantContext(true).currentTenantId());
    }
    private static UsernamePasswordAuthenticationToken auth(Map<String,Object> claims) {
        var payload = new java.util.HashMap<String,Object>(claims); payload.putIfAbsent("sub", "user");
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "none"), payload);
        return new UsernamePasswordAuthenticationToken(jwt, "token", java.util.List.of());
    }
}
