package com.h_group.gvs_terceirizacoes_desktop.service;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiException;
import com.h_group.gvs_terceirizacoes_desktop.api.HistoryClient;
import com.h_group.gvs_terceirizacoes_desktop.dto.HistoryDTO;
import feign.FeignException;
import lombok.RequiredArgsConstructor;

import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Obtém o histórico auxiliar e adapta falhas de conexão e validação.
 * A consulta não cria automaticamente uma auditoria das operações do servidor.
 */
@Service
@RequiredArgsConstructor
public class HistoryService {
	private static final String CONNECTION_ERROR = "Não foi possível conectar ao servidor. Verifique sua conexão.";

	private final HistoryClient historyClient;

	public List<HistoryDTO> findAll() {
		try {
			return historyClient.findAll();
		} catch (RuntimeException exception) {
			throw translate(exception);
		}
	}

	private RuntimeException translate(RuntimeException exception) {
		if (exception instanceof ApiException apiException) {
			if (apiException.getStatus() == 400) {
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
		return new ApiException("Não foi possível carregar a auditoria.", exception);
	}

}
