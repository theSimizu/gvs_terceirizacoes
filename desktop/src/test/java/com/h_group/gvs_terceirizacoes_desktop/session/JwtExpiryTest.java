package com.h_group.gvs_terceirizacoes_desktop.session;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class JwtExpiryTest {

	private final JwtExpiry jwtExpiry = new JwtExpiry(JsonMapper.builder().build());

	@Test
	void detectsExpiredToken() {
		String token = tokenWithExpiration(1_000L);

		assertTrue(jwtExpiry.isExpired(token, Instant.ofEpochSecond(2_000L)));
	}

	@Test
	void acceptsValidToken() {
		String token = tokenWithExpiration(3_000L);

		assertFalse(jwtExpiry.isExpired(token, Instant.ofEpochSecond(2_000L)));
	}

	@Test
	void treatsMalformedTokenAsExpired() {
		assertTrue(jwtExpiry.isExpired("not-a-token", Instant.now()));
	}

	private String tokenWithExpiration(long expiration) {
		String payload = Base64.getUrlEncoder().withoutPadding()
				.encodeToString(("{\"exp\":" + expiration + "}").getBytes(StandardCharsets.UTF_8));
		return "header." + payload + ".signature";
	}

}
