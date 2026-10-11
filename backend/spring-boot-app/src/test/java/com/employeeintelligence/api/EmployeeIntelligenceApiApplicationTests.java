package com.employeeintelligence.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import dev.langchain4j.model.chat.ChatModel;
import com.employeeintelligence.api.agent.EmployeeAiAgent;
import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@Import(EmployeeIntelligenceApiApplicationTests.TestChatModelConfig.class)
class EmployeeIntelligenceApiApplicationTests {

    private static final String SCHEMA = "nexora_it_" + UUID.randomUUID().toString().replace("-", "");

    @DynamicPropertySource
    static void isolatedDatabase(DynamicPropertyRegistry properties) {
        properties.add("spring.flyway.schemas", () -> SCHEMA);
        properties.add("spring.flyway.default-schema", () -> SCHEMA);
        properties.add("spring.flyway.baseline-on-migrate", () -> "false");
        properties.add("spring.jpa.properties.hibernate.default_schema", () -> SCHEMA);
        properties.add("spring.datasource.hikari.connection-init-sql", () -> "SET search_path TO " + SCHEMA + ", public");
        properties.add("employee.import.enabled", () -> "false");
    }

    @Autowired private Flyway flyway;
    @Autowired private DataSource dataSource;
    @Autowired private com.employeeintelligence.api.repository.EmployeeRepository employees;

    @AfterAll
    static void removeTestSchema(@Autowired DataSource dataSource) {
        // Only the UUID-named schema owned by this test is removed; never public.
        new JdbcTemplate(dataSource).execute("DROP SCHEMA IF EXISTS " + SCHEMA + " CASCADE");
    }

    @TestConfiguration
    static class TestChatModelConfig {
        @Bean
        ChatModel chatModel() {
            return mock(ChatModel.class);
        }

        @Bean
        EmployeeAiAgent employeeAiAgent() {
            return mock(EmployeeAiAgent.class);
        }
    }

	@Test
	void contextLoads() {

        assertEquals("4", flyway.info().current().getVersion().getVersion());
        assertTrue(flyway.validateWithResult().validationSuccessful);
	}

    @Test
    void upgradesLegacyRowsWithoutLosingDataAndScopesUniquenessToTenant() {
        String legacy = SCHEMA + "_legacy";
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        try {
            Flyway.configure().dataSource(dataSource).schemas(legacy).defaultSchema(legacy)
                    .target("3").load().migrate();
            jdbc.update("INSERT INTO " + legacy + ".employee (name, employee_number) VALUES (?, ?)", "Migration fixture", 42);
            jdbc.update("INSERT INTO " + legacy + ".rag_documents (content) VALUES (?)", "Migration policy fixture");
            jdbc.update("INSERT INTO " + legacy + ".audit_events (event_timestamp, actor_subject, action, result) VALUES (CURRENT_TIMESTAMP, ?, ?, ?)",
                    "fixture-actor", "FIXTURE", "SUCCESS");

            Flyway upgrade = Flyway.configure().dataSource(dataSource).schemas(legacy).defaultSchema(legacy).load();
            upgrade.migrate();
            assertEquals("4", upgrade.info().current().getVersion().getVersion());
            assertEquals("Migration fixture", jdbc.queryForObject("SELECT name FROM " + legacy + ".employee WHERE tenant_id = 'demo'", String.class));
            assertEquals("Migration policy fixture", jdbc.queryForObject("SELECT content FROM " + legacy + ".rag_documents WHERE tenant_id = 'demo'", String.class));
            assertEquals("FIXTURE", jdbc.queryForObject("SELECT action FROM " + legacy + ".audit_events WHERE tenant_id = 'demo'", String.class));
            jdbc.update("INSERT INTO " + legacy + ".employee (tenant_id, employee_number) VALUES (?, ?)", "another-tenant", 42);
            assertThrows(org.springframework.dao.DuplicateKeyException.class, () ->
                    jdbc.update("INSERT INTO " + legacy + ".employee (tenant_id, employee_number) VALUES (?, ?)", "another-tenant", 42));
        } finally {
            jdbc.execute("DROP SCHEMA IF EXISTS " + legacy + " CASCADE");
        }
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    void employeeQueriesDoNotReturnAnotherTenantsRows() {
        var employee = new com.employeeintelligence.api.model.Employee("Tenant fixture", "Engineering", "Developer", 30);
        employee.setTenantId("tenant-a");
        var saved = employees.saveAndFlush(employee);
        assertTrue(employees.findByIdAndTenantId(saved.getId(), "tenant-a").isPresent());
        assertTrue(employees.findByIdAndTenantId(saved.getId(), "tenant-b").isEmpty());
        assertFalse(employees.existsByIdAndTenantId(saved.getId(), "tenant-b"));
        assertTrue(employees.findAllByTenantId("tenant-b").isEmpty());
        assertEquals(0, employees.search("tenant-b", "Tenant", null,
                org.springframework.data.domain.PageRequest.of(0, 10)).getTotalElements());
        assertEquals(1, employees.search("tenant-a", "Tenant", null,
                org.springframework.data.domain.PageRequest.of(0, 10)).getTotalElements());
        assertEquals(1, employees.search("tenant-a", null, null,
                org.springframework.data.domain.PageRequest.of(0, 10)).getTotalElements());
        assertEquals(1, employees.search("tenant-a", null, "engineering",
                org.springframework.data.domain.PageRequest.of(0, 10)).getTotalElements());
        assertEquals(0, employees.search("tenant-b", null, null,
                org.springframework.data.domain.PageRequest.of(0, 10)).getTotalElements());
    }

}
