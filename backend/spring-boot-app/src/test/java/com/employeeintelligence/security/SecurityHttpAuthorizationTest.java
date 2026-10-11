package com.employeeintelligence.security;

import com.employeeintelligence.api.controller.EmployeeController;
import com.employeeintelligence.api.controller.RagIngestionController;
import com.employeeintelligence.api.EmployeeIntelligenceApiApplication;
import com.employeeintelligence.api.service.AuditService;
import com.employeeintelligence.api.service.EmployeeService;
import com.employeeintelligence.api.service.RagDocumentIngestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {EmployeeController.class, RagIngestionController.class})
@Import(com.employeeintelligence.api.config.SecurityConfig.class)
@ContextConfiguration(classes = EmployeeIntelligenceApiApplication.class)
@TestPropertySource(properties = "security.auth.enabled=true")
class SecurityHttpAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private RagDocumentIngestionService ingestionService;

    @MockitoBean
    private AuditService auditService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean(name = "securityMode")
    private SecurityMode securityMode;

    @BeforeEach
    void secureMode() {
        when(securityMode.isDemoMode()).thenReturn(false);
    }

    @Test
    void employeeRoleCannotReadSensitiveDirectory() throws Exception {
        mockMvc.perform(get("/api/employees").with(jwt().jwt(j -> j.claim("tenant_id", "acme")).authorities(() -> "ROLE_EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void hrAnalystCanReadDirectory() throws Exception {
        when(employeeService.getEmployees()).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/employees").with(jwt().jwt(j -> j.claim("tenant_id", "acme")).authorities(() -> "ROLE_HR_ANALYST")))
                .andExpect(status().isOk());
    }

    @Test
    void analystCannotModifyEmployee() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .with(jwt().jwt(j -> j.claim("tenant_id", "acme")).authorities(() -> "ROLE_HR_ANALYST"))
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCannotIngestRagDocuments() throws Exception {
        mockMvc.perform(post("/api/rag/ingestion")
                        .with(jwt().jwt(j -> j.claim("tenant_id", "acme")).authorities(() -> "ROLE_HR_MANAGER"))
                        .param("filePath", "document.txt")
                        .param("source", "test"))
                .andExpect(status().isForbidden());
    }
}
