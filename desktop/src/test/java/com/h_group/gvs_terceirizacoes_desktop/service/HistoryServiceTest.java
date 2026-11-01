package com.h_group.gvs_terceirizacoes_desktop.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiException;
import com.h_group.gvs_terceirizacoes_desktop.api.HistoryClient;
import com.h_group.gvs_terceirizacoes_desktop.dto.HistoryDTO;
import feign.Request;
import feign.RetryableException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class HistoryServiceTest {

	private final HistoryClient historyClient = mock(HistoryClient.class);

	private final HistoryService historyService = new HistoryService(historyClient);

	@Test
	void returnsEntries() {
		HistoryDTO entry = new HistoryDTO(UUID.randomUUID(), "create", "Client",
				UUID.randomUUID().toString(), Map.of("legalName", "Acme"), Instant.now());
		when(historyClient.findAll()).thenReturn(List.of(entry));

		List<HistoryDTO> entries = historyService.findAll();

		assertEquals(1, entries.size());
		assertSame(entry, entries.get(0));
	}

	@Test
	void translatesBadRequest() {
		when(historyClient.findAll()).thenThrow(new ApiException("Invalid request", 400));

		ApiException exception = assertThrows(ApiException.class, historyService::findAll);

		assertEquals(400, exception.getStatus());
		assertEquals("Verifique os dados informados.", exception.getMessage());
	}

	@Test
	void keepsUnauthorizedStatus() {
		ApiException unauthorized = new ApiException("Sessão expirada.", 401);
		when(historyClient.findAll()).thenThrow(unauthorized);

		ApiException exception = assertThrows(ApiException.class, historyService::findAll);

		assertSame(unauthorized, exception);
	}

	@Test
	void translatesConnectionFailure() {
		Request request = Request.create(Request.HttpMethod.GET, "http://localhost/api/historico",
				Map.of(), null, StandardCharsets.UTF_8);
		RetryableException failure = new RetryableException(-1, "Connection refused",
				Request.HttpMethod.GET, (Long) null, request);
		when(historyClient.findAll()).thenThrow(failure);

		ApiException exception = assertThrows(ApiException.class, historyService::findAll);

		assertEquals(0, exception.getStatus());
		assertTrue(exception.getMessage().startsWith("Não foi possível conectar"));
	}

	@Test
	void translatesUnexpectedFailure() {
		when(historyClient.findAll()).thenThrow(new IllegalStateException("boom"));

		ApiException exception = assertThrows(ApiException.class, historyService::findAll);

		assertEquals("Não foi possível carregar a auditoria.", exception.getMessage());
	}

}
