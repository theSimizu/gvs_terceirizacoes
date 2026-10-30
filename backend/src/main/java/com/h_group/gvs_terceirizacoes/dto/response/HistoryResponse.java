package com.h_group.gvs_terceirizacoes.dto.response;

import com.h_group.gvs_terceirizacoes.entity.History;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record HistoryResponse(
        UUID id,
        String action,
        String entity,
        String documentId,
        Map<String, Object> details,
        Instant date
) {

    public static HistoryResponse from(History history) {
        return new HistoryResponse(
                history.getId(),
                history.getAction(),
                history.getEntity(),
                history.getDocumentId(),
                history.getDetails(),
                history.getDate()
        );
    }
}
