package com.h_group.gvs_terceirizacoes_desktop.service;

import com.h_group.gvs_terceirizacoes_desktop.api.HistoryClient;
import com.h_group.gvs_terceirizacoes_desktop.dto.CreateHistoryRequest;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import org.springframework.stereotype.Component;

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

	public void record(String action, String entity, String documentId, Map<String, Object> details) {
		try {
			historyClient.create(new CreateHistoryRequest(action, entity, documentId, details, null));
		} catch (RuntimeException ignored) {
			// Auditoria nunca deve interromper a operação principal.
		}
	}

}
