package com.employeeintelligence.api.graphql;

import com.employeeintelligence.api.dto.DashboardResponse;
import com.employeeintelligence.api.service.DashboardService;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * Small GraphQL proof-of-value for dashboard consumers that need a tailored
 * aggregate shape. It deliberately delegates to the tenant-scoped REST read model.
 */
@Controller
public class DashboardGraphQlController {
    private final DashboardService dashboardService;

    public DashboardGraphQlController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @QueryMapping
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN', 'ROLE_HR_MANAGER', 'ROLE_HR_ANALYST')")
    public Dashboard dashboard() {
        return new Dashboard(dashboardService.getDashboardSummary());
    }

    public record Dashboard(int totalEmployees, Double attritionRate, List<Department> departments) {
        Dashboard(DashboardResponse source) {
            this(Math.toIntExact(source.getTotalEmployees()), source.getAttritionRate(), source.getDepartmentSummaries().stream()
                    .map(d -> new Department(d.getDepartment(), Math.toIntExact(d.getEmployeeCount()),
                            source.getTotalEmployees() == 0 ? 0.0 : (double) d.getEmployeeCount() / source.getTotalEmployees()))
                    .toList());
        }
    }

    public record Department(String name, int count, Double share) {}
}
