package com.employeeintelligence.api.notification;

import java.util.Arrays;

/** Event types that are explicitly allowed to produce user notifications. */
public enum NotificationEventType {
    RISK_DETECTED("risk.detected"),
    PREDICTION_COMPLETED("prediction.completed"),
    REPORT_GENERATED("report.generated");

    private final String wireValue;

    NotificationEventType(String wireValue) { this.wireValue = wireValue; }

    public String wireValue() { return wireValue; }

    public static NotificationEventType fromWireValue(String value) {
        return Arrays.stream(values()).filter(type -> type.wireValue.equals(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported notification event type"));
    }
}
