package com.employeeintelligence.api.exception;

import com.employeeintelligence.api.dto.ApiErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldUseApiErrorResponseForDomainErrors() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleEmployeeNotFoundException(new EmployeeNotFoundException(42L));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().status());
        assertEquals("Employee not found with id: 42", response.getBody().message());
        assertNull(response.getBody().errors());
    }

    @Test
    void shouldUseApiErrorResponseForBadRequestAndMlErrors() {
        ResponseEntity<ApiErrorResponse> badRequest =
                handler.handleIllegalArgumentException(new IllegalArgumentException("Invalid input"));
        ResponseEntity<ApiErrorResponse> unavailable =
                handler.handleMlServiceException(new MlServiceException("ML service unavailable", null));

        assertEquals(HttpStatus.BAD_REQUEST, badRequest.getStatusCode());
        assertEquals("Invalid input", badRequest.getBody().message());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, unavailable.getStatusCode());
        assertEquals("ML service unavailable", unavailable.getBody().message());
    }

    @Test
    void shouldUseApiErrorResponseForUnexpectedErrors() {
        ResponseEntity<ApiErrorResponse> response =
                handler.handleGenericException(new RuntimeException("failure"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().status());
        assertEquals("An unexpected error occurred", response.getBody().message());
        assertNull(response.getBody().errors());
    }
}
