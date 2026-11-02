package com.h_group.gvs_terceirizacoes_desktop.session;

import com.h_group.gvs_terceirizacoes_desktop.dto.UserDTO;
import org.springframework.stereotype.Component;

@Component
public class AuthSession {
	private String token;
	private UserDTO user;

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

	public synchronized boolean isAuthenticated() {
		return token != null && !token.isBlank();
	}

	public synchronized boolean isAdministrator() {
		return isAuthenticated() && user != null && user.isAdministrator();
	}

	public synchronized void clear() {
		this.token = null;
		this.user = null;
	}

}
