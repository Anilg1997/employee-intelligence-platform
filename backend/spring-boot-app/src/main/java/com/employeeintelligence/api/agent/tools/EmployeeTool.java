package com.employeeintelligence.api.agent.tools;

import com.employeeintelligence.api.model.Employee;
import com.employeeintelligence.api.service.EmployeeAiService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;
import com.employeeintelligence.api.agent.AgentToolSecurity;

@Component
public class EmployeeTool {

    private final EmployeeAiService employeeAiService;
    private final AgentToolSecurity security;

    public EmployeeTool(EmployeeAiService employeeAiService, AgentToolSecurity security) {
        this.employeeAiService = employeeAiService;
        this.security = security;
    }

    @Tool("Get employee information using the employee ID")
    public Employee getEmployee(Long employeeId) {

        security.check("get_employee");
        var result = employeeAiService.getEmployee(employeeId);
        security.completed("get_employee", "returned");
        return result;
    }
}
