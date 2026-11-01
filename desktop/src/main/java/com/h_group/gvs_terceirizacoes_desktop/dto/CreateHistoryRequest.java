package com.h_group.gvs_terceirizacoes_desktop.dto;

import java.time.Instant;
import java.util.Map;

public record CreateHistoryRequest(
		String action,
		String entity,
		String documentId,
		Map<String, Object> details,
		Instant date
) {
}
