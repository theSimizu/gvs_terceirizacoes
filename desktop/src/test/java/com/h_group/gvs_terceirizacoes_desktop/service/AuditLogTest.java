package com.h_group.gvs_terceirizacoes_desktop.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiException;
import com.h_group.gvs_terceirizacoes_desktop.api.HistoryClient;
import com.h_group.gvs_terceirizacoes_desktop.dto.CreateHistoryRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AuditLogTest {

	private final HistoryClient historyClient = mock(HistoryClient.class);

	private final AuditLog auditLog = new AuditLog(historyClient);

	@Test
	void recordsEntry() {
		Map<String, Object> details = new LinkedHashMap<>();
		details.put("legalName", "Acme");
		details.put("status", "active");

		auditLog.record(AuditLog.CREATE, AuditLog.CLIENT, "client-id", details);

		ArgumentCaptor<CreateHistoryRequest> captor = ArgumentCaptor.forClass(CreateHistoryRequest.class);
		verify(historyClient).create(captor.capture());
		CreateHistoryRequest request = captor.getValue();
		assertEquals(AuditLog.CREATE, request.action());
		assertEquals(AuditLog.CLIENT, request.entity());
		assertEquals("client-id", request.documentId());
		assertEquals(details, request.details());
		assertNull(request.date());
	}

	@Test
	void acceptsMissingDetails() {
		auditLog.record(AuditLog.DELETE, AuditLog.PROVIDER, "provider-id", null);

		ArgumentCaptor<CreateHistoryRequest> captor = ArgumentCaptor.forClass(CreateHistoryRequest.class);
		verify(historyClient).create(captor.capture());
		assertNull(captor.getValue().details());
	}

	@Test
	void swallowsApiFailures() {
		when(historyClient.create(any())).thenThrow(new ApiException("History unavailable", 500));

		assertDoesNotThrow(() -> auditLog.record(AuditLog.UPDATE, AuditLog.CONTRACT, "id", Map.of()));
	}

	@Test
	void swallowsUnexpectedFailures() {
		when(historyClient.create(any())).thenThrow(new IllegalStateException("boom"));

		assertDoesNotThrow(() -> auditLog.record(AuditLog.STATUS_CHANGE, AuditLog.SERVICE_ORDER,
				"id", Map.of("status", "completed")));
	}

	@Test
	void doesNotMutateDetails() {
		Map<String, Object> details = Map.of("status", "closed");

		auditLog.record(AuditLog.STATUS_CHANGE, AuditLog.CONTRACT, "contract-id", details);

		ArgumentCaptor<CreateHistoryRequest> captor = ArgumentCaptor.forClass(CreateHistoryRequest.class);
		verify(historyClient).create(captor.capture());
		assertEquals(Map.of("status", "closed"), captor.getValue().details());
	}

	@Test
	void neverInteractsWithClientWhenNotRecording() {
		verifyNoInteractions(historyClient);
	}

}
