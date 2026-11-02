package com.h_group.gvs_terceirizacoes_desktop.api;

import java.util.Map;
import java.util.regex.Pattern;

final class ValidationMessages {

	private static final Map<String, String> FIELDS = Map.ofEntries(
			Map.entry("legalName", "Razão social"), Map.entry("name", "Nome"),
			Map.entry("cnpj", "CNPJ"), Map.entry("taxId", "CPF/CNPJ"),
			Map.entry("email", "E-mail"), Map.entry("phone", "Telefone"),
			Map.entry("password", "Senha"), Map.entry("address", "Endereço"),
			Map.entry("clientId", "Cliente"), Map.entry("providerId", "Prestador"),
			Map.entry("contractId", "Contrato"), Map.entry("startDate", "Data de início"),
			Map.entry("endDate", "Data de término"), Map.entry("executionDate", "Data de execução"),
			Map.entry("agreedValue", "Valor acordado"), Map.entry("description", "Descrição"),
			Map.entry("specialty", "Especialidade"), Map.entry("status", "Situação"));
	private static final Pattern SIZE = Pattern.compile("size must be between (\\d+) and (\\d+)");
	private static final Pattern MINIMUM = Pattern.compile("must be greater than or equal to (.+)");

	private ValidationMessages() {
	}

	static String format(String field, String message) {
		String text = translate(message);
		if (field == null || field.isBlank()) {
			return text;
		}
		String name = field.substring(field.lastIndexOf('.') + 1);
		return FIELDS.getOrDefault(name, "Campo informado") + ": " + text;
	}

	private static String translate(String message) {
		String text = message.trim();
		var size = SIZE.matcher(text);
		if (size.matches()) {
			String min = size.group(1);
			String max = size.group(2);
			if (max.equals("2147483647")) {
				return "Use pelo menos " + min + " caracteres.";
			}
			if (min.equals(max)) {
				return "Use " + min + " caracteres.";
			}
			if (min.equals("0")) {
				return "Use no máximo " + max + " caracteres.";
			}
			return "Use de " + min + " a " + max + " caracteres.";
		}
		var minimum = MINIMUM.matcher(text);
		if (minimum.matches()) {
			return "Informe um valor maior ou igual a " + minimum.group(1).replace('.', ',') + ".";
		}
		return switch (text) {
			case "must not be null", "must not be blank", "must not be empty" -> "Campo obrigatório.";
			case "must be a well-formed email address" -> "Informe um e-mail válido.";
			default -> text;
		};
	}
}
