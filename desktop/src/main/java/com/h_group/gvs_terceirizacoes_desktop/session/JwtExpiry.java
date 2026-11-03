package com.h_group.gvs_terceirizacoes_desktop.session;

import java.time.Instant;
import java.util.Base64;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Lê a expiração do payload JWT para orientar a restauração local da sessão.
 * Não verifica a assinatura do token. A identidade e o perfil atuais são
 * conferidos no servidor antes de abrir uma sessão restaurada.
 */
@Component
@RequiredArgsConstructor
public class JwtExpiry {
	private final ObjectMapper objectMapper;

	/**
	 * Verifica a expiração usando o horário atual.
	 * @param token token salvo no desktop
	 * @return true se expirou ou não foi possível ler a expiração
	 */
	public boolean isExpired(String token) {
		return isExpired(token, Instant.now());
	}

	/**
	 * Verifica a expiração em um instante explícito, permitindo testes determinísticos.
	 * @param token token a examinar
	 * @param now instante usado na comparação
	 * @return true quando expirou ou o payload é inválido
	 */
	public boolean isExpired(String token, Instant now) {
		Long expiresAt = readExpiration(token);
		if (expiresAt == null) {
			return true;
		}
		return now.getEpochSecond() >= expiresAt;
	}

	private Long readExpiration(String token) {
		if (token == null || token.isBlank()) {
			return null;
		}
		String[] parts = token.split("\\.");
		if (parts.length < 2) {
			return null;
		}
		try {
			byte[] decoded = Base64.getUrlDecoder().decode(pad(parts[1]));
			JsonNode payload = objectMapper.readTree(decoded);
			JsonNode expiration = payload.get("exp");
			if (expiration == null || !expiration.isNumber()) {
				return null;
			}
			return expiration.asLong();
		} catch (Exception exception) {
			return null;
		}
	}

	private String pad(String segment) {
		int remainder = segment.length() % 4;
		if (remainder == 0) {
			return segment;
		}
		return segment + "=".repeat(4 - remainder);
	}

}
