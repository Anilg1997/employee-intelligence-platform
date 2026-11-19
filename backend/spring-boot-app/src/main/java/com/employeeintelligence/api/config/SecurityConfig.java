package com.employeeintelligence.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import com.employeeintelligence.api.service.AuditService;
import org.springframework.beans.factory.ObjectProvider;
import com.employeeintelligence.security.TenantContext;
import com.employeeintelligence.security.TenantFilter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;

@Configuration
@org.springframework.context.annotation.Import(com.employeeintelligence.security.SecurityMode.class)
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    public SecurityConfig(Environment environment) {
    }

    @Bean
    TenantContext tenantContext(Environment environment) {
        return new TenantContext(!environment.acceptsProfiles(Profiles.of("local")));
    }

    @Bean
    @Profile("local")
    SecurityFilterChain localSecurityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health/**", "/actuator/info", "/actuator/metrics", "/actuator/prometheus").permitAll()
                        .requestMatchers("/api/auth/**", "/api/invitations/**", "/api/employees/search").permitAll()
                        .anyRequest().permitAll())
                .formLogin(form -> form
                        .loginPage("/login").permitAll()
                        .defaultSuccessUrl("/api/dashboard/summary", true)
                        .passwordParameter("password").permitAll())
                .build();
    }

    @Bean
    @Profile("secure")
    SecurityFilterChain oidcSecurityFilterChain(
            HttpSecurity http,
            ObjectProvider<AuditService> audit,
            TenantContext tenants) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health/**", "/actuator/info", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .requestMatchers("/actuator/metrics/**", "/actuator/prometheus").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/employees", "/api/employees/**", "/api/dashboard/**")
                        .hasAnyAuthority("ROLE_EMPLOYEE", "ROLE_MANAGER", "ROLE_HR", "ROLE_ADMIN", "ROLE_DIRECTOR",
                                "ROLE_SUPER_ADMIN", "ROLE_HR_ADMIN", "ROLE_HR_MANAGER", "ROLE_HR_ANALYST")
                        .requestMatchers(HttpMethod.GET, "/api/employees/**").hasAnyAuthority("ROLE_EMPLOYEE", "ROLE_MANAGER", "ROLE_HR", "ROLE_ADMIN", "ROLE_DIRECTOR",
                                "ROLE_SUPER_ADMIN", "ROLE_HR_ADMIN", "ROLE_HR_MANAGER", "ROLE_HR_ANALYST")
                        .requestMatchers(HttpMethod.POST, "/api/employees").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_HR_ADMIN", "ROLE_HR_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/employees/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_HR_ADMIN", "ROLE_HR_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/employees/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_HR_ADMIN", "ROLE_HR_MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/dashboard/**").hasAnyAuthority("ROLE_EMPLOYEE", "ROLE_MANAGER", "ROLE_HR", "ROLE_ADMIN", "ROLE_DIRECTOR",
                                "ROLE_SUPER_ADMIN", "ROLE_HR_ADMIN", "ROLE_HR_MANAGER", "ROLE_HR_ANALYST")
                        .requestMatchers(HttpMethod.GET, "/api/ml/predict").hasAnyAuthority("ROLE_EMPLOYEE", "ROLE_MANAGER", "ROLE_HR", "ROLE_ADMIN", "ROLE_DIRECTOR",
                                "ROLE_SUPER_ADMIN", "ROLE_HR_ADMIN", "ROLE_HR_MANAGER", "ROLE_HR_ANALYST")
                        .requestMatchers("/api/audit/**", "/api/rag/ingestion/**", "/api/rag/documents/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_HR_ADMIN")
                        .requestMatchers("/api/notifications/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_HR_ADMIN", "ROLE_HR_MANAGER", "ROLE_HR_ANALYST")
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions.accessDeniedHandler((request, response, denied) -> {
                    AuditService service = audit.getIfAvailable();
                    if (service != null) service.record("AUTHORIZATION_FAILURE", "http", request.getRequestURI(), "FAILURE", "access denied");
                    response.sendError(403);
                }))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    static void validateIssuerUri(String value) {
        try {
            java.net.URI uri = java.net.URI.create(value == null ? "" : value);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getRawUserInfo() != null || uri.getRawQuery() != null
                    || uri.getRawFragment() != null || uri.getPort() == 0 || uri.getPort() > 65535) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException invalid) {
            throw new IllegalStateException("Secure authentication requires an HTTPS OIDC issuer URI with a host and no credentials, query or fragment");
        }
    }

    /** Maps the provider's conventional `roles` claim to Spring ROLE_ authorities. */
    @Bean
    @Profile("secure")
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}
