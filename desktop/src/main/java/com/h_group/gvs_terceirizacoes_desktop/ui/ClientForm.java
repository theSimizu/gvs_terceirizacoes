package com.h_group.gvs_terceirizacoes_desktop.ui;

import com.h_group.gvs_terceirizacoes_desktop.dto.ClientDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.CreateClientRequest;
import com.h_group.gvs_terceirizacoes_desktop.dto.UpdateClientRequest;
import java.util.LinkedHashMap;
import java.util.Map;

public record ClientForm(
		String legalName,
		String cnpj,
		String email,
		String phone,
		String status) {

	public CreateClientRequest toCreateRequest() {
		return new CreateClientRequest(legalName, cnpj, email, phone, null, status);
	}

	public UpdateClientRequest toUpdateRequest() {
		return new UpdateClientRequest(legalName, cnpj, email, phone, null, status);
	}

	public static ClientForm from(ClientDTO client, String status) {
		return new ClientForm(client.legalName(), client.cnpj(), client.email(), client.phone(), status);
	}

	public Map<String, Object> toDetails() {
		Map<String, Object> details = new LinkedHashMap<>();
		details.put("legalName", legalName);
		details.put("cnpj", cnpj);
		details.put("email", email);
		details.put("phone", phone);
		details.put("status", status);
		return details;
	}

}
