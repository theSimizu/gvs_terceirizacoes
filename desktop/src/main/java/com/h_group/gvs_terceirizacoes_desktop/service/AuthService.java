package com.h_group.gvs_terceirizacoes_desktop.service;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiException;
import com.h_group.gvs_terceirizacoes_desktop.api.AuthClient;
import com.h_group.gvs_terceirizacoes_desktop.dto.LoginRequest;
import com.h_group.gvs_terceirizacoes_desktop.dto.LoginResponse;
import com.h_group.gvs_terceirizacoes_desktop.dto.RegisterRequest;
import com.h_group.gvs_terceirizacoes_desktop.dto.RegisterResponse;
import com.h_group.gvs_terceirizacoes_desktop.dto.StoredSession;
import com.h_group.gvs_terceirizacoes_desktop.dto.UserDTO;
import com.h_group.gvs_terceirizacoes_desktop.session.AuthSession;
import com.h_group.gvs_terceirizacoes_desktop.session.JwtExpiry;
import com.h_group.gvs_terceirizacoes_desktop.session.TokenStore;
import feign.FeignException;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Coordena login, cadastro, restauração e encerramento local de sessão.
 * A restauração consulta a identidade atual na API, evitando confiar no perfil
 * guardado em um arquivo editável do desktop.
 */
@Service
@RequiredArgsConstructor
public class AuthService {
	private static final Logger log = LoggerFactory.getLogger(AuthService.class);
	private static final String CONNECTION_ERROR = "Não foi possível conectar ao servidor. Verifique sua conexão.";

	private final AuthClient authClient;
	private final AuthSession authSession;
	private final TokenStore tokenStore;
	private final JwtExpiry jwtExpiry;

	/**
	 * Autentica e guarda a sessão recebida.
	 * @param email e-mail da conta
	 * @param password senha informada, sem inclusão em logs
	 * @return resposta autenticada da API
	 * @throws com.h_group.gvs_terceirizacoes_desktop.api.ApiException se o login ou a comunicação falhar
	 */
	public LoginResponse login(String email, String password) {
		try {
			LoginResponse response = authClient.login(new LoginRequest(email, password));
			if (response == null || response.token() == null || response.token().isBlank()) {
				throw new ApiException("Resposta inválida do servidor.", 0);
			}
			authSession.store(response.token(), response.user());
			persist(response.token(), response.user());
			return response;
		} catch (ApiException exception) {
			throw exception;
		} catch (FeignException exception) {
			throw translate(exception);
		} catch (Exception exception) {
			throw new ApiException(CONNECTION_ERROR, exception);
		}
	}

	/**
	 * Solicita uma conta comum; uma resposta sem token exige login posterior.
	 * @param name nome informado
	 * @param email e-mail informado
	 * @param password senha informada
	 * @return resposta do cadastro
	 * @throws com.h_group.gvs_terceirizacoes_desktop.api.ApiException se o cadastro ou a comunicação falhar
	 */
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
		} catch (ApiException exception) {
			throw exception;
		} catch (FeignException exception) {
			throw translate(exception);
		} catch (Exception exception) {
			throw new ApiException(CONNECTION_ERROR, exception);
		}
	}

	/**
	 * Confere expiração e consulta a identidade atual na API antes de restaurar.
	 * O perfil persistido no arquivo local não é aceito como autoridade.
	 * @return true se uma sessão foi restaurada após a conferência do servidor
	 */
	public boolean restoreSession() {
		Optional<StoredSession> stored = tokenStore.load();
		if (stored.isEmpty()) {
			return false;
		}
		StoredSession session = stored.get();
		if (jwtExpiry.isExpired(session.token())) {
			tokenStore.clear();
			return false;
		}
		authSession.store(session.token(), null);
		try {
			UserDTO user = authClient.currentUser();
			if (user == null || user.role() == null) {
				authSession.clear();
				return false;
			}
			authSession.store(session.token(), user);
			persist(session.token(), user);
			return true;
		} catch (RuntimeException exception) {
			authSession.clear();
			if (exception instanceof ApiException apiException && apiException.getStatus() == 401) {
				tokenStore.clear();
			}
			return false;
		}
	}

	/**
	 * Limpa o estado e o arquivo locais; não revoga o JWT no servidor.
	 */
	public void logout() {
		authSession.clear();
		tokenStore.clear();
	}

	private void persist(String token, UserDTO user) {
		try {
			tokenStore.save(new StoredSession(token, user));
		} catch (RuntimeException exception) {
			log.warn("Could not persist the session.", exception);
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
