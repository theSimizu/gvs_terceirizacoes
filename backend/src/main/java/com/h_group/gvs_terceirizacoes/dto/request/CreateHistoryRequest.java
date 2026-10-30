package com.h_group.gvs_terceirizacoes.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.Map;

public record CreateHistoryRequest(
        @NotBlank String action,
        @NotBlank String entity,
        String documentId,
        Map<String, Object> details,
        Instant date
) {
}
