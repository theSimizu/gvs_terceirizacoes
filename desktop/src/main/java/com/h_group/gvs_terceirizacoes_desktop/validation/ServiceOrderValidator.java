package com.h_group.gvs_terceirizacoes_desktop.validation;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Validação dos dados necessários ao formulário de ordem de serviço.
 * Auxilia a pessoa antes do envio; a API mantém suas próprias restrições de entrada.
 */
@Component
public class ServiceOrderValidator {

	private static final int MAX_DESCRIPTION_LENGTH = 1000;

	public Optional<String> validate(UUID contractId, String description, LocalDate executionDate) {
		if (contractId == null) {
			return Optional.of("Selecione um contrato.");
		}
		if (description == null || description.isBlank()) {
			return Optional.of("Informe a descrição da atividade.");
		}
		if (description.trim().length() > MAX_DESCRIPTION_LENGTH) {
			return Optional.of("A descrição deve ter no máximo 1000 caracteres.");
		}
		if (executionDate == null) {
			return Optional.of("Informe a data de execução.");
		}
		return Optional.empty();
	}

}
