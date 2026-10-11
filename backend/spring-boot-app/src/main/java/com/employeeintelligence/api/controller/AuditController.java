package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.dto.AuditEventResponse;
import com.employeeintelligence.api.service.AuditService;
import org.springframework.data.domain.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/audit")
public class AuditController {
    private final AuditService auditService;
    public AuditController(AuditService auditService) { this.auditService = auditService; }
    @GetMapping("/events")
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN')")
    public Map<String,Object> events(@RequestParam(required=false) String action, @RequestParam(required=false) String result,
                                      @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="25") int size) {
        Page<?> found = auditService.find(action, result, PageRequest.of(Math.max(0,page), Math.min(Math.max(1,size),100), Sort.by(Sort.Direction.DESC,"timestamp")));
        return Map.of("content", found.getContent().stream().map(e -> AuditEventResponse.from((com.employeeintelligence.api.model.AuditEvent)e)).toList(), "page", found.getNumber(), "size", found.getSize(), "totalElements", found.getTotalElements(), "totalPages", found.getTotalPages());
    }
}
