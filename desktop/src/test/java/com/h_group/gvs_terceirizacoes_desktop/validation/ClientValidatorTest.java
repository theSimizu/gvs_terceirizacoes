package com.h_group.gvs_terceirizacoes_desktop.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ClientValidatorTest {

	private final ClientValidator validator = new ClientValidator();

	@Test
	void acceptsValidClient() {
		assertTrue(validator.validate("Empresa Exemplo", "12.345.678/0001-90",
				"contato@empresa.com", "(11) 91234-5678").isEmpty());
	}

	@Test
	void rejectsShortLegalName() {
		assertTrue(validator.validate("AB", "12.345.678/0001-90",
				"contato@empresa.com", "(11) 91234-5678").isPresent());
	}

	@Test
	void rejectsInvalidCnpj() {
		assertTrue(validator.validate("Empresa Exemplo", "123",
				"contato@empresa.com", "(11) 91234-5678").isPresent());
	}

	@Test
	void rejectsInvalidEmail() {
		assertTrue(validator.validate("Empresa Exemplo", "12.345.678/0001-90",
				"not-an-email", "(11) 91234-5678").isPresent());
	}

	@Test
	void rejectsPhoneWithoutAreaCode() {
		assertTrue(validator.validate("Empresa Exemplo", "12.345.678/0001-90",
				"contato@empresa.com", "91234-5678").isPresent());
	}

}
