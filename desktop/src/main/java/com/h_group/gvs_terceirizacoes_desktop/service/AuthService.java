package com.h_group.gvs_terceirizacoes_desktop.service;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiException;
import com.h_group.gvs_terceirizacoes_desktop.api.AuthClient;
import com.h_group.gvs_terceirizacoes_desktop.dto.LoginRequest;
import com.h_group.gvs_terceirizacoes_desktop.dto.LoginResponse;
import com.h_group.gvs_terceirizacoes_desktop.dto.RegisterRequest;
import com.h_group.gvs_terceirizacoes_desktop.dto.RegisterResponse;
import com.h_group.gvs_terceirizacoes_desktop.session.AuthSession;
import feign.FeignException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private static final String CONNECTION_ERROR =
			"Não foi possível conectar ao servidor. Verifique sua conexão.";

	private final AuthClient authClient;

	private final AuthSession authSession;

	public AuthService(AuthClient authClient, AuthSession authSession) {
		this.authClient = authClient;
		this.authSession = authSession;
	}

	public LoginResponse login(String email, String password) {
		try {
			LoginResponse response = authClient.login(new LoginRequest(email, password));
			if (response == null || response.token() == null || response.token().isBlank()) {
				throw new ApiException("Resposta inválida do servidor.", 0);
			}
			authSession.store(response.token(), response.user());
			return response;
		}
		catch (ApiException exception) {
			throw exception;
		}
		catch (FeignException exception) {
			throw translate(exception);
		}
		catch (Exception exception) {
			throw new ApiException(CONNECTION_ERROR, exception);
		}
	}

	public RegisterResponse register(String name, String email, String password) {
		try {
			RegisterResponse response = authClient.register(new RegisterRequest(name, email, password));
			if (response == null) {
				throw new ApiException("Resposta inválida do servidor.", 0);
			}
			if (response.token() != null && !response.token().isBlank()) {
				authSession.store(response.token(), response.user());
			}
			return response;
		}
		catch (ApiException exception) {
			throw exception;
		}
		catch (FeignException exception) {
			throw translate(exception);
		}
		catch (Exception exception) {
			throw new ApiException(CONNECTION_ERROR, exception);
		}
	}

	private ApiException translate(FeignException exception) {
		if (exception.status() < 0) {
			return new ApiException(CONNECTION_ERROR, exception);
		}
		if (exception.status() == 401) {
			return new ApiException("E-mail ou senha incorretos.", exception.status());
		}
		if (exception.status() >= 500) {
			return new ApiException("Erro no servidor. Tente novamente mais tarde.", exception.status());
		}
		return new ApiException("Não foi possível concluir a solicitação.", exception.status());
	}

}
