package com.h_group.gvs_terceirizacoes_desktop.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ProviderValidatorTest {

	private final ProviderValidator validator = new ProviderValidator();

	@Test
	void acceptsValidProviderWithCpf() {
		assertTrue(validator.validate("Maria Silva", "123.456.789-01",
				"Elétrica", "maria@exemplo.com", "(11) 91234-5678").isEmpty());
	}

	@Test
	void acceptsValidProviderWithCnpj() {
		assertTrue(validator.validate("Empresa Exemplo", "12.345.678/0001-90",
				"Hidráulica", "contato@empresa.com", "(11) 3456-7890").isEmpty());
	}

	@Test
	void rejectsShortName() {
		assertTrue(validator.validate("AB", "123.456.789-01",
				"Elétrica", "maria@exemplo.com", "(11) 91234-5678").isPresent());
	}

	@Test
	void rejectsTaxIdWithWrongLength() {
		assertTrue(validator.validate("Maria Silva", "123.456.789",
				"Elétrica", "maria@exemplo.com", "(11) 91234-5678").isPresent());
	}

	@Test
	void rejectsBlankSpecialty() {
		assertTrue(validator.validate("Maria Silva", "123.456.789-01",
				"  ", "maria@exemplo.com", "(11) 91234-5678").isPresent());
	}

	@Test
	void rejectsInvalidEmail() {
		assertTrue(validator.validate("Maria Silva", "123.456.789-01",
				"Elétrica", "not-an-email", "(11) 91234-5678").isPresent());
	}

	@Test
	void rejectsPhoneWithoutAreaCode() {
		assertTrue(validator.validate("Maria Silva", "123.456.789-01",
				"Elétrica", "maria@exemplo.com", "91234-5678").isPresent());
	}

}
