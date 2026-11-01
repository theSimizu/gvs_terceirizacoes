package com.h_group.gvs_terceirizacoes_desktop.ui;

import com.h_group.gvs_terceirizacoes_desktop.dto.CreateProviderRequest;
import com.h_group.gvs_terceirizacoes_desktop.dto.UpdateProviderRequest;

public record ProviderForm(
		String name,
		String taxId,
		String specialty,
		String email,
		String phone,
		boolean available
) {

	public CreateProviderRequest toCreateRequest() {
		return new CreateProviderRequest(name, taxId, specialty, email, phone, available);
	}

	public UpdateProviderRequest toUpdateRequest() {
		return new UpdateProviderRequest(name, taxId, specialty, email, phone, available);
	}

}
