package com.expertlink.dto;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ApiResponseTest {

    @Test
    void successResponseWrapsPayloadWithMetadata() {
        ApiResponse<String> response = ApiResponse.success("ok");

        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertEquals("success", response.getMessage());
        assertEquals("ok", response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void createdResponseWrapsPayloadWithCreatedStatus() {
        ApiResponse<String> response = ApiResponse.created("created");

        assertEquals(HttpStatus.CREATED.value(), response.getCode());
        assertEquals("created", response.getMessage());
        assertEquals("created", response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void paginatedResponseMirrorsSpringPageShape() {
        Page<String> page = new PageImpl<>(List.of("a", "b"), PageRequest.of(1, 2), 5);

        PaginatedResponse<String> response = PaginatedResponse.from(page);

        assertEquals(List.of("a", "b"), response.getContent());
        assertEquals(5, response.getTotalElements());
        assertEquals(3, response.getTotalPages());
        assertEquals(2, response.getSize());
        assertEquals(1, response.getNumber());
        assertEquals(false, response.isFirst());
        assertEquals(false, response.isLast());
        assertEquals(false, response.isEmpty());
    }

    @Test
    void noContentResponseUsesNullPayload() {
        ResponseEntity<ApiResponse<Void>> responseEntity = ApiResponses.noContent("deleted");

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.OK.value(), responseEntity.getBody().getCode());
        assertEquals("deleted", responseEntity.getBody().getMessage());
        assertNull(responseEntity.getBody().getData());
        assertNotNull(responseEntity.getBody().getTimestamp());
    }
}
