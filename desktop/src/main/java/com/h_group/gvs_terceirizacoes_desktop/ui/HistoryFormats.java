package com.h_group.gvs_terceirizacoes_desktop.ui;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class HistoryFormats {

	private static final String NEUTRAL_BADGE = "status-neutral";

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private static final Map<String, String> ACTION_LABELS = new LinkedHashMap<>();

	private static final Map<String, String> ACTION_BADGES = new LinkedHashMap<>();

	private static final Map<String, String> ENTITY_LABELS = new LinkedHashMap<>();

	private static final Map<String, String> DETAIL_LABELS = new LinkedHashMap<>();

	static {
		ACTION_LABELS.put("create", "Criação");
		ACTION_LABELS.put("update", "Atualização");
		ACTION_LABELS.put("delete", "Exclusão");
		ACTION_LABELS.put("status_change", "Alteração de Status");

		ACTION_BADGES.put("create", "status-completed");
		ACTION_BADGES.put("update", "status-progress");
		ACTION_BADGES.put("delete", "status-canceled");
		ACTION_BADGES.put("status_change", "status-pending");

		ENTITY_LABELS.put("Client", "Cliente");
		ENTITY_LABELS.put("Provider", "Prestador");
		ENTITY_LABELS.put("Contract", "Contrato");
		ENTITY_LABELS.put("ServiceOrder", "Ordem de Serviço");

		DETAIL_LABELS.put("legalName", "Razão Social");
		DETAIL_LABELS.put("name", "Nome");
		DETAIL_LABELS.put("cnpj", "CNPJ");
		DETAIL_LABELS.put("taxId", "CPF/CNPJ");
		DETAIL_LABELS.put("specialty", "Especialidade");
		DETAIL_LABELS.put("email", "E-mail");
		DETAIL_LABELS.put("phone", "Telefone");
		DETAIL_LABELS.put("status", "Status");
		DETAIL_LABELS.put("available", "Disponível");
		DETAIL_LABELS.put("clientId", "Cliente");
		DETAIL_LABELS.put("providerId", "Prestador");
		DETAIL_LABELS.put("contractId", "Contrato");
		DETAIL_LABELS.put("agreedValue", "Valor");
		DETAIL_LABELS.put("startDate", "Início");
		DETAIL_LABELS.put("endDate", "Término");
		DETAIL_LABELS.put("description", "Descrição");
		DETAIL_LABELS.put("executionDate", "Execução");
	}

	private HistoryFormats() {
	}

	public static String actionLabel(String action) {
		return label(ACTION_LABELS, action);
	}

	public static String entityLabel(String entity) {
		return label(ENTITY_LABELS, entity);
	}

	public static String actionBadgeClass(String action) {
		if (action == null) {
			return NEUTRAL_BADGE;
		}
		return ACTION_BADGES.getOrDefault(action, NEUTRAL_BADGE);
	}

	public static String documentLabel(String documentId) {
		if (documentId == null || documentId.isBlank()) {
			return "—";
		}
		String value = documentId.trim();
		String tail = value.length() <= 4 ? value : value.substring(value.length() - 4);
		return "#" + tail.toUpperCase(Locale.ROOT);
	}

	public static String summary(Map<String, Object> details) {
		if (details == null || details.isEmpty()) {
			return "—";
		}
		List<String> parts = new ArrayList<>();
		details.forEach((key, value) -> parts.add(prettyKey(key) + ": " + formatValue(key, value)));
		return String.join("; ", parts);
	}

	public static List<String> detailLines(Map<String, Object> details) {
		if (details == null || details.isEmpty()) {
			return List.of("Sem detalhes registrados.");
		}
		List<String> lines = new ArrayList<>();
		details.forEach((key, value) -> lines.add(prettyKey(key) + ": " + formatValue(key, value)));
		return lines;
	}

	public static String prettyKey(String key) {
		if (key == null || key.isBlank()) {
			return "";
		}
		String mapped = DETAIL_LABELS.get(key);
		if (mapped != null) {
			return mapped;
		}
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < key.length(); i++) {
			char current = key.charAt(i);
			if (i > 0 && Character.isUpperCase(current)) {
				text.append(' ');
			}
			text.append(current);
		}
		String spaced = text.toString();
		return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
	}

	private static String formatValue(String key, Object value) {
		if (value == null) {
			return "—";
		}
		if ("available".equals(key) && value instanceof Boolean available) {
			return available ? "Sim" : "Não";
		}
		if ("agreedValue".equals(key) && value instanceof Number number) {
			return InputMasks.formatCurrency(new BigDecimal(number.toString()));
		}
		if (value instanceof String text && text.matches("\\d{4}-\\d{2}-\\d{2}")) {
			try {
				return DATE_FORMAT.format(LocalDate.parse(text));
			}
			catch (DateTimeParseException ignored) {
				return text;
			}
		}
		return String.valueOf(value);
	}

	private static String label(Map<String, String> labels, String value) {
		if (value == null) {
			return "";
		}
		return labels.getOrDefault(value, value);
	}

}
