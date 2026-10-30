package com.h_group.gvs_terceirizacoes_desktop.api;

import com.h_group.gvs_terceirizacoes_desktop.session.AuthSession;
import feign.RequestInterceptor;
import feign.RequestTemplate;

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
