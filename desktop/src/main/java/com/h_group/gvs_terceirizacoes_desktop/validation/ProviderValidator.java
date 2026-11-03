package com.h_group.gvs_terceirizacoes_desktop.validation;

import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Validação antecipada do formulário de prestador.
 * Confere dados obrigatórios, documento por comprimento e contato; não comprova CPF/CNPJ válido.
 */
@Component
public class ProviderValidator {

	private static final Pattern EMAIL_PATTERN =
			Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$");

	public Optional<String> validate(String name, String taxId, String specialty, String email, String phone) {
		if (isBlank(name) || name.trim().length() < 3) {
			return Optional.of("Informe o nome completo (mínimo 3 caracteres).");
		}
		int taxIdDigits = digitsOnly(taxId).length();
		if (taxIdDigits != 11 && taxIdDigits != 14) {
			return Optional.of("Informe um CPF (11 dígitos) ou CNPJ (14 dígitos).");
		}
		if (isBlank(specialty)) {
			return Optional.of("Informe a especialidade do prestador.");
		}
		if (isBlank(email) || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
			return Optional.of("Informe um e-mail válido.");
		}
		int phoneDigits = digitsOnly(phone).length();
		if (phoneDigits < 10 || phoneDigits > 11) {
			return Optional.of("Informe um telefone com DDD.");
		}
		return Optional.empty();
	}

	private String digitsOnly(String value) {
		return value == null ? "" : value.replaceAll("\\D", "");
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

}
