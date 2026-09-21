package com.employeeintelligence.api.agent.tools;

import com.employeeintelligence.api.model.Employee;
import com.employeeintelligence.api.service.EmployeeAiService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class EmployeeTool {

    private final EmployeeAiService employeeAiService;

    public EmployeeTool(EmployeeAiService employeeAiService) {
        this.employeeAiService = employeeAiService;
    }

    @Tool("Get employee information using the employee ID")
    public Employee getEmployee(Long employeeId) {

        return employeeAiService.getEmployee(employeeId);
    }
}