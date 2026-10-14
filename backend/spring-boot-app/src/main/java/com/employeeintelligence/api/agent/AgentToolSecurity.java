package com.employeeintelligence.api.agent;

import com.employeeintelligence.api.service.AuditService;
import com.employeeintelligence.security.AuthenticatedUser;
import com.employeeintelligence.security.SecurityMode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Set;

/** Backend authorization boundary; prompts and MCP configuration cannot grant access. */
@Component
public class AgentToolSecurity {
    private static final Map<String, Set<String>> ROLE_TOOLS = Map.of(
            "SUPER_ADMIN", Set.of("get_employee", "assess_attrition_risk", "search_hr_policy"),
            "HR_ADMIN", Set.of("get_employee", "assess_attrition_risk", "search_hr_policy"),
            "HR_MANAGER", Set.of("get_employee", "assess_attrition_risk", "search_hr_policy"),
            "HR_ANALYST", Set.of("get_employee", "assess_attrition_risk", "search_hr_policy"));
    private final AuditService audit;
    private final SecurityMode securityMode;
    public AgentToolSecurity(AuditService audit, SecurityMode securityMode) { this.audit = audit; this.securityMode = securityMode; }
    public void check(String toolName) {
        var user = AuthenticatedUser.current();
        boolean allowed = securityMode.isDemoMode() || user.roles().stream().map(String::toUpperCase)
                .anyMatch(role -> ROLE_TOOLS.getOrDefault(role, Set.of()).contains(toolName));
        try {
            if (!allowed) throw new AccessDeniedException("agent tool is not authorized");
            AgentExecutionBudget budget = AgentExecutionBudget.current();
            if (budget != null) budget.consumeToolCall();
            audit.record("AGENT_TOOL_CALL", "agent_tool", toolName, "STARTED", "tool=" + toolName + ";subject=" + user.subject());
        } catch (RuntimeException denied) {
            audit.record("AGENT_TOOL_CALL", "agent_tool", toolName, "DENIED", "tool=" + toolName);
            throw denied;
        }
    }
    public void completed(String toolName, String result) {
        audit.record("AGENT_TOOL_CALL", "agent_tool", toolName, "SUCCESS", "tool=" + toolName + ";result=" + result);
    }
}
