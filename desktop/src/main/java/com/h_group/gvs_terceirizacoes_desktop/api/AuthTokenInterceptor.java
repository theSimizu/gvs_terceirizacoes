package com.h_group.gvs_terceirizacoes_desktop.api;

import com.h_group.gvs_terceirizacoes_desktop.session.AuthSession;
import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * Acrescenta o token da sessão às requisições Feign quando disponível.
 * A validação e a autorização do token permanecem no backend.
 */
public class AuthTokenInterceptor implements RequestInterceptor {

	private final AuthSession authSession;

	public AuthTokenInterceptor(AuthSession authSession) {
		this.authSession = authSession;
	}

	@Override
	public void apply(RequestTemplate template) {
		String token = authSession.getToken();
		if (token != null && !token.isBlank()) {
			template.header("Authorization", "Bearer " + token);
		}
	}

}
