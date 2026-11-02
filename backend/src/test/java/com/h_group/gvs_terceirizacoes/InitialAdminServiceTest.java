package com.h_group.gvs_terceirizacoes;

import com.h_group.gvs_terceirizacoes.entity.User;
import com.h_group.gvs_terceirizacoes.entity.UserRole;
import com.h_group.gvs_terceirizacoes.repository.UserRepository;
import com.h_group.gvs_terceirizacoes.service.InitialAdminService;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InitialAdminServiceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    @AfterEach
    void closeValidator() {
        factory.close();
    }

    @Test
    void noConfigurationDoesNotCreateAnyAccount() {
        service("", "").run(null);
        verifyNoInteractions(users);
    }

    @Test
    void createsInitialAdminWithHashedPassword() {
        service(" INITIAL@EXAMPLE.COM ", "initial-test-password").run(null);
        var captured = ArgumentCaptor.forClass(User.class);
        verify(users).save(captured.capture());
        User admin = captured.getValue();
        assertEquals("initial@example.com", admin.getEmail());
        assertEquals(UserRole.ADM, admin.getRole());
        assertNotEquals("initial-test-password", admin.getPassword());
        assertTrue(encoder.matches("initial-test-password", admin.getPassword()));
    }

    @Test
    void existingAdminPreventsAnotherBootstrapAccount() {
        when(users.existsByRole(UserRole.ADM)).thenReturn(true);
        service("initial@example.com", "initial-test-password").run(null);
        verify(users, never()).save(any());
    }

    @Test
    void existingUserIsNotSilentlyPromotedOrOverwritten() {
        when(users.existsByEmailIgnoreCase("initial@example.com")).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> service("initial@example.com", "initial-test-password").run(null));
        verify(users, never()).save(any());
    }

    @Test
    void partialOrInvalidConfigurationFailsWithoutStoringCredentials() {
        assertThrows(IllegalStateException.class, () -> service("initial@example.com", "").run(null));
        assertThrows(IllegalStateException.class, () -> service("invalid-email", "initial-test-password").run(null));
        verifyNoInteractions(users);
    }

    private InitialAdminService service(String email, String password) {
        return new InitialAdminService(users, encoder, factory.getValidator(), email, password, "Initial admin");
    }
}
