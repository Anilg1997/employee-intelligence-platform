package com.employeeintelligence.api.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NotificationCursorTest {
    @Test
    void acceptsNumericLastEventIdAndRejectsInjection() {
        assertEquals(42L, NotificationService.parseCursor("42"));
        assertEquals(0L, NotificationService.parseCursor(null));
        assertThrows(IllegalArgumentException.class, () -> NotificationService.parseCursor("42,43"));
        assertThrows(IllegalArgumentException.class, () -> NotificationService.parseCursor("-1"));
    }
}
