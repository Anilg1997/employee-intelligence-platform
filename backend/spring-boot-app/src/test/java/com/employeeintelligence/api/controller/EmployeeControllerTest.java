package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.model.Employee;
import com.employeeintelligence.api.service.EmployeeService;
import com.employeeintelligence.api.exception.EmployeeNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import com.employeeintelligence.api.dto.EmployeePageResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.util.ReflectionTestUtils;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    private static final String FULL_EMPLOYEE = """
            {"id":7,"name":"Anil","department":"Research & Development",
             "jobRole":"Research Scientist","age":30,"attrition":"No",
             "businessTravel":"Travel_Rarely","dailyRate":1001,"distanceFromHome":2,
             "education":3,"educationField":"Life Sciences","employeeCount":1,
             "employeeNumber":42,"environmentSatisfaction":4,"gender":"Male",
             "hourlyRate":80,"jobInvolvement":2,"jobLevel":3,"jobSatisfaction":4,
             "maritalStatus":"Single","monthlyIncome":5000,"monthlyRate":12000,
             "numCompaniesWorked":2,"over18":"Y","overTime":"No",
             "percentSalaryHike":15,"performanceRating":3,"relationshipSatisfaction":2,
             "standardHours":80,"stockOptionLevel":0,"totalWorkingYears":10,
             "trainingTimesLastYear":3,"workLifeBalance":4,"yearsAtCompany":8,
             "yearsInCurrentRole":5,"yearsSinceLastPromotion":2,"yearsWithCurrManager":6}
            """;

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
    void shouldReturnStableSearchPageShapeAndEmployeeResponses() throws Exception {
        Employee employee = new Employee("Anil", "Research & Development", "Research Scientist", 30);
        when(employeeService.searchEmployees(eq("anil"), eq("IT"), eq("0"), eq("10"),
                eq("name"), eq("ASC")))
                .thenReturn(new EmployeePageResponse(
                        List.of(com.employeeintelligence.api.mapper.EmployeeMapper.toResponse(employee)),
                        0, 10, 1, 1));

        mockMvc.perform(get("/api/employees/search")
                        .param("query", "anil")
                        .param("department", "IT")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "name")
                        .param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Anil"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
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

    @Test
    void shouldPreserveEveryResponseFieldOnListAndDetail() throws Exception {
        Employee employee = objectMapper.readValue(FULL_EMPLOYEE, Employee.class);
        when(employeeService.getEmployees()).thenReturn(List.of(employee));
        when(employeeService.getEmployeeById(7L)).thenReturn(Optional.of(employee));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(content().json("[" + FULL_EMPLOYEE + "]", JsonCompareMode.STRICT));
        mockMvc.perform(get("/api/employees/7"))
                .andExpect(status().isOk())
                .andExpect(content().json(FULL_EMPLOYEE, JsonCompareMode.STRICT));
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT"})
    void shouldMapEveryWritableFieldAndKeepServerIdentity(String method) throws Exception {
        when(employeeService.createEmployee(any(Employee.class))).thenAnswer(invocation -> {
            Employee employee = invocation.getArgument(0);
            assertNull(employee.getId());
            ReflectionTestUtils.setField(employee, "id", 7L);
            return employee;
        });
        when(employeeService.updateEmployee(eq(7L), any(Employee.class))).thenAnswer(invocation -> {
            Employee employee = invocation.getArgument(1);
            assertNull(employee.getId());
            ReflectionTestUtils.setField(employee, "id", 7L);
            return employee;
        });

        mockMvc.perform((method.equals("POST") ? post("/api/employees") : put("/api/employees/7"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FULL_EMPLOYEE.replace("\"id\":7", "\"id\":999")))
                .andExpect(status().isOk())
                .andExpect(content().json(FULL_EMPLOYEE, JsonCompareMode.STRICT));
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT"})
    void shouldAcceptNullableDemoFieldsAndPreserveNullResponseFields(String method) throws Exception {
        Employee employee = new Employee();
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(employee);
        when(employeeService.updateEmployee(eq(7L), any(Employee.class))).thenReturn(employee);

        mockMvc.perform((method.equals("POST") ? post("/api/employees") : put("/api/employees/7"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":null,\"age\":null,\"monthlyIncome\":null}"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(employee), JsonCompareMode.STRICT));

        ArgumentCaptor<Employee> captured = ArgumentCaptor.forClass(Employee.class);
        if (method.equals("POST")) {
            verify(employeeService).createEmployee(captured.capture());
        } else {
            verify(employeeService).updateEmployee(eq(7L), captured.capture());
        }
        assertEquals(objectMapper.valueToTree(employee), objectMapper.valueToTree(captured.getValue()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT"})
    void shouldRejectInvalidNumbersAndOversizedStringsBeforeCallingService(String method) throws Exception {
        String payload = "{\"age\":-1,\"monthlyIncome\":-1,\"yearsAtCompany\":-1,\"name\":\""
                + "x".repeat(256) + "\"}";
        mockMvc.perform((method.equals("POST") ? post("/api/employees") : put("/api/employees/7"))
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.age").isString())
                .andExpect(jsonPath("$.errors.monthlyIncome").isString())
                .andExpect(jsonPath("$.errors.yearsAtCompany").isString())
                .andExpect(jsonPath("$.errors.name").isString());
        verifyNoInteractions(employeeService);
    }

    @Test
    void shouldReturnEmptyList() throws Exception {
        when(employeeService.getEmployees()).thenReturn(List.of());
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]", JsonCompareMode.STRICT));
    }

    @Test
    void shouldReturnEmptyNotFoundForMissingDetail() throws Exception {
        when(employeeService.getEmployeeById(99L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));
    }

    @Test
    void shouldDeleteEmployee() throws Exception {
        mockMvc.perform(delete("/api/employees/7"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(employeeService).deleteEmployee(7L);
    }
}
