package com.h_group.gvs_terceirizacoes_desktop.session;

import com.h_group.gvs_terceirizacoes_desktop.dto.UserDTO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthSessionTest {

	@Test
	void administratorRequiresAuthenticatedSessionAndExplicitRole() {
		AuthSession session = new AuthSession();
		assertFalse(session.isAdministrator());
		session.store("token", new UserDTO("id", "Name", "mail@example.com", "adm"));
		assertTrue(session.isAdministrator());
		session.clear();
		assertFalse(session.isAdministrator());
	}

	@Test
	void missingUnknownAndRegularRolesDoNotGrantAdministratorActions() {
		AuthSession session = new AuthSession();
		for (String role : new String[]{null, "user", "unknown"}) {
			session.store("token", new UserDTO("id", "Name", "mail@example.com", role));
			assertFalse(session.isAdministrator());
		}
		session.store("", new UserDTO("id", "Name", "mail@example.com", "adm"));
		assertFalse(session.isAdministrator());
	}
}
