package com.h_group.gvs_terceirizacoes_desktop.ui;

import com.h_group.gvs_terceirizacoes_desktop.dto.CreateContractRequest;
import com.h_group.gvs_terceirizacoes_desktop.dto.UpdateContractRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
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

}
