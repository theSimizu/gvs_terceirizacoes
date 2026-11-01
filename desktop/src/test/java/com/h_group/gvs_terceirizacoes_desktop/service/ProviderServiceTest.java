package com.h_group.gvs_terceirizacoes_desktop.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiException;
import com.h_group.gvs_terceirizacoes_desktop.api.ProviderClient;
import com.h_group.gvs_terceirizacoes_desktop.dto.CreateProviderRequest;
import com.h_group.gvs_terceirizacoes_desktop.dto.ProviderDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.UpdateProviderRequest;
import feign.Request;
import feign.RetryableException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ProviderServiceTest {

	private final ProviderClient providerClient = mock(ProviderClient.class);

	private final ProviderService providerService = new ProviderService(providerClient);

	@Test
	void returnsProviders() {
		ProviderDTO provider = new ProviderDTO(UUID.randomUUID(), "Maria Silva", "123.456.789-01",
				"Elétrica", "maria@exemplo.com", "(11) 91234-5678", true);
		when(providerClient.findAll()).thenReturn(List.of(provider));

		List<ProviderDTO> providers = providerService.findAll();

		assertEquals(1, providers.size());
		assertEquals("Maria Silva", providers.get(0).name());
		assertTrue(providers.get(0).available());
	}

	@Test
	void translatesTaxIdConflict() {
		when(providerClient.create(any()))
				.thenThrow(new ApiException("Tax ID already registered", 409));

		ApiException exception = assertThrows(ApiException.class,
				() -> providerService.create(createRequest()));

		assertEquals(409, exception.getStatus());
		assertEquals("Já existe um prestador com este CPF/CNPJ.", exception.getMessage());
	}

	@Test
	void translatesTaxIdConflictOnUpdate() {
		when(providerClient.update(any(), any()))
				.thenThrow(new ApiException("Tax ID already belongs to another provider", 409));

		ApiException exception = assertThrows(ApiException.class,
				() -> providerService.update(UUID.randomUUID(), updateRequest()));

		assertEquals(409, exception.getStatus());
		assertEquals("Já existe um prestador com este CPF/CNPJ.", exception.getMessage());
	}

	@Test
	void translatesDeleteConflict() {
		doThrow(new ApiException("Data integrity violation", 409))
				.when(providerClient).delete(any());

		ApiException exception = assertThrows(ApiException.class,
				() -> providerService.delete(UUID.randomUUID()));

		assertEquals(409, exception.getStatus());
		assertTrue(exception.getMessage().contains("contratos vinculados"));
	}

	@Test
	void translatesNotFound() {
		when(providerClient.update(any(), any()))
				.thenThrow(new ApiException("Provider not found", 404));

		ApiException exception = assertThrows(ApiException.class,
				() -> providerService.update(UUID.randomUUID(), updateRequest()));

		assertEquals(404, exception.getStatus());
		assertEquals("Prestador não encontrado. Atualize a lista.", exception.getMessage());
	}

	@Test
	void setAvailableSendsOnlyAvailability() {
		UUID id = UUID.randomUUID();
		when(providerClient.update(any(), any())).thenReturn(provider(true));

		providerService.setAvailable(id, false);

		ArgumentCaptor<UpdateProviderRequest> captor = ArgumentCaptor.forClass(UpdateProviderRequest.class);
		verify(providerClient).update(org.mockito.ArgumentMatchers.eq(id), captor.capture());
		UpdateProviderRequest request = captor.getValue();
		assertNull(request.name());
		assertNull(request.taxId());
		assertNull(request.specialty());
		assertNull(request.email());
		assertNull(request.phone());
		assertFalse(request.available());
	}

	@Test
	void keepsUnauthorizedStatus() {
		when(providerClient.findAll()).thenThrow(new ApiException("E-mail ou senha incorretos.", 401));

		ApiException exception = assertThrows(ApiException.class, providerService::findAll);

		assertEquals(401, exception.getStatus());
	}

	@Test
	void translatesConnectionFailure() {
		Request request = Request.create(Request.HttpMethod.GET, "http://localhost/api/prestadores",
				Map.of(), null, StandardCharsets.UTF_8);
		RetryableException failure = new RetryableException(-1, "Connection refused",
				Request.HttpMethod.GET, (Long) null, request);
		when(providerClient.findAll()).thenThrow(failure);

		ApiException exception = assertThrows(ApiException.class, providerService::findAll);

		assertEquals(0, exception.getStatus());
		assertTrue(exception.getMessage().startsWith("Não foi possível conectar"));
	}

	private ProviderDTO provider(boolean available) {
		return new ProviderDTO(UUID.randomUUID(), "Maria Silva", "123.456.789-01", "Elétrica",
				"maria@exemplo.com", "(11) 91234-5678", available);
	}

	private CreateProviderRequest createRequest() {
		return new CreateProviderRequest("Maria Silva", "123.456.789-01", "Elétrica",
				"maria@exemplo.com", "(11) 91234-5678", true);
	}

	private UpdateProviderRequest updateRequest() {
		return new UpdateProviderRequest("Maria Silva", "123.456.789-01", "Elétrica",
				"maria@exemplo.com", "(11) 91234-5678", true);
	}

}
