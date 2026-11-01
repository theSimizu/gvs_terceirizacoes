package com.h_group.gvs_terceirizacoes_desktop.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiException;
import com.h_group.gvs_terceirizacoes_desktop.api.ServiceOrderClient;
import com.h_group.gvs_terceirizacoes_desktop.dto.ClientDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.ContractDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.CreateServiceOrderRequest;
import com.h_group.gvs_terceirizacoes_desktop.dto.ProviderDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.ServiceOrderDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.UpdateServiceOrderRequest;
import feign.Request;
import feign.RetryableException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ServiceOrderServiceTest {

	private final ServiceOrderClient serviceOrderClient = mock(ServiceOrderClient.class);

	private final ServiceOrderService serviceOrderService = new ServiceOrderService(serviceOrderClient);

	@Test
	void returnsServiceOrders() {
		when(serviceOrderClient.findAll()).thenReturn(List.of(serviceOrder("pending")));

		List<ServiceOrderDTO> orders = serviceOrderService.findAll();

		assertEquals(1, orders.size());
		assertEquals("Acme", orders.get(0).contract().client().legalName());
		assertEquals("Maria Silva", orders.get(0).contract().provider().name());
	}

	@Test
	void createsServiceOrder() {
		when(serviceOrderClient.create(any())).thenReturn(serviceOrder("pending"));

		ServiceOrderDTO created = serviceOrderService.create(createRequest());

		assertEquals("pending", created.status());
	}

	@Test
	void translatesContractNotFound() {
		when(serviceOrderClient.create(any())).thenThrow(new ApiException("Contract not found", 404));

		ApiException exception = assertThrows(ApiException.class,
				() -> serviceOrderService.create(createRequest()));

		assertEquals(404, exception.getStatus());
		assertEquals("Contrato não encontrado. Atualize a lista.", exception.getMessage());
	}

	@Test
	void translatesServiceOrderNotFound() {
		when(serviceOrderClient.update(any(), any()))
				.thenThrow(new ApiException("Service order not found", 404));

		ApiException exception = assertThrows(ApiException.class,
				() -> serviceOrderService.update(UUID.randomUUID(), updateRequest()));

		assertEquals(404, exception.getStatus());
		assertEquals("Ordem de serviço não encontrada. Atualize a lista.", exception.getMessage());
	}

	@Test
	void translatesValidationFailure() {
		when(serviceOrderClient.create(any())).thenThrow(new ApiException("Validation failed", 400));

		ApiException exception = assertThrows(ApiException.class,
				() -> serviceOrderService.create(createRequest()));

		assertEquals(400, exception.getStatus());
		assertEquals("Verifique os dados informados.", exception.getMessage());
	}

	@Test
	void setStatusSendsOnlyStatus() {
		UUID id = UUID.randomUUID();
		when(serviceOrderClient.update(any(), any())).thenReturn(serviceOrder("completed"));

		serviceOrderService.setStatus(id, "completed");

		ArgumentCaptor<UpdateServiceOrderRequest> captor = ArgumentCaptor.forClass(UpdateServiceOrderRequest.class);
		verify(serviceOrderClient).update(eq(id), captor.capture());
		UpdateServiceOrderRequest request = captor.getValue();
		assertEquals("completed", request.status());
		assertNull(request.contractId());
		assertNull(request.description());
		assertNull(request.executionDate());
	}

	@Test
	void deletesServiceOrder() {
		UUID id = UUID.randomUUID();

		serviceOrderService.delete(id);

		verify(serviceOrderClient).delete(id);
	}

	@Test
	void keepsUnauthorizedStatus() {
		when(serviceOrderClient.findAll()).thenThrow(new ApiException("Não autorizado", 401));

		ApiException exception = assertThrows(ApiException.class, serviceOrderService::findAll);

		assertEquals(401, exception.getStatus());
	}

	@Test
	void translatesConnectionFailure() {
		Request request = Request.create(Request.HttpMethod.GET, "http://localhost/api/ordens-servico",
				Map.of(), null, StandardCharsets.UTF_8);
		RetryableException failure = new RetryableException(-1, "Connection refused",
				Request.HttpMethod.GET, (Long) null, request);
		when(serviceOrderClient.findAll()).thenThrow(failure);

		ApiException exception = assertThrows(ApiException.class, serviceOrderService::findAll);

		assertEquals(0, exception.getStatus());
		assertTrue(exception.getMessage().startsWith("Não foi possível conectar"));
	}

	private ServiceOrderDTO serviceOrder(String status) {
		ClientDTO client = new ClientDTO(UUID.randomUUID(), "Acme", "12.345.678/0001-90",
				"contato@acme.com", "(11) 91234-5678", null, "active");
		ProviderDTO provider = new ProviderDTO(UUID.randomUUID(), "Maria Silva", "123.456.789-01",
				"Elétrica", "maria@exemplo.com", "(11) 91234-5678", true);
		ContractDTO contract = new ContractDTO(UUID.randomUUID(), client.id(), client, provider.id(), provider,
				LocalDate.of(2026, 1, 10), LocalDate.of(2026, 7, 10), new BigDecimal("1500.00"), "active");
		return new ServiceOrderDTO(UUID.randomUUID(), contract.id(), contract, "Troca de iluminação",
				LocalDate.of(2026, 2, 20), status, Instant.parse("2026-02-01T12:00:00Z"),
				Instant.parse("2026-02-02T12:00:00Z"));
	}

	private CreateServiceOrderRequest createRequest() {
		return new CreateServiceOrderRequest(UUID.randomUUID(), "Troca de iluminação",
				LocalDate.of(2026, 2, 20), "pending");
	}

	private UpdateServiceOrderRequest updateRequest() {
		return new UpdateServiceOrderRequest(UUID.randomUUID(), "Troca de iluminação",
				LocalDate.of(2026, 2, 20), "pending");
	}

}
