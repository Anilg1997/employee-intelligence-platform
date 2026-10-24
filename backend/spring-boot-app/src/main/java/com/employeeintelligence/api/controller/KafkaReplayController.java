package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.dto.DlqReplayRequest;
import com.employeeintelligence.api.service.KafkaReplayService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/kafka")
public class KafkaReplayController {
    private final KafkaReplayService service;
    public KafkaReplayController(KafkaReplayService service) { this.service = service; }

    @PostMapping("/outbox/{id}/replay")
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN')")
    public void replayOutbox(@PathVariable Long id) { service.replayOutbox(id); }

    @PostMapping("/dlq/replay")
    @PreAuthorize("@securityMode.demoMode or hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_HR_ADMIN')")
    public void replayDlq(@Valid @RequestBody DlqReplayRequest request) { service.replayDlq(request); }
}
