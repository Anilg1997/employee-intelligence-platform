package com.employeeintelligence.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ReportRequest(@NotBlank @Pattern(regexp = "(?i)EMPLOYEE|DEPARTMENT|WORKFORCE") String reportType,
                            Long employeeId, String department) { }
