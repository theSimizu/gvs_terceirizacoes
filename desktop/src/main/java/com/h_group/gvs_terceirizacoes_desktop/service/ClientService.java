package com.h_group.gvs_terceirizacoes_desktop.service;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiException;
import com.h_group.gvs_terceirizacoes_desktop.api.ClientClient;
import com.h_group.gvs_terceirizacoes_desktop.dto.ClientDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.CreateClientRequest;
import com.h_group.gvs_terceirizacoes_desktop.dto.UpdateClientRequest;
import feign.FeignException;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Encaminha operações de clientes pelo Feign e traduz falhas para a interface.
 * Mantém os detalhes de validação e distingue documento duplicado de conflito de vínculo.
 */
@Service
@RequiredArgsConstructor
public class ClientService {
	private static final String CONNECTION_ERROR = "Não foi possível conectar ao servidor. Verifique sua conexão.";

	private final ClientClient clientClient;

	public List<ClientDTO> findAll() {
		try {
			return clientClient.findAll();
		} catch (RuntimeException exception) {
			throw translate(exception, "Não foi possível carregar os clientes.");
		}
	}

	public ClientDTO create(CreateClientRequest request) {
		try {
			return clientClient.create(request);
		} catch (RuntimeException exception) {
			throw translate(exception, "Não foi possível cadastrar o cliente.");
		}
	}

	public ClientDTO update(UUID id, UpdateClientRequest request) {
		try {
			return clientClient.update(id, request);
		} catch (RuntimeException exception) {
			throw translate(exception, "Não foi possível atualizar o cliente.");
		}
	}

	public void delete(UUID id) {
		try {
			clientClient.delete(id);
		} catch (RuntimeException exception) {
			throw translate(exception, "Não foi possível excluir o cliente.");
		}
	}

	private RuntimeException translate(RuntimeException exception, String fallback) {
		if (exception instanceof ApiException apiException) {
			int status = apiException.getStatus();
			if (status == 409) {
				String detail = apiException.getMessage() == null ? ""
						: apiException.getMessage().toLowerCase(Locale.ROOT);
				if (detail.contains("cnpj")) {
					return new ApiException("Já existe um cliente com este CNPJ.", 409);
				}
				return new ApiException(
						"Não é possível concluir: existem contratos vinculados a este cliente.", 409);
			}
			if (status == 404) {
				return new ApiException("Cliente não encontrado. Atualize a lista.", 404);
			}
			if (status == 400) {
				if (apiException.hasValidationDetails()) {
					return apiException;
				}
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
