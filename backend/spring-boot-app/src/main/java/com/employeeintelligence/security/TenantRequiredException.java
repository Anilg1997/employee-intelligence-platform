package com.employeeintelligence.security;

public class TenantRequiredException extends org.springframework.security.access.AccessDeniedException {
    public TenantRequiredException(String message) { super(message); }
}
