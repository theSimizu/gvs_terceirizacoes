package com.h_group.gvs_terceirizacoes_desktop.service;

import com.h_group.gvs_terceirizacoes_desktop.api.*;
import com.h_group.gvs_terceirizacoes_desktop.dto.*;
import com.h_group.gvs_terceirizacoes_desktop.session.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

	private final AuthClient client = mock(AuthClient.class);
	private final AuthSession session = new AuthSession();
	private final TokenStore store = mock(TokenStore.class);
	private final JwtExpiry expiry = mock(JwtExpiry.class);
	private final AuthService service = new AuthService(client, session, store, expiry);

	@Test
	void restoredSessionUsesServerRoleInsteadOfEditableLocalRole() {
		var localAdmin = new UserDTO("id", "Name", "mail@example.com", "adm");
		var actualUser = new UserDTO("id", "Name", "mail@example.com", "user");
		when(store.load()).thenReturn(Optional.of(new StoredSession("token", localAdmin)));
		when(client.currentUser()).thenReturn(actualUser);
		assertTrue(service.restoreSession());
		assertFalse(session.isAdministrator());
		assertEquals(actualUser, session.getUser());
		verify(store).save(new StoredSession("token", actualUser));
	}

	@Test
	void rejectedSessionIsCleared() {
		storedSession();
		when(client.currentUser()).thenThrow(new ApiException("Expired", 401));
		assertFalse(service.restoreSession());
		assertFalse(session.isAuthenticated());
		verify(store).clear();
	}

	@Test
	void networkFailureDoesNotOpenWindowWithCachedAdminPermissions() {
		storedSession();
		when(client.currentUser()).thenThrow(new ApiException("Offline", 0));
		assertFalse(service.restoreSession());
		assertFalse(session.isAuthenticated());
		verify(store, never()).clear();
	}

	@Test
	void expiredTokenDoesNotCallApi() {
		storedSession();
		when(expiry.isExpired("token")).thenReturn(true);
		assertFalse(service.restoreSession());
		verifyNoInteractions(client);
		verify(store).clear();
	}

	@Test
	void loginKeepsRoleReturnedByServerAndLogoutRemovesIt() {
		var user = new UserDTO("id", "Name", "mail@example.com", "adm");
		when(client.login(any())).thenReturn(new LoginResponse("token", user));
		service.login("mail@example.com", "test-password");
		assertTrue(session.isAdministrator());
		service.logout();
		assertFalse(session.isAdministrator());
		verify(store).clear();
	}

	private void storedSession() {
		when(store.load()).thenReturn(Optional.of(new StoredSession("token", new UserDTO("id", "Name", "mail@example.com", "adm"))));
	}
}
