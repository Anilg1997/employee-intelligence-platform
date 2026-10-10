package com.employeeintelligence.security;

import com.employeeintelligence.api.controller.EmployeeController;
import com.employeeintelligence.api.controller.RagIngestionController;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import static org.junit.jupiter.api.Assertions.*;

class SecurityAuthorizationTest {
    @Test
    void exposesTheDefinedRoleContract() {
        assertArrayEquals(new Role[] { Role.SUPER_ADMIN, Role.HR_ADMIN, Role.HR_MANAGER,
                Role.HR_ANALYST, Role.EMPLOYEE }, Role.values());
    }

    @Test
    void highImpactEndpointsDeclareRoleChecks() throws Exception {
        PreAuthorize delete = EmployeeController.class.getDeclaredMethod("deleteEmployee", Long.class)
                .getAnnotation(PreAuthorize.class);
        PreAuthorize ingestion = RagIngestionController.class.getDeclaredMethod("ingest", String.class, String.class)
                .getAnnotation(PreAuthorize.class);
        assertNotNull(delete);
        assertTrue(delete.value().contains("ROLE_HR_ADMIN"));
        assertNotNull(ingestion);
        assertTrue(ingestion.value().contains("ROLE_HR_MANAGER"));
    }

    @Test
    void localModeExplicitlyBypassesRoleChecksForTheDemo() {
        assertTrue(new SecurityMode(false).isDemoMode());
        assertFalse(new SecurityMode(true).isDemoMode());
    }
}
