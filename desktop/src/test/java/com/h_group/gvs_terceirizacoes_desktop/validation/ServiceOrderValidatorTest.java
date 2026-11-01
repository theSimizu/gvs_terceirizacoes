package com.h_group.gvs_terceirizacoes_desktop.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ServiceOrderValidatorTest {

	private final ServiceOrderValidator validator = new ServiceOrderValidator();

	private final UUID contractId = UUID.randomUUID();

	private final LocalDate executionDate = LocalDate.of(2026, 2, 20);

	@Test
	void acceptsValidServiceOrder() {
		assertTrue(validator.validate(contractId, "Troca de iluminação", executionDate).isEmpty());
	}

	@Test
	void rejectsMissingContract() {
		assertTrue(validator.validate(null, "Troca de iluminação", executionDate).isPresent());
	}

	@Test
	void rejectsBlankDescription() {
		assertTrue(validator.validate(contractId, "   ", executionDate).isPresent());
	}

	@Test
	void rejectsNullDescription() {
		assertTrue(validator.validate(contractId, null, executionDate).isPresent());
	}

	@Test
	void rejectsTooLongDescription() {
		assertTrue(validator.validate(contractId, "a".repeat(1001), executionDate).isPresent());
	}

	@Test
	void acceptsMaximumLengthDescription() {
		assertTrue(validator.validate(contractId, "a".repeat(1000), executionDate).isEmpty());
	}

	@Test
	void rejectsMissingExecutionDate() {
		assertTrue(validator.validate(contractId, "Troca de iluminação", null).isPresent());
	}

}
