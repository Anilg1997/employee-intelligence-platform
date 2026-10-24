package com.employeeintelligence.api.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Versioned, identity-only notification event envelope.
 *
 * Producers must not put display prose in this contract. Titles and bodies are
 * selected by the notification service from the allowlisted event type.
 */
public record NotificationEvent(int schemaVersion, NotificationEventType eventType,
                                String eventId, String tenantId, String recipientSubject) {
    public static final int CURRENT_SCHEMA_VERSION = 1;

    public NotificationEvent {
        if (schemaVersion != CURRENT_SCHEMA_VERSION) throw new IllegalArgumentException("Unsupported notification event schema");
        if (eventType == null) throw new IllegalArgumentException("Notification event type is required");
        validateIdentity("eventId", eventId);
        validateIdentity("tenantId", tenantId);
        validateIdentity("recipientSubject", recipientSubject);
    }

    /** Parses untrusted broker data. Invalid or non-notification payloads are not events. */
    public static Optional<NotificationEvent> parse(String payload, ObjectMapper mapper) {
        if (payload == null || payload.length() > 65536) return Optional.empty();
        try {
            JsonNode root = mapper.readTree(payload);
            if (root == null || !root.isObject()) return Optional.empty();
            JsonNode version = root.get("schemaVersion");
            if (version == null || !version.canConvertToInt()) return Optional.empty();
            return Optional.of(new NotificationEvent(version.intValue(),
                    NotificationEventType.fromWireValue(text(root, "eventType")),
                    text(root, "eventId"), text(root, "tenantId"), text(root, "recipientSubject")));
        } catch (JsonProcessingException | IllegalArgumentException invalid) {
            return Optional.empty();
        }
    }

    public Map<String, Object> toPayload() {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("schemaVersion", schemaVersion);
        payload.put("eventType", eventType.wireValue());
        payload.put("eventId", eventId);
        payload.put("tenantId", tenantId);
        payload.put("recipientSubject", recipientSubject);
        return payload;
    }

    private static String text(JsonNode root, String name) {
        JsonNode value = root.get(name);
        if (value == null || !value.isTextual()) throw new IllegalArgumentException("Notification event field must be text: " + name);
        return value.textValue();
    }

    private static void validateIdentity(String name, String value) {
        if (value == null || value.isBlank() || value.length() > 255 || value.chars().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Invalid notification event " + name);
    }
}
