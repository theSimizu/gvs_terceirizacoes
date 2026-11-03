package com.h_group.gvs_terceirizacoes_desktop.session;

import com.h_group.gvs_terceirizacoes_desktop.dto.UserDTO;
import org.springframework.stereotype.Component;

/**
 * Estado de autenticação em memória, acessado de forma sincronizada.
 * As consultas locais de perfil orientam os controles da interface; a API é
 * responsável pela autorização efetiva das operações.
 */
@Component
public class AuthSession {
	private String token;
	private UserDTO user;

	/**
	 * Substitui o estado local da sessão.
	 * @param token token recebido da API
	 * @param user identificação usada pela interface; pode estar ausente durante a verificação
	 */
	public synchronized void store(String token, UserDTO user) {
		this.token = token;
		this.user = user;
	}

	public synchronized String getToken() {
		return token;
	}

	public synchronized UserDTO getUser() {
		return user;
	}

	/**
	 * Verifica a presença de um token no estado local.
	 * @return true se há token não vazio; isso não verifica assinatura ou validade no servidor
	 */
	public synchronized boolean isAuthenticated() {
		return token != null && !token.isBlank();
	}

	/**
	 * Consulta o perfil local para orientar a apresentação de controles.
	 * @return true quando há sessão local e identificação com perfil adm
	 */
	public synchronized boolean isAdministrator() {
		return isAuthenticated() && user != null && user.isAdministrator();
	}

	public synchronized void clear() {
		this.token = null;
		this.user = null;
	}

}
