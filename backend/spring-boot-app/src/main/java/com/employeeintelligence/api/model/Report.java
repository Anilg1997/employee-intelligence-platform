package com.employeeintelligence.api.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "reports", indexes = @Index(name = "reports_tenant_created_idx", columnList = "tenant_id,created_at"))
public class Report {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "tenant_id", nullable = false) private String tenantId;
    @Column(name = "report_type", nullable = false) private String reportType;
    @Column(name = "employee_id") private Long employeeId;
    private String department;
    @Column(nullable = false) private String status;
    @Lob @Column(nullable = false) private String content;
    @Column(name = "ai_assisted", nullable = false) private boolean aiAssisted;
    @Lob @Column(nullable = false) private String limitations;
    @Column(nullable = false) private String requester;
    @Column(name = "human_review_state", nullable = false) private String humanReviewState;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    public Report() { }
    public Long getId() { return id; }
    public String getTenantId() { return tenantId; } public void setTenantId(String v) { tenantId = v; }
    public String getReportType() { return reportType; } public void setReportType(String v) { reportType = v; }
    public Long getEmployeeId() { return employeeId; } public void setEmployeeId(Long v) { employeeId = v; }
    public String getDepartment() { return department; } public void setDepartment(String v) { department = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public String getContent() { return content; } public void setContent(String v) { content = v; }
    public boolean isAiAssisted() { return aiAssisted; } public void setAiAssisted(boolean v) { aiAssisted = v; }
    public String getLimitations() { return limitations; } public void setLimitations(String v) { limitations = v; }
    public String getRequester() { return requester; } public void setRequester(String v) { requester = v; }
    public String getHumanReviewState() { return humanReviewState; } public void setHumanReviewState(String v) { humanReviewState = v; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant v) { createdAt = v; }
}
