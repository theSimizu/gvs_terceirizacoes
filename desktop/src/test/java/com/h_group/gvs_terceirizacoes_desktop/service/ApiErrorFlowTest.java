package com.h_group.gvs_terceirizacoes_desktop.service;

import com.h_group.gvs_terceirizacoes_desktop.api.*;
import com.h_group.gvs_terceirizacoes_desktop.dto.*;
import com.h_group.gvs_terceirizacoes_desktop.session.*;
import com.sun.net.httpserver.HttpServer;
import feign.Feign;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiErrorFlowTest {

	private final JsonMapper mapper = JsonMapper.builder().build();
	private HttpServer server;
	private volatile int status;
	private volatile String body;
	private String baseUrl;

	@BeforeEach
	void startServer() throws Exception {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
		server.createContext("/", exchange -> {
			exchange.getRequestBody().readAllBytes();
			byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().set("Content-Type","application/json; charset=utf-8");
			exchange.sendResponseHeaders(status,bytes.length);
			try (var output = exchange.getResponseBody()) {
				output.write(bytes);
			} finally {
				exchange.close();
			}
		});
		server.start();
		baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
	}

	@AfterEach
	void stopServer() {
		if (server != null) {
			server.stop(0);
		}
	}

	@Test
	void realHttpCnpjConflictReachesUserAsDuplicateInsteadOfLinkedContract() {
		respond(409,"{\"status\":\"error\",\"error\":\"CNPJ already registered\"}");
		var service = new ClientService(client(ClientClient.class));
		ApiException error = assertThrows(ApiException.class, () -> service.create(clientRequest()));
		assertEquals(409,error.getStatus());
		assertEquals("Já existe um cliente com este CNPJ.",error.getMessage());
	}

	@Test
	void realHttpProviderConflictKeepsCorrectCause() {
		respond(409,"{\"error\":\"Tax ID already belongs to another provider\"}");
		var service = new ProviderService(client(ProviderClient.class));
		ApiException error = assertThrows(ApiException.class, () -> service.create(providerRequest()));
		assertEquals("Já existe um prestador com este CPF/CNPJ.",error.getMessage());
	}

	@Test
	void contractErrorsIdentifyMissingClientOrProvider() {
		var service = new ContractService(client(ContractClient.class));
		respond(404,"{\"error\":\"Client not found\"}");
		assertEquals("Cliente não encontrado. Atualize a lista.",assertThrows(ApiException.class, () -> service.create(contractRequest())).getMessage());
		respond(404,"{\"error\":\"Provider not found\"}");
		assertEquals("Prestador não encontrado. Atualize a lista.",assertThrows(ApiException.class, () -> service.create(contractRequest())).getMessage());
	}

	@Test
	void orderErrorsIdentifyMissingContract() {
		respond(404,"{\"error\":\"Contract not found\"}");
		var service = new ServiceOrderService(client(ServiceOrderClient.class));
		assertEquals("Contrato não encontrado. Atualize a lista.",assertThrows(ApiException.class, () -> service.create(orderRequest())).getMessage());
	}

	@Test
	void validationDetailsSurviveEveryServiceLayer() {
		respond(400,"{\"error\":\"Validation failed\",\"details\":[{\"field\":\"email\",\"message\":\"must be a well-formed email address\"}]}");
		Runnable[] actions = {
			() -> new ClientService(client(ClientClient.class)).create(clientRequest()),
			() -> new ProviderService(client(ProviderClient.class)).create(providerRequest()),
			() -> new ContractService(client(ContractClient.class)).create(contractRequest()),
			() -> new ServiceOrderService(client(ServiceOrderClient.class)).create(orderRequest()),
			() -> new HistoryService(client(HistoryClient.class)).findAll(),
			() -> authService().register("Teste","invalid","test-password")
		};
		for (Runnable action : actions) {
			ApiException error = assertThrows(ApiException.class, action::run);
			assertEquals(400,error.getStatus());
			assertTrue(error.hasValidationDetails());
			assertTrue(error.getMessage().contains("E-mail: Informe um e-mail válido."));
		}
	}

	@Test
	void registrationConflictIsShownInPortuguese() {
		respond(409,"{\"error\":\"E-mail already registered\"}");
		ApiException error = assertThrows(ApiException.class, () -> authService().register("Teste","teste@example.com","test-password"));
		assertEquals("Este e-mail já está cadastrado.",error.getMessage());
	}

	@Test
	void malformedValidationResponseKeepsExistingFriendlyFallback() {
		respond(400,"<html>Bad gateway</html>");
		ApiException error = assertThrows(ApiException.class, () -> new ClientService(client(ClientClient.class)).create(clientRequest()));
		assertEquals("Verifique os dados informados.",error.getMessage());
	}

	private void respond(int status, String body) {
		this.status = status;
		this.body = body;
	}

	private <T> T client(Class<T> type) {
		return Feign.builder().contract(new SpringMvcContract())
				.encoder((object, bodyType, template) -> template.body(mapper.writeValueAsString(object)))
				.errorDecoder(new ApiErrorDecoder(mapper)).target(type,baseUrl);
	}

	private AuthService authService() {
		return new AuthService(client(AuthClient.class),new AuthSession(),mock(TokenStore.class),mock(JwtExpiry.class));
	}

	private CreateClientRequest clientRequest() {
		return new CreateClientRequest("Teste","12345678000190","teste@example.com","11999999999",null,"active");
	}

	private CreateProviderRequest providerRequest() {
		return new CreateProviderRequest("Teste","12345678901","Limpeza","teste@example.com","11999999999",true);
	}

	private CreateContractRequest contractRequest() {
		return new CreateContractRequest(UUID.randomUUID(),UUID.randomUUID(),LocalDate.of(2026,11,2),null,BigDecimal.TEN,"active");
	}

	private CreateServiceOrderRequest orderRequest() {
		return new CreateServiceOrderRequest(UUID.randomUUID(),"Teste",LocalDate.of(2026,11,2),"pending");
	}
}
