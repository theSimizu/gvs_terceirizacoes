package com.h_group.gvs_terceirizacoes_desktop.service;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiException;
import com.h_group.gvs_terceirizacoes_desktop.api.ServiceOrderClient;
import com.h_group.gvs_terceirizacoes_desktop.dto.CreateServiceOrderRequest;
import com.h_group.gvs_terceirizacoes_desktop.dto.ServiceOrderDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.UpdateServiceOrderRequest;
import feign.FeignException;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServiceOrderService {
	private static final String CONNECTION_ERROR = "Não foi possível conectar ao servidor. Verifique sua conexão.";

	private final ServiceOrderClient serviceOrderClient;

	public List<ServiceOrderDTO> findAll() {
		try {
			return serviceOrderClient.findAll();
		} catch (RuntimeException exception) {
			throw translate(exception, "Não foi possível carregar as ordens de serviço.");
		}
	}

	public ServiceOrderDTO create(CreateServiceOrderRequest request) {
		try {
			return serviceOrderClient.create(request);
		} catch (RuntimeException exception) {
			throw translate(exception, "Não foi possível registrar a ordem de serviço.");
		}
	}

	public ServiceOrderDTO update(UUID id, UpdateServiceOrderRequest request) {
		try {
			return serviceOrderClient.update(id, request);
		} catch (RuntimeException exception) {
			throw translate(exception, "Não foi possível atualizar a ordem de serviço.");
		}
	}

	public ServiceOrderDTO setStatus(UUID id, String status) {
		return update(id, new UpdateServiceOrderRequest(null, null, null, status));
	}

	public void delete(UUID id) {
		try {
			serviceOrderClient.delete(id);
		} catch (RuntimeException exception) {
			throw translate(exception, "Não foi possível excluir a ordem de serviço.");
		}
	}

	private RuntimeException translate(RuntimeException exception, String fallback) {
		if (exception instanceof ApiException apiException) {
			int status = apiException.getStatus();
			String detail = apiException.getMessage() == null ? ""
					: apiException.getMessage().toLowerCase(Locale.ROOT);
			if (status == 404) {
				if (detail.contains("contract")) {
					return new ApiException("Contrato não encontrado. Atualize a lista.", 404);
				}
				return new ApiException("Ordem de serviço não encontrada. Atualize a lista.", 404);
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
