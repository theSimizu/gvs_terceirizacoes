package com.h_group.gvs_terceirizacoes_desktop.ui;

import com.h_group.gvs_terceirizacoes_desktop.dto.CreateServiceOrderRequest;
import com.h_group.gvs_terceirizacoes_desktop.dto.UpdateServiceOrderRequest;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public record ServiceOrderForm(
		UUID contractId,
		String description,
		LocalDate executionDate,
		String status
) {

	public CreateServiceOrderRequest toCreateRequest() {
		return new CreateServiceOrderRequest(contractId, description, executionDate, status);
	}

	public UpdateServiceOrderRequest toUpdateRequest() {
		return new UpdateServiceOrderRequest(contractId, description, executionDate, status);
	}

	public Map<String, Object> toDetails() {
		Map<String, Object> details = new LinkedHashMap<>();
		details.put("contractId", contractId);
		details.put("description", description);
		details.put("executionDate", executionDate);
		details.put("status", status);
		return details;
	}

}
