package com.h_group.gvs_terceirizacoes_desktop.api;

import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
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
		ErrorMessage detail = extractMessage(readBody(response));

		if (status == 401) {
			String message = methodKey != null && methodKey.contains("#login(")
					? "E-mail ou senha incorretos."
					: "Sua sessão expirou ou é inválida. Entre novamente.";
			return new ApiException(message, status);
		}
		if (status == 403) {
			return new ApiException("Seu perfil não permite esta operação.", status);
		}
		if (status >= 500) {
			return new ApiException("Erro no servidor. Tente novamente mais tarde.", status);
		}
		if (detail != null) {
			return new ApiException(detail.text(), status, status == 400 && detail.validationDetails());
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

	private record ErrorMessage(String text, boolean validationDetails) {
	}

	private ErrorMessage extractMessage(String body) {
		if (body == null || body.isBlank()) {
			return null;
		}
		try {
			JsonNode root = objectMapper.readTree(body);
			if (root == null || !root.isObject()) {
				return null;
			}
			var messages = new LinkedHashSet<String>();
			for (String name : new String[]{"details", "detalhes"}) {
				JsonNode details = root.path(name);
				if (!details.isArray()) {
					continue;
				}
				for (JsonNode item : details) {
					String message = firstText(item, "message", "mensagem");
					if (message != null) {
						messages.add(ValidationMessages.format(firstText(item, "field", "campo"), message));
					}
				}
			}
			if (!messages.isEmpty()) {
				return new ErrorMessage("Verifique os campos informados:\n" + String.join("\n", messages), true);
			}
			String message = firstText(root, "error", "message", "erro");
			if (message == null) {
				return null;
			}
			message = switch (message) {
				case "Validation failed" -> "Verifique os dados informados.";
				case "E-mail already registered" -> "Este e-mail já está cadastrado.";
				case "Malformed request body" -> "Não foi possível interpretar os dados enviados. Confira os campos.";
				case "Invalid path parameter" -> "Identificador inválido. Atualize a lista e tente novamente.";
				default -> message;
			};
			return new ErrorMessage(message, false);
		}
		catch (Exception exception) {
			return null;
		}
	}

	private String firstText(JsonNode node, String... fields) {
		for (String field : fields) {
			String text = textualField(node, field);
			if (text != null) {
				return text;
			}
		}
		return null;
	}

	private String textualField(JsonNode node, String field) {
		if (node == null) {
			return null;
		}
		JsonNode value = node.get(field);
		if (value != null && value.isString()) {
			String text = value.asString();
			if (text != null && !text.isBlank()) {
				return text.trim();
			}
		}
		return null;
	}

}
