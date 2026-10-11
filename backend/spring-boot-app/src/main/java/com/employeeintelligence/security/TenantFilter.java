package com.employeeintelligence.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

/** Runs after bearer-token authentication and before controller/method authorization. */
public class TenantFilter extends OncePerRequestFilter {
    private final TenantContext tenants;
    public TenantFilter(TenantContext tenants) { this.tenants = tenants; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof Jwt) {
            try { tenants.currentTenantId(); }
            catch (TenantRequiredException denied) { response.sendError(403, "A valid tenant claim is required"); return; }
        }
        chain.doFilter(request, response);
    }
}
