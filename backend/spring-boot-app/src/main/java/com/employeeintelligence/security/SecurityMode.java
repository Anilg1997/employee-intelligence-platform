package com.employeeintelligence.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("securityMode")
public class SecurityMode {
    private final boolean demoMode;

    public SecurityMode(@Value("${security.auth.enabled:false}") boolean authEnabled) {
        this.demoMode = !authEnabled;
    }

    public boolean isDemoMode() {
        return demoMode;
    }
}
