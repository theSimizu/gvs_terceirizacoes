package com.h_group.gvs_terceirizacoes_desktop.ui;

import com.h_group.gvs_terceirizacoes_desktop.dto.CreateContractRequest;
import com.h_group.gvs_terceirizacoes_desktop.dto.UpdateContractRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public record ContractForm(
		UUID clientId,
		UUID providerId,
		LocalDate startDate,
		LocalDate endDate,
		BigDecimal agreedValue,
		String status
) {

	public CreateContractRequest toCreateRequest() {
		return new CreateContractRequest(clientId, providerId, startDate, endDate, agreedValue, status);
	}

	public UpdateContractRequest toUpdateRequest() {
		return new UpdateContractRequest(clientId, providerId, startDate, endDate, agreedValue, status);
	}

	public Map<String, Object> toDetails() {
		Map<String, Object> details = new LinkedHashMap<>();
		details.put("clientId", clientId);
		details.put("providerId", providerId);
		details.put("startDate", startDate);
		details.put("endDate", endDate);
		details.put("agreedValue", agreedValue);
		details.put("status", status);
		return details;
	}

}
