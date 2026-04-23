package com.expertlink.controller;

import com.expertlink.dto.ApiResponse;
import com.expertlink.dto.importing.ImportResultResponse;
import com.expertlink.service.DomainService;
import com.expertlink.service.ExpertService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DomainControllerTest {

    private final DomainService domainService = mock(DomainService.class);
    private final ExpertService expertService = mock(ExpertService.class);
    private final DomainController controller = new DomainController(domainService, expertService);

    @Test
    void downloadImportTemplateReturnsExcelBytes() {
        byte[] bytes = new byte[]{9, 8, 7};
        when(domainService.buildImportTemplate()).thenReturn(bytes);

        ResponseEntity<byte[]> response = controller.downloadImportTemplate();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(bytes, response.getBody());
    }

    @Test
    void importDomainsWrapsResultInApiResponse() {
        var file = new MockMultipartFile("file", "domains.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[]{1});
        ImportResultResponse result = ImportResultResponse.builder()
                .total(1).success(1).failed(0).skipped(0).build();
        when(domainService.importDomains(any())).thenReturn(result);

        ResponseEntity<ApiResponse<ImportResultResponse>> response = controller.importDomains(file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().getSuccess());
    }
}
