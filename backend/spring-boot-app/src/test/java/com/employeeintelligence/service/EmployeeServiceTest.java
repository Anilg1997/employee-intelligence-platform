package com.employeeintelligence.api.service;

import com.employeeintelligence.api.model.Employee;
import com.employeeintelligence.api.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

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

        when(employeeRepository.findAll())
                .thenReturn(List.of(employee));

        List<Employee> employees = employeeService.getEmployees();

        assertEquals(1, employees.size());
        assertEquals("Anil", employees.get(0).getName());

        verify(employeeRepository).findAll();
    }

    @Test
    void shouldGetEmployeeById() {

        Employee employee = new Employee(
                "Anil",
                "Research & Development",
                "Research Scientist",
                30
        );

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        Optional<Employee> result =
                employeeService.getEmployeeById(1L);

        assertTrue(result.isPresent());
        assertEquals("Anil", result.get().getName());

        verify(employeeRepository).findById(1L);
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

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(existingEmployee));

        when(employeeRepository.save(any(Employee.class)))
                .thenReturn(existingEmployee);

        Employee result =
                employeeService.updateEmployee(1L, updatedEmployee);

        assertEquals("Anil Kumar", result.getName());
        assertEquals("IT", result.getDepartment());
        assertEquals("Software Engineer", result.getJobRole());
        assertEquals(31, result.getAge());

        verify(employeeRepository).findById(1L);
        verify(employeeRepository).save(existingEmployee);
    }

    @Test
    void shouldDeleteEmployee() {

        when(employeeRepository.existsById(1L))
                .thenReturn(true);

        employeeService.deleteEmployee(1L);

        verify(employeeRepository).existsById(1L);
        verify(employeeRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingEmployee() {

        when(employeeRepository.findById(99L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
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

        verify(employeeRepository).findById(99L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingEmployee() {

        when(employeeRepository.existsById(99L))
                .thenReturn(false);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> employeeService.deleteEmployee(99L)
                );

        assertEquals(
                "Employee not found with id: 99",
                exception.getMessage()
        );

        verify(employeeRepository).existsById(99L);
        verify(employeeRepository, never()).deleteById(anyLong());
    }
}