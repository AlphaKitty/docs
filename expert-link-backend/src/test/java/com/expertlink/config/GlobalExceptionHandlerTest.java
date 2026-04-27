package com.expertlink.config;

import com.expertlink.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void accessDeniedShouldReturnForbiddenInsteadOfInternalServerError() {
        ResponseEntity<ApiResponse<Void>> resp = handler.accessDenied(new AccessDeniedException("Access Denied"));

        assertEquals(403, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertEquals(403, resp.getBody().getCode());
    }
}
