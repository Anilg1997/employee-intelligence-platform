package com.employeeintelligence.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

@Component("securityMode")
public class SecurityMode {

    private final boolean demoMode;

    @Autowired
    public SecurityMode(Environment environment) {
        this.demoMode = environment.acceptsProfiles(Profiles.of("local"));
    }

    public SecurityMode(boolean securityEnabled) {
        this.demoMode = !securityEnabled;
    }

    public boolean isDemoMode() {
        return demoMode;
    }
}