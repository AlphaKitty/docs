package com.expertlink.controller;

import com.expertlink.domain.Expert;
import com.expertlink.dto.ApiResponse;
import com.expertlink.security.AuthPrincipal;
import com.expertlink.dto.PaginatedResponse;
import com.expertlink.service.ExpertPrivacyService;
import com.expertlink.service.ExpertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExpertControllerTest {

    private final ExpertService expertService = mock(ExpertService.class);
    private final ExpertPrivacyService expertPrivacyService = mock(ExpertPrivacyService.class);
    private final ExpertController controller = new ExpertController(expertService, expertPrivacyService);

    @BeforeEach
    void wirePrivacyPassthrough() {
        when(expertPrivacyService.maskPage(any(), any())).thenAnswer(inv -> inv.getArgument(0));
        when(expertPrivacyService.maskForRead(any(), any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void getAllExpertsWrapsPageInApiResponse() {
        Expert expert = Expert.builder().name("Alice").email("alice@example.com").build();
        when(expertService.findAll(any())).thenReturn(new PageImpl<>(List.of(expert), PageRequest.of(0, 20), 1));

        ResponseEntity<ApiResponse<PaginatedResponse<Expert>>> response = controller.getAllExperts(PageRequest.of(0, 20), null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        assertEquals(1, response.getBody().getData().getTotalElements());
        assertEquals("Alice", response.getBody().getData().getContent().get(0).getName());
    }

    @Test
    void getExpertByIdWrapsEntityInApiResponse() {
        Expert expert = Expert.builder().name("Bob").email("bob@example.com").build();
        when(expertService.findById(1L)).thenReturn(expert);

        ResponseEntity<ApiResponse<Expert>> response = controller.getExpertById(1L, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getCode());
        assertEquals("Bob", response.getBody().getData().getName());
    }

    @Test
    void createExpertWrapsCreatedEntityInApiResponse() {
        Expert request = Expert.builder().name("Carol").email("carol@example.com").build();
        Expert created = Expert.builder().name("Carol").email("carol@example.com").build();
        when(expertService.create(eq(request), eq(99L), eq(3L), eq(Set.of(1L, 2L)), eq(Set.of(3L)), eq(11L)))
                .thenReturn(created);

        var auth = new TestingAuthenticationToken(
                new AuthPrincipal(99L, "tester"),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));

        ResponseEntity<ApiResponse<Expert>> response = controller.createExpert(
                request,
                99L,
                11L,
                3L,
                Set.of(1L, 2L),
                Set.of(3L),
                auth
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(201, response.getBody().getCode());
        assertEquals("Carol", response.getBody().getData().getName());
    }
}
