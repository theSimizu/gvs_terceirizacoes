package com.h_group.gvs_terceirizacoes_desktop.ui;

import com.h_group.gvs_terceirizacoes_desktop.dto.CreateProviderRequest;
import com.h_group.gvs_terceirizacoes_desktop.dto.UpdateProviderRequest;
import java.util.LinkedHashMap;
import java.util.Map;

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

	public Map<String, Object> toDetails() {
		Map<String, Object> details = new LinkedHashMap<>();
		details.put("name", name);
		details.put("taxId", taxId);
		details.put("specialty", specialty);
		details.put("email", email);
		details.put("phone", phone);
		details.put("available", available);
		return details;
	}

}
