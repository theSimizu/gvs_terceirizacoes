package com.h_group.gvs_terceirizacoes_desktop.api;

import feign.Request;
import feign.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiErrorDecoderTest {

	private final ApiErrorDecoder decoder = new ApiErrorDecoder(JsonMapper.builder().build());

	@Test
	void readsErrorFieldFromCurrentApiAndKeepsStatus() {
		ApiException error = decode(409, "{\"status\":\"error\",\"error\":\"CNPJ already registered\"}");
		assertEquals(409, error.getStatus());
		assertEquals("CNPJ already registered", error.getMessage());
		assertFalse(error.hasValidationDetails());
	}

	@Test
	void listsAllValidationFieldsInPortugueseAndRemovesDuplicates() {
		ApiException error = decode(400, """
				{"error":"Validation failed","details":[
				  {"field":"email","message":"must be a well-formed email address"},
				  {"field":"legalName","message":"size must be between 3 and 2147483647"},
				  {"field":"email","message":"must be a well-formed email address"}
				]}
				""");
		assertEquals("Verifique os campos informados:\nE-mail: Informe um e-mail válido.\nRazão social: Use pelo menos 3 caracteres.", error.getMessage());
		assertTrue(error.hasValidationDetails());
	}

	@Test
	void translatesRequiredLengthAndAmountConstraints() {
		ApiException error = decode(400, """
				{"details":[
				  {"field":"clientId","message":"must not be null"},
				  {"field":"cnpj","message":"size must be between 14 and 18"},
				  {"field":"agreedValue","message":"must be greater than or equal to 0.00"},
				  {"field":"password","message":"size must be between 6 and 6"},
				  {"field":"description","message":"size must be between 0 and 1000"}
				]}
				""");
		assertTrue(error.getMessage().contains("Cliente: Campo obrigatório."));
		assertTrue(error.getMessage().contains("CNPJ: Use de 14 a 18 caracteres."));
		assertTrue(error.getMessage().contains("Valor acordado: Informe um valor maior ou igual a 0,00."));
		assertTrue(error.getMessage().contains("Senha: Use 6 caracteres."));
		assertTrue(error.getMessage().contains("Descrição: Use no máximo 1000 caracteres."));
	}

	@Test
	void acceptsLegacyMessageAndPortugueseDetails() {
		assertEquals("Erro de negócio", decode(409, "{\"message\":\"Erro de negócio\"}").getMessage());
		assertEquals("Erro de negócio", decode(409, "{\"erro\":\"Erro de negócio\"}").getMessage());
		assertTrue(decode(400, "{\"detalhes\":[{\"campo\":\"email\",\"mensagem\":\"Confira o endereço informado.\"}]}")
				.getMessage().contains("E-mail: Confira o endereço informado."));
	}

	@Test
	void ignoresMalformedDetailsAndFallsBackToError() {
		ApiException error = decode(404, """
				{"error":"Client not found","details":[null,5,{}, {"message":"  "}, {"message":false}]}
				""");
		assertEquals("Client not found", error.getMessage());
		assertFalse(error.hasValidationDetails());
	}

	@Test
	void retainsUsefulValidationEvenWithoutFieldAndHandlesNestedFields() {
		String message = decode(400, """
				{"details":[{"field":"request.email","message":"must not be blank"},
				{"message":"Confira o período."},{"field":"unknown","message":"Valor inválido."}]}
				""").getMessage();
		assertTrue(message.contains("E-mail: Campo obrigatório."));
		assertTrue(message.contains("Confira o período."));
		assertTrue(message.contains("Campo informado: Valor inválido."));
	}

	@Test
	void emptyHtmlInvalidJsonAndNonObjectBodiesUseSafeFallback() {
		for (String body : new String[]{"", " ", "<html>proxy error</html>", "{invalid", "[]", "null", "{\"error\":42}"}) {
			ApiException error = decode(400, body);
			assertEquals("Não foi possível concluir a solicitação.", error.getMessage());
			assertFalse(error.hasValidationDetails());
		}
		Response withoutBody = response(404, null);
		assertEquals("Não foi possível concluir a solicitação.", decoder.decode("ClientClient#findAll()", withoutBody).getMessage());
	}

	@Test
	void unreadableBodyDoesNotHideHttpStatus() throws IOException {
		Response.Body body = mock(Response.Body.class);
		when(body.asInputStream()).thenThrow(new IOException("Untrusted transport detail"));
		Response response = response(409, null).toBuilder().body(body).build();
		ApiException error = (ApiException) decoder.decode("ClientClient#create()", response);
		assertEquals(409, error.getStatus());
		assertFalse(error.getMessage().contains("Untrusted"));
	}

	@Test
	void distinguishesBadLoginFromExpiredSession() {
		Response response = response(401, "{\"error\":\"private authentication detail\"}");
		assertEquals("E-mail ou senha incorretos.", decoder.decode("AuthClient#login(LoginRequest)", response).getMessage());
		assertEquals("Sua sessão expirou ou é inválida. Entre novamente.",
				decoder.decode("ClientClient#findAll()", response(401, "{}")).getMessage());
	}

	@Test
	void forbiddenAndServerFailuresUseSafePortugueseMessages() {
		assertEquals("Seu perfil não permite esta operação.", decode(403, "{\"error\":\"Internal policy\"}").getMessage());
		for (int status : new int[]{500,502,503}) {
			ApiException error = decode(status, "{\"error\":\"SQL password=private\",\"details\":[{\"message\":\"stack trace\"}]}");
			assertEquals(status, error.getStatus());
			assertEquals("Erro no servidor. Tente novamente mais tarde.", error.getMessage());
			assertFalse(error.hasValidationDetails());
		}
	}

	@Test
	void translatesRegistrationAndInvalidRequestSummaries() {
		assertEquals("Este e-mail já está cadastrado.", decode(409, "{\"error\":\"E-mail already registered\"}").getMessage());
		assertEquals("Verifique os dados informados.", decode(400, "{\"error\":\"Validation failed\"}").getMessage());
		assertTrue(decode(400, "{\"error\":\"Malformed request body\"}").getMessage().contains("interpretar os dados"));
		assertTrue(decode(400, "{\"error\":\"Invalid path parameter\"}").getMessage().startsWith("Identificador inválido"));
	}

	private ApiException decode(int status, String body) {
		return (ApiException) decoder.decode("ClientClient#create()", response(status, body));
	}

	private Response response(int status, String body) {
		var builder = Response.builder().status(status).reason("Error")
				.request(Request.create(Request.HttpMethod.POST, "http://localhost/api/clientes", Map.of(), null, StandardCharsets.UTF_8));
		if (body != null) {
			builder.body(body, StandardCharsets.UTF_8);
		}
		return builder.build();
	}
}
