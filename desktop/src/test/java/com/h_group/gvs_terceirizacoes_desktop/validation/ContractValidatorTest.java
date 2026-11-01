package com.h_group.gvs_terceirizacoes_desktop.validation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ContractValidatorTest {

	private final ContractValidator validator = new ContractValidator();

	private final UUID clientId = UUID.randomUUID();

	private final UUID providerId = UUID.randomUUID();

	private final LocalDate startDate = LocalDate.of(2026, 1, 10);

	@Test
	void acceptsValidContract() {
		assertTrue(validator.validate(clientId, providerId, startDate, startDate.plusMonths(6),
				new BigDecimal("1500.00")).isEmpty());
	}

	@Test
	void acceptsContractWithoutEndDate() {
		assertTrue(validator.validate(clientId, providerId, startDate, null,
				new BigDecimal("1500.00")).isEmpty());
	}

	@Test
	void rejectsMissingClient() {
		assertTrue(validator.validate(null, providerId, startDate, null,
				new BigDecimal("1500.00")).isPresent());
	}

	@Test
	void rejectsMissingProvider() {
		assertTrue(validator.validate(clientId, null, startDate, null,
				new BigDecimal("1500.00")).isPresent());
	}

	@Test
	void rejectsMissingStartDate() {
		assertTrue(validator.validate(clientId, providerId, null, null,
				new BigDecimal("1500.00")).isPresent());
	}

	@Test
	void rejectsEndDateBeforeStartDate() {
		assertTrue(validator.validate(clientId, providerId, startDate, startDate.minusDays(1),
				new BigDecimal("1500.00")).isPresent());
	}

	@Test
	void rejectsMissingValue() {
		assertTrue(validator.validate(clientId, providerId, startDate, null, null).isPresent());
	}

	@Test
	void rejectsNegativeValue() {
		assertTrue(validator.validate(clientId, providerId, startDate, null,
				new BigDecimal("-0.01")).isPresent());
	}

	@Test
	void acceptsZeroValue() {
		assertTrue(validator.validate(clientId, providerId, startDate, null, BigDecimal.ZERO).isEmpty());
	}

}
