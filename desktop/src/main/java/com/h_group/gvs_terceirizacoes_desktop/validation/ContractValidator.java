package com.h_group.gvs_terceirizacoes_desktop.validation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Validação antecipada dos vínculos, período e valor de um contrato.
 * A aplicação desta regra na tela não substitui uma validação correspondente no servidor.
 */
@Component
public class ContractValidator {

	public Optional<String> validate(UUID clientId, UUID providerId, LocalDate startDate, LocalDate endDate,
			BigDecimal agreedValue) {
		if (clientId == null) {
			return Optional.of("Selecione um cliente.");
		}
		if (providerId == null) {
			return Optional.of("Selecione um prestador.");
		}
		if (startDate == null) {
			return Optional.of("Informe a data de início.");
		}
		if (endDate != null && endDate.isBefore(startDate)) {
			return Optional.of("A data de término não pode ser anterior à data de início.");
		}
		if (agreedValue == null) {
			return Optional.of("Informe o valor acordado.");
		}
		if (agreedValue.signum() < 0) {
			return Optional.of("O valor acordado não pode ser negativo.");
		}
		return Optional.empty();
	}

}
