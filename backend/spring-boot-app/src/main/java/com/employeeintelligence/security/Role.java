package com.employeeintelligence.security;

/** Roles expected from the external OIDC provider's role claim mapping. */
public enum Role {
    SUPER_ADMIN,
    HR_ADMIN,
    HR_MANAGER,
    HR_ANALYST,
    EMPLOYEE
}
