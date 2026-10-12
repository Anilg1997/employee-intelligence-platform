package com.employeeintelligence.api.controller;

import com.employeeintelligence.api.dto.NotificationResponse;
import com.employeeintelligence.api.service.NotificationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService service;
    public NotificationController(NotificationService service) { this.service = service; }

    @GetMapping
    public List<NotificationResponse> recent(@RequestParam(defaultValue = "20") int size) { return service.recent(size); }

    @PostMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable Long id) { return service.markRead(id); }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestHeader(value = "Last-Event-ID", required = false) String cursor) { return service.subscribe(cursor); }

    @ExceptionHandler(java.util.NoSuchElementException.class)
    public ResponseEntity<Void> missingNotification() { return ResponseEntity.notFound().build(); }
}
