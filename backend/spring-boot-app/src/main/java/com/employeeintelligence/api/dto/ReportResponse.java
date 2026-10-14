package com.employeeintelligence.api.dto;

import java.time.Instant;

public record ReportResponse(Long id, String reportType, Long employeeId, String department, String status,
                             String content, boolean aiAssisted, String limitations, String requester,
                             String humanReviewState, Instant createdAt) {
    public static ReportResponse from(com.employeeintelligence.api.model.Report r) {
        return new ReportResponse(r.getId(), r.getReportType(), r.getEmployeeId(), r.getDepartment(), r.getStatus(),
                r.getContent(), r.isAiAssisted(), r.getLimitations(), r.getRequester(), r.getHumanReviewState(), r.getCreatedAt());
    }
}
