package com.h_group.gvs_terceirizacoes_desktop.service;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiException;
import com.h_group.gvs_terceirizacoes_desktop.api.ProviderClient;
import com.h_group.gvs_terceirizacoes_desktop.dto.CreateProviderRequest;
import com.h_group.gvs_terceirizacoes_desktop.dto.ProviderDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.UpdateProviderRequest;
import feign.FeignException;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProviderService {
	private static final String CONNECTION_ERROR = "Não foi possível conectar ao servidor. Verifique sua conexão.";

	private final ProviderClient providerClient;

	public List<ProviderDTO> findAll() {
		try {
			return providerClient.findAll();
		} catch (RuntimeException exception) {
			throw translate(exception, "Não foi possível carregar os prestadores.");
		}
	}

	public ProviderDTO create(CreateProviderRequest request) {
		try {
			return providerClient.create(request);
		} catch (RuntimeException exception) {
			throw translate(exception, "Não foi possível cadastrar o prestador.");
		}
	}

	public ProviderDTO update(UUID id, UpdateProviderRequest request) {
		try {
			return providerClient.update(id, request);
		} catch (RuntimeException exception) {
			throw translate(exception, "Não foi possível atualizar o prestador.");
		}
	}

	public ProviderDTO setAvailable(UUID id, boolean available) {
		return update(id, new UpdateProviderRequest(null, null, null, null, null, available));
	}

	public void delete(UUID id) {
		try {
			providerClient.delete(id);
		} catch (RuntimeException exception) {
			throw translate(exception, "Não foi possível excluir o prestador.");
		}
	}

	private RuntimeException translate(RuntimeException exception, String fallback) {
		if (exception instanceof ApiException apiException) {
			int status = apiException.getStatus();
			if (status == 409) {
				String detail = apiException.getMessage() == null ? ""
						: apiException.getMessage().toLowerCase(Locale.ROOT);
				if (detail.contains("tax id")) {
					return new ApiException("Já existe um prestador com este CPF/CNPJ.", 409);
				}
				return new ApiException(
						"Não é possível concluir: existem contratos vinculados a este prestador.", 409);
			}
			if (status == 404) {
				return new ApiException("Prestador não encontrado. Atualize a lista.", 404);
			}
			if (status == 400) {
				return new ApiException("Verifique os dados informados.", 400);
			}
			return apiException;
		}
		if (exception instanceof FeignException feignException && feignException.status() < 0) {
			return new ApiException(CONNECTION_ERROR, exception);
		}
		return new ApiException(fallback, exception);
	}

}
