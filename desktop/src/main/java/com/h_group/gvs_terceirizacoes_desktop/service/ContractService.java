package com.h_group.gvs_terceirizacoes_desktop.service;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiException;
import com.h_group.gvs_terceirizacoes_desktop.api.ContractClient;
import com.h_group.gvs_terceirizacoes_desktop.dto.ContractDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.CreateContractRequest;
import com.h_group.gvs_terceirizacoes_desktop.dto.UpdateContractRequest;
import feign.FeignException;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Encaminha operações de contratos e identifica vínculos ausentes nas mensagens.
 * Preserva as validações detalhadas recebidas da API.
 */
@Service
@RequiredArgsConstructor
public class ContractService {
	private static final String ACTIVE = "active";
	private static final String CLOSED = "closed";
	private static final String CONNECTION_ERROR = "Não foi possível conectar ao servidor. Verifique sua conexão.";

	private final ContractClient contractClient;

	public List<ContractDTO> findAll() {
		try {
			return contractClient.findAll();
		} catch (RuntimeException exception) {
			throw translate(exception, "Não foi possível carregar os contratos.");
		}
	}

	public ContractDTO create(CreateContractRequest request) {
		try {
			return contractClient.create(request);
		} catch (RuntimeException exception) {
			throw translate(exception, "Não foi possível cadastrar o contrato.");
		}
	}

	public ContractDTO update(UUID id, UpdateContractRequest request) {
		try {
			return contractClient.update(id, request);
		} catch (RuntimeException exception) {
			throw translate(exception, "Não foi possível atualizar o contrato.");
		}
	}

	public ContractDTO setStatus(UUID id, String status) {
		return update(id, new UpdateContractRequest(null, null, null, null, null, status));
	}

	public ContractDTO toggleStatus(UUID id, String currentStatus) {
		return setStatus(id, CLOSED.equalsIgnoreCase(currentStatus) ? ACTIVE : CLOSED);
	}

	public void delete(UUID id) {
		try {
			contractClient.delete(id);
		} catch (RuntimeException exception) {
			throw translate(exception, "Não foi possível excluir o contrato.");
		}
	}

	private RuntimeException translate(RuntimeException exception, String fallback) {
		if (exception instanceof ApiException apiException) {
			int status = apiException.getStatus();
			String detail = apiException.getMessage() == null ? ""
					: apiException.getMessage().toLowerCase(Locale.ROOT);
			if (status == 409) {
				return new ApiException(
						"Não é possível excluir: existem ordens de serviço vinculadas a este contrato.", 409);
			}
			if (status == 404) {
				if (detail.contains("client")) {
					return new ApiException("Cliente não encontrado. Atualize a lista.", 404);
				}
				if (detail.contains("provider")) {
					return new ApiException("Prestador não encontrado. Atualize a lista.", 404);
				}
				return new ApiException("Contrato não encontrado. Atualize a lista.", 404);
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
