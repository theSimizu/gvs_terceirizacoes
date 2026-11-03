package com.h_group.gvs_terceirizacoes_desktop.service;

import com.h_group.gvs_terceirizacoes_desktop.api.HistoryClient;
import com.h_group.gvs_terceirizacoes_desktop.dto.CreateHistoryRequest;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Solicita a gravação de eventos após operações realizadas pelo desktop.
 * Falhas no histórico não interrompem a operação principal. Este mecanismo é
 * auxiliar e não garante auditoria completa das alterações da API.
 */
@Component
@RequiredArgsConstructor
public class AuditLog {
	public static final String CREATE = "create";
	public static final String UPDATE = "update";
	public static final String DELETE = "delete";
	public static final String STATUS_CHANGE = "status_change";
	public static final String CLIENT = "Client";
	public static final String PROVIDER = "Provider";
	public static final String CONTRACT = "Contract";
	public static final String SERVICE_ORDER = "ServiceOrder";

	private final HistoryClient historyClient;

	/**
	 * Solicita um evento e tolera falhas para não interromper a operação principal.
	 * @param action ação registrada
	 * @param entity tipo de entidade
	 * @param documentId identificador do registro relacionado
	 * @param details detalhes auxiliares, quando disponíveis
	 */
	public void record(String action, String entity, String documentId, Map<String, Object> details) {
		try {
			historyClient.create(new CreateHistoryRequest(action, entity, documentId, details, null));
		} catch (RuntimeException ignored) {
			// Auditoria nunca deve interromper a operação principal.
		}
	}

}
