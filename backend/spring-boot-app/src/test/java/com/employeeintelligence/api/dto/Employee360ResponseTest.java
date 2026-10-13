package com.employeeintelligence.api.dto;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class Employee360ResponseTest {
    @Test
    void copiesActivityTimelineAndDefaultsNullToEmpty() {
        EmployeeResponse employee = com.employeeintelligence.api.mapper.EmployeeMapper.toResponse(
                new com.employeeintelligence.api.model.Employee("Anil", "IT", "Developer", 30));
        Employee360Response response = new Employee360Response(employee, null, null);
        assertTrue(response.activity().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> response.activity().add(null));
    }
}
