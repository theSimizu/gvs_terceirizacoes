package com.h_group.gvs_terceirizacoes_desktop.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiException;
import com.h_group.gvs_terceirizacoes_desktop.api.ClientClient;
import com.h_group.gvs_terceirizacoes_desktop.dto.ClientDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.CreateClientRequest;
import com.h_group.gvs_terceirizacoes_desktop.dto.UpdateClientRequest;
import feign.Request;
import feign.RetryableException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ClientServiceTest {

	private final ClientClient clientClient = mock(ClientClient.class);

	private final ClientService clientService = new ClientService(clientClient);

	@Test
	void returnsClients() {
		ClientDTO client = new ClientDTO(UUID.randomUUID(), "Acme", "12.345.678/0001-90",
				"contato@acme.com", "(11) 91234-5678", null, "active");
		when(clientClient.findAll()).thenReturn(List.of(client));

		List<ClientDTO> clients = clientService.findAll();

		assertEquals(1, clients.size());
		assertEquals("Acme", clients.get(0).legalName());
	}

	@Test
	void translatesCnpjConflict() {
		when(clientClient.create(any())).thenThrow(new ApiException("CNPJ already registered", 409));

		ApiException exception = assertThrows(ApiException.class,
				() -> clientService.create(createRequest()));

		assertEquals(409, exception.getStatus());
		assertEquals("Já existe um cliente com este CNPJ.", exception.getMessage());
	}

	@Test
	void translatesDeleteConflict() {
		doThrow(new ApiException("Data integrity violation", 409))
				.when(clientClient).delete(any());

		ApiException exception = assertThrows(ApiException.class,
				() -> clientService.delete(UUID.randomUUID()));

		assertEquals(409, exception.getStatus());
		assertTrue(exception.getMessage().contains("contratos vinculados"));
	}

	@Test
	void translatesNotFound() {
		when(clientClient.update(any(), any()))
				.thenThrow(new ApiException("Client not found", 404));

		ApiException exception = assertThrows(ApiException.class,
				() -> clientService.update(UUID.randomUUID(), updateRequest()));

		assertEquals(404, exception.getStatus());
		assertTrue(exception.getMessage().contains("não encontrado"));
	}

	@Test
	void keepsUnauthorizedStatus() {
		when(clientClient.findAll()).thenThrow(new ApiException("E-mail ou senha incorretos.", 401));

		ApiException exception = assertThrows(ApiException.class, clientService::findAll);

		assertEquals(401, exception.getStatus());
	}

	@Test
	void translatesConnectionFailure() {
		Request request = Request.create(Request.HttpMethod.GET, "http://localhost/api/clientes",
				Map.of(), null, StandardCharsets.UTF_8);
		RetryableException failure = new RetryableException(-1, "Connection refused",
				Request.HttpMethod.GET, (Long) null, request);
		when(clientClient.findAll()).thenThrow(failure);

		ApiException exception = assertThrows(ApiException.class, clientService::findAll);

		assertEquals(0, exception.getStatus());
		assertTrue(exception.getMessage().startsWith("Não foi possível conectar"));
	}

	private CreateClientRequest createRequest() {
		return new CreateClientRequest("Acme", "12.345.678/0001-90", "contato@acme.com",
				"(11) 91234-5678", null, "active");
	}

	private UpdateClientRequest updateRequest() {
		return new UpdateClientRequest("Acme", "12.345.678/0001-90", "contato@acme.com",
				"(11) 91234-5678", null, "active");
	}

}
