package com.employeeintelligence.api.service;

import com.employeeintelligence.api.model.Employee;
import com.employeeintelligence.api.repository.EmployeeRepository;
import com.employeeintelligence.api.exception.EmployeeNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import com.employeeintelligence.security.TenantContext;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import com.employeeintelligence.api.dto.EmployeePageResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Spy private TenantContext tenants = new TenantContext(false);

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void shouldGetAllEmployees() {

        Employee employee = new Employee(
                "Anil",
                "Research & Development",
                "Research Scientist",
                30
        );

        when(employeeRepository.findAllByTenantId("demo"))
                .thenReturn(List.of(employee));

        List<Employee> employees = employeeService.getEmployees();

        assertEquals(1, employees.size());
        assertEquals("Anil", employees.get(0).getName());

        verify(employeeRepository).findAllByTenantId("demo");
    }

    @Test
    void shouldSearchWithFiltersPaginationAndAllowlistedSort() {
        Employee employee = new Employee("Anil", "Research & Development", "Research Scientist", 30);
        when(employeeRepository.search(eq("demo"), eq("anil"), eq("Research & Development"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(employee),
                        org.springframework.data.domain.PageRequest.of(1, 2), 5));

        EmployeePageResponse result = employeeService.searchEmployees(
                " anil ", " Research & Development ", "1", "2", "name", "DESC");

        assertEquals(1, result.page());
        assertEquals(2, result.size());
        assertEquals(5, result.totalElements());
        assertEquals(3, result.totalPages());
        assertEquals("Anil", result.content().get(0).name());
        verify(employeeRepository).search(eq("demo"), eq("anil"), eq("Research & Development"),
                argThat(pageable -> pageable.getSort().getOrderFor("name").isDescending()));
    }

    @Test
    void shouldSafelyDefaultInvalidSearchParameters() {
        when(employeeRepository.search(eq("demo"), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(),
                        org.springframework.data.domain.PageRequest.of(0, 1), 0));

        EmployeePageResponse result = employeeService.searchEmployees(
                " ", "", "not-a-number", "-10", "name;drop", "sideways");

        assertEquals(0, result.page());
        assertEquals(1, result.size());
        verify(employeeRepository).search(eq("demo"), isNull(), isNull(), argThat(pageable ->
                pageable.getSort().getOrderFor("name").isAscending()));
    }

    @Test
    void shouldGetEmployeeById() {

        Employee employee = new Employee(
                "Anil",
                "Research & Development",
                "Research Scientist",
                30
        );

        when(employeeRepository.findByIdAndTenantId(1L, "demo"))
                .thenReturn(Optional.of(employee));

        Optional<Employee> result =
                employeeService.getEmployeeById(1L);

        assertTrue(result.isPresent());
        assertEquals("Anil", result.get().getName());

        verify(employeeRepository).findByIdAndTenantId(1L, "demo");
    }

    @Test
    void shouldCreateEmployee() {

        Employee employee = new Employee(
                "Anil",
                "Research & Development",
                "Research Scientist",
                30
        );

        when(employeeRepository.save(any(Employee.class)))
                .thenReturn(employee);

        Employee result =
                employeeService.createEmployee(employee);

        assertNotNull(result);
        assertEquals("Anil", result.getName());
        assertEquals("Research Scientist", result.getJobRole());

        verify(employeeRepository).save(employee);
    }

    @Test
    void shouldUpdateEmployee() {

        Employee existingEmployee = new Employee(
                "Anil",
                "Research & Development",
                "Research Scientist",
                30
        );

        Employee updatedEmployee = new Employee(
                "Anil Kumar",
                "IT",
                "Software Engineer",
                31
        );

        when(employeeRepository.findByIdAndTenantId(1L, "demo"))
                .thenReturn(Optional.of(existingEmployee));

        when(employeeRepository.save(any(Employee.class)))
                .thenReturn(existingEmployee);

        Employee result =
                employeeService.updateEmployee(1L, updatedEmployee);

        assertEquals("Anil Kumar", result.getName());
        assertEquals("IT", result.getDepartment());
        assertEquals("Software Engineer", result.getJobRole());
        assertEquals(31, result.getAge());

        verify(employeeRepository).findByIdAndTenantId(1L, "demo");
        verify(employeeRepository).save(existingEmployee);
    }

    @Test
    void shouldDeleteEmployee() {

        when(employeeRepository.existsByIdAndTenantId(1L, "demo"))
                .thenReturn(true);

        employeeService.deleteEmployee(1L);

        verify(employeeRepository).existsByIdAndTenantId(1L, "demo");
        verify(employeeRepository).deleteByIdAndTenantId(1L, "demo");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingEmployee() {

        when(employeeRepository.findByIdAndTenantId(99L, "demo"))
                .thenReturn(Optional.empty());

        EmployeeNotFoundException exception =
                assertThrows(
                        EmployeeNotFoundException.class,
                        () -> employeeService.updateEmployee(
                                99L,
                                new Employee(
                                        "Test",
                                        "IT",
                                        "Developer",
                                        30
                                )
                        )
                );

        assertEquals(
                "Employee not found with id: 99",
                exception.getMessage()
        );

        verify(employeeRepository).findByIdAndTenantId(99L, "demo");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingEmployee() {

        when(employeeRepository.existsByIdAndTenantId(99L, "demo"))
                .thenReturn(false);

        EmployeeNotFoundException exception =
                assertThrows(
                        EmployeeNotFoundException.class,
                        () -> employeeService.deleteEmployee(99L)
                );

        assertEquals(
                "Employee not found with id: 99",
                exception.getMessage()
        );

        verify(employeeRepository).existsByIdAndTenantId(99L, "demo");
        verify(employeeRepository, never()).deleteByIdAndTenantId(anyLong(), anyString());
    }
}
