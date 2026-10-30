package com.h_group.gvs_terceirizacoes_desktop.api;

import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class ApiErrorDecoder implements ErrorDecoder {

	private final ObjectMapper objectMapper;

	public ApiErrorDecoder(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public Exception decode(String methodKey, Response response) {
		int status = response.status();
		String detail = extractMessage(readBody(response));

		if (status == 401) {
			return new ApiException("E-mail ou senha incorretos.", status);
		}
		if (status >= 500) {
			return new ApiException("Erro no servidor. Tente novamente mais tarde.", status);
		}
		if (detail != null && !detail.isBlank()) {
			return new ApiException(detail, status);
		}
		return new ApiException("Não foi possível concluir a solicitação.", status);
	}

	private String readBody(Response response) {
		if (response.body() == null) {
			return null;
		}
		try (InputStream stream = response.body().asInputStream()) {
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		}
		catch (IOException exception) {
			return null;
		}
	}

	private String extractMessage(String body) {
		if (body == null || body.isBlank()) {
			return null;
		}
		try {
			JsonNode root = objectMapper.readTree(body);
			if (root == null) {
				return null;
			}
			String message = textualField(root, "message");
			if (message == null) {
				message = textualField(root, "erro");
			}
			if (message == null) {
				JsonNode details = root.path("detalhes");
				if (details != null && details.isArray() && details.size() > 0) {
					message = textualField(details.get(0), "mensagem");
				}
			}
			return message;
		}
		catch (Exception exception) {
			return null;
		}
	}

	private String textualField(JsonNode node, String field) {
		if (node == null) {
			return null;
		}
		JsonNode value = node.get(field);
		if (value != null && value.isString()) {
			String text = value.asString();
			if (text != null && !text.isBlank()) {
				return text;
			}
		}
		return null;
	}

}
