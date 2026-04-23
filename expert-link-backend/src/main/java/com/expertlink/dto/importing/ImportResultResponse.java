package com.expertlink.dto.importing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultResponse {
    private int total;
    private int success;
    private int failed;
    private int skipped;
    @Builder.Default
    private List<ImportErrorRow> errors = new ArrayList<>();
}
