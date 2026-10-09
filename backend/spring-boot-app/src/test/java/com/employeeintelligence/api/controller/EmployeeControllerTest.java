package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.model.Employee;
import com.employeeintelligence.api.service.EmployeeService;
import com.employeeintelligence.api.exception.EmployeeNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

        private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    void shouldGetAllEmployees() throws Exception {

        Employee employee =
                new Employee(
                        "Anil",
                        "Research & Development",
                        "Research Scientist",
                        30
                );

        when(employeeService.getEmployees())
                .thenReturn(List.of(employee));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Anil"))
                .andExpect(jsonPath("$[0].department")
                        .value("Research & Development"));
    }

    @Test
    void shouldGetEmployeeById() throws Exception {

        Employee employee =
                new Employee(
                        "Anil",
                        "Research & Development",
                        "Research Scientist",
                        30
                );

        when(employeeService.getEmployeeById(1L))
                .thenReturn(Optional.of(employee));

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Anil"))
                .andExpect(jsonPath("$.age").value(30));
    }

    @Test
    void shouldCreateEmployee() throws Exception {

        Employee employee =
                new Employee(
                        "Anil",
                        "Research & Development",
                        "Research Scientist",
                        30
                );

        when(employeeService.createEmployee(any(Employee.class)))
                .thenReturn(employee);

        mockMvc.perform(
                        post("/api/employees")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(employee))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Anil"))
                .andExpect(jsonPath("$.jobRole")
                        .value("Research Scientist"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingEmployee() throws Exception {
        Employee employee = new Employee("Test", "IT", "Developer", 30);
        when(employeeService.updateEmployee(eq(99L), any(Employee.class)))
                .thenThrow(new EmployeeNotFoundException(99L));

        mockMvc.perform(put("/api/employees/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Employee not found with id: 99"));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingMissingEmployee() throws Exception {
        doThrow(new EmployeeNotFoundException(99L))
                .when(employeeService).deleteEmployee(99L);

        mockMvc.perform(delete("/api/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Employee not found with id: 99"));
    }
}
