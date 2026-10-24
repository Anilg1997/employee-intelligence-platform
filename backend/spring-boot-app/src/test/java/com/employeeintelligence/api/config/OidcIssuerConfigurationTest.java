package com.employeeintelligence.api.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class OidcIssuerConfigurationTest {
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "http://issuer.example", "https://", "https:///realm",
            "https://user:secret@issuer.example", "https://issuer.example?token=secret",
            "https://issuer.example#fragment", "https://issuer.example:0", "https://issuer.example:65536"})
    void rejectsMalformedOrUnsafeIssuerWithoutEchoingIt(String issuer) {
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> SecurityConfig.validateIssuerUri(issuer));
        assertFalse(error.getMessage().contains("secret"));
    }

    @Test
    void acceptsHttpsIssuerWithRealmAndOptionalPort() {
        assertDoesNotThrow(() -> SecurityConfig.validateIssuerUri("https://identity.example/realms/nexora"));
        assertDoesNotThrow(() -> SecurityConfig.validateIssuerUri("https://identity.example:8443/realms/nexora"));
    }
}
