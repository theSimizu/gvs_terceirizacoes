package com.h_group.gvs_terceirizacoes_desktop.validation;

import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Validação antecipada de login e cadastro no desktop.
 * O cadastro exige senha com oito caracteres e composição definida pelo formulário;
 * essa regra é mais restritiva que o mínimo de seis caracteres da API atual.
 */
@Component
public class AuthValidator {

	private static final Pattern EMAIL_PATTERN =
			Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$");

	private static final Pattern PASSWORD_PATTERN =
			Pattern.compile("(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*]).{8,}");

	public Optional<String> validateLogin(String email, String password) {
		if (isBlank(email)) {
			return Optional.of("Informe o e-mail.");
		}
		if (!EMAIL_PATTERN.matcher(email).matches()) {
			return Optional.of("Informe um e-mail válido.");
		}
		if (isBlank(password)) {
			return Optional.of("Informe a senha.");
		}
		return Optional.empty();
	}

	public Optional<String> validateRegistration(String name, String email, String password,
			String passwordConfirmation) {
		if (isBlank(name)) {
			return Optional.of("Informe o nome completo.");
		}
		if (isBlank(email)) {
			return Optional.of("Informe o e-mail.");
		}
		if (!EMAIL_PATTERN.matcher(email).matches()) {
			return Optional.of("Informe um e-mail válido.");
		}
		if (isBlank(password)) {
			return Optional.of("Informe a senha.");
		}
		if (!PASSWORD_PATTERN.matcher(password).matches()) {
			return Optional.of("A senha deve ter no mínimo 8 caracteres, uma letra maiúscula, "
					+ "uma minúscula, um número e um caractere especial.");
		}
		if (!password.equals(passwordConfirmation)) {
			return Optional.of("As senhas não conferem.");
		}
		return Optional.empty();
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

}
