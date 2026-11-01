package com.h_group.gvs_terceirizacoes_desktop.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class HistoryFormatsTest {

	@Test
	void translatesActionLabels() {
		assertEquals("Criação", HistoryFormats.actionLabel("create"));
		assertEquals("Atualização", HistoryFormats.actionLabel("update"));
		assertEquals("Exclusão", HistoryFormats.actionLabel("delete"));
		assertEquals("Alteração de Status", HistoryFormats.actionLabel("status_change"));
	}

	@Test
	void translatesEntityLabels() {
		assertEquals("Cliente", HistoryFormats.entityLabel("Client"));
		assertEquals("Prestador", HistoryFormats.entityLabel("Provider"));
		assertEquals("Contrato", HistoryFormats.entityLabel("Contract"));
		assertEquals("Ordem de Serviço", HistoryFormats.entityLabel("ServiceOrder"));
	}

	@Test
	void fallsBackToRawValueForUnknownLabels() {
		assertEquals("unknown", HistoryFormats.actionLabel("unknown"));
		assertEquals("Widget", HistoryFormats.entityLabel("Widget"));
		assertEquals("", HistoryFormats.actionLabel(null));
		assertEquals("", HistoryFormats.entityLabel(null));
	}

	@Test
	void mapsBadgeClasses() {
		assertEquals("status-completed", HistoryFormats.actionBadgeClass("create"));
		assertEquals("status-progress", HistoryFormats.actionBadgeClass("update"));
		assertEquals("status-canceled", HistoryFormats.actionBadgeClass("delete"));
		assertEquals("status-pending", HistoryFormats.actionBadgeClass("status_change"));
		assertEquals("status-neutral", HistoryFormats.actionBadgeClass("unknown"));
		assertEquals("status-neutral", HistoryFormats.actionBadgeClass(null));
	}

	@Test
	void shortensDocumentToLastFourCharacters() {
		assertEquals("#D4E5", HistoryFormats.documentLabel("1234abcd-0000-4000-8000-0000abcdd4e5"));
		assertEquals("#ABC", HistoryFormats.documentLabel("abc"));
		assertEquals("—", HistoryFormats.documentLabel(null));
		assertEquals("—", HistoryFormats.documentLabel("  "));
	}

	@Test
	void formatsEmptySummary() {
		assertEquals("—", HistoryFormats.summary(null));
		assertEquals("—", HistoryFormats.summary(Map.of()));
		assertEquals(List.of("Sem detalhes registrados."), HistoryFormats.detailLines(null));
		assertEquals(List.of("Sem detalhes registrados."), HistoryFormats.detailLines(Map.of()));
	}

	@Test
	void formatsSummaryWithPortugueseLabelsAndValues() {
		Map<String, Object> details = new LinkedHashMap<>();
		details.put("legalName", "Acme");
		details.put("available", false);
		details.put("agreedValue", new BigDecimal("1500.00"));
		details.put("startDate", "2025-01-31");

		assertEquals("Razão Social: Acme; Disponível: Não; Valor: R$ 1.500,00; Início: 31/01/2025",
				HistoryFormats.summary(details));
	}

	@Test
	void formatsDetailLinesInOrder() {
		Map<String, Object> details = new LinkedHashMap<>();
		details.put("name", "Prestadora");
		details.put("email", null);

		assertEquals(List.of("Nome: Prestadora", "E-mail: —"), HistoryFormats.detailLines(details));
	}

	@Test
	void prettifiesUnknownKeys() {
		assertEquals("Some Other Key", HistoryFormats.prettyKey("someOtherKey"));
		assertEquals("Widget", HistoryFormats.prettyKey("widget"));
		assertEquals("", HistoryFormats.prettyKey(null));
		assertEquals("", HistoryFormats.prettyKey(" "));
	}

	@Test
	void keepsNonDateStringsUntouched() {
		assertEquals("Status: pending", HistoryFormats.summary(Map.of("status", "pending")));
		assertTrue(HistoryFormats.summary(Map.of("description", "2025-13-99")).contains("2025-13-99"));
	}

}
