package com.employeeintelligence.api.dto;

import java.util.List;

/** Employee detail composed with a bounded, tenant-scoped activity timeline and risk metadata. */
public record Employee360Response(EmployeeResponse employee,
                                  List<AuditEventResponse> activity,
                                  RiskSummaryResponse riskSummary) {
    public Employee360Response {
        activity = activity == null ? List.of() : List.copyOf(activity);
    }
}
