package com.h_group.gvs_terceirizacoes_desktop.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiException;
import com.h_group.gvs_terceirizacoes_desktop.api.ContractClient;
import com.h_group.gvs_terceirizacoes_desktop.dto.ClientDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.ContractDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.CreateContractRequest;
import com.h_group.gvs_terceirizacoes_desktop.dto.ProviderDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.UpdateContractRequest;
import feign.Request;
import feign.RetryableException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ContractServiceTest {

	private final ContractClient contractClient = mock(ContractClient.class);

	private final ContractService contractService = new ContractService(contractClient);

	@Test
	void returnsContracts() {
		ContractDTO contract = contract("active");
		when(contractClient.findAll()).thenReturn(List.of(contract));

		List<ContractDTO> contracts = contractService.findAll();

		assertEquals(1, contracts.size());
		assertEquals("Acme", contracts.get(0).client().legalName());
		assertEquals("Maria Silva", contracts.get(0).provider().name());
	}

	@Test
	void createsContract() {
		when(contractClient.create(any())).thenReturn(contract("active"));

		ContractDTO created = contractService.create(createRequest());

		assertEquals("active", created.status());
	}

	@Test
	void translatesContractNotFound() {
		when(contractClient.update(any(), any()))
				.thenThrow(new ApiException("Contract not found", 404));

		ApiException exception = assertThrows(ApiException.class,
				() -> contractService.update(UUID.randomUUID(), updateRequest()));

		assertEquals(404, exception.getStatus());
		assertEquals("Contrato não encontrado. Atualize a lista.", exception.getMessage());
	}

	@Test
	void translatesClientNotFound() {
		when(contractClient.create(any()))
				.thenThrow(new ApiException("Client not found", 404));

		ApiException exception = assertThrows(ApiException.class,
				() -> contractService.create(createRequest()));

		assertEquals(404, exception.getStatus());
		assertEquals("Cliente não encontrado. Atualize a lista.", exception.getMessage());
	}

	@Test
	void translatesProviderNotFound() {
		when(contractClient.create(any()))
				.thenThrow(new ApiException("Provider not found", 404));

		ApiException exception = assertThrows(ApiException.class,
				() -> contractService.create(createRequest()));

		assertEquals(404, exception.getStatus());
		assertEquals("Prestador não encontrado. Atualize a lista.", exception.getMessage());
	}

	@Test
	void translatesDeleteConflict() {
		doThrow(new ApiException("Data integrity violation", 409))
				.when(contractClient).delete(any());

		ApiException exception = assertThrows(ApiException.class,
				() -> contractService.delete(UUID.randomUUID()));

		assertEquals(409, exception.getStatus());
		assertTrue(exception.getMessage().contains("ordens de serviço"));
	}

	@Test
	void toggleStatusClosesActiveContractWithOnlyStatus() {
		UUID id = UUID.randomUUID();
		when(contractClient.update(any(), any())).thenReturn(contract("closed"));

		contractService.toggleStatus(id, "active");

		ArgumentCaptor<UpdateContractRequest> captor = ArgumentCaptor.forClass(UpdateContractRequest.class);
		verify(contractClient).update(eq(id), captor.capture());
		UpdateContractRequest request = captor.getValue();
		assertEquals("closed", request.status());
		assertNull(request.clientId());
		assertNull(request.providerId());
		assertNull(request.startDate());
		assertNull(request.endDate());
		assertNull(request.agreedValue());
	}

	@Test
	void toggleStatusReopensClosedContract() {
		UUID id = UUID.randomUUID();
		when(contractClient.update(any(), any())).thenReturn(contract("active"));

		contractService.toggleStatus(id, "closed");

		ArgumentCaptor<UpdateContractRequest> captor = ArgumentCaptor.forClass(UpdateContractRequest.class);
		verify(contractClient).update(eq(id), captor.capture());
		assertEquals("active", captor.getValue().status());
	}

	@Test
	void keepsUnauthorizedStatus() {
		when(contractClient.findAll()).thenThrow(new ApiException("E-mail ou senha incorretos.", 401));

		ApiException exception = assertThrows(ApiException.class, contractService::findAll);

		assertEquals(401, exception.getStatus());
	}

	@Test
	void translatesConnectionFailure() {
		Request request = Request.create(Request.HttpMethod.GET, "http://localhost/api/contratos",
				Map.of(), null, StandardCharsets.UTF_8);
		RetryableException failure = new RetryableException(-1, "Connection refused",
				Request.HttpMethod.GET, (Long) null, request);
		when(contractClient.findAll()).thenThrow(failure);

		ApiException exception = assertThrows(ApiException.class, contractService::findAll);

		assertEquals(0, exception.getStatus());
		assertTrue(exception.getMessage().startsWith("Não foi possível conectar"));
	}

	private ContractDTO contract(String status) {
		ClientDTO client = new ClientDTO(UUID.randomUUID(), "Acme", "12.345.678/0001-90",
				"contato@acme.com", "(11) 91234-5678", null, "active");
		ProviderDTO provider = new ProviderDTO(UUID.randomUUID(), "Maria Silva", "123.456.789-01",
				"Elétrica", "maria@exemplo.com", "(11) 91234-5678", true);
		return new ContractDTO(UUID.randomUUID(), client.id(), client, provider.id(), provider,
				LocalDate.of(2026, 1, 10), LocalDate.of(2026, 7, 10), new BigDecimal("1500.00"), status);
	}

	private CreateContractRequest createRequest() {
		return new CreateContractRequest(UUID.randomUUID(), UUID.randomUUID(), LocalDate.of(2026, 1, 10),
				LocalDate.of(2026, 7, 10), new BigDecimal("1500.00"), "active");
	}

	private UpdateContractRequest updateRequest() {
		return new UpdateContractRequest(UUID.randomUUID(), UUID.randomUUID(), LocalDate.of(2026, 1, 10),
				LocalDate.of(2026, 7, 10), new BigDecimal("1500.00"), "active");
	}

}
