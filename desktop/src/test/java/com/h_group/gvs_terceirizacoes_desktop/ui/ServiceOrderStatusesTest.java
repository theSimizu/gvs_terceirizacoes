package com.h_group.gvs_terceirizacoes_desktop.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;

class ServiceOrderStatusesTest {

	@Test
	void exposesStatusesInCanonicalOrder() {
		assertEquals(List.of("pending", "in_progress", "completed", "canceled"),
				ServiceOrderStatuses.all());
	}

	@Test
	void translatesLabels() {
		assertEquals("Pendente", ServiceOrderStatuses.label("pending"));
		assertEquals("Em Andamento", ServiceOrderStatuses.label("in_progress"));
		assertEquals("Concluída", ServiceOrderStatuses.label("completed"));
		assertEquals("Cancelada", ServiceOrderStatuses.label("canceled"));
	}

	@Test
	void fallsBackToRawValueForUnknownStatus() {
		assertEquals("unknown", ServiceOrderStatuses.label("unknown"));
		assertEquals("", ServiceOrderStatuses.label(null));
	}

	@Test
	void mapsBadgeClasses() {
		assertEquals("status-pending", ServiceOrderStatuses.badgeClass("pending"));
		assertEquals("status-progress", ServiceOrderStatuses.badgeClass("in_progress"));
		assertEquals("status-completed", ServiceOrderStatuses.badgeClass("completed"));
		assertEquals("status-canceled", ServiceOrderStatuses.badgeClass("canceled"));
		assertNull(ServiceOrderStatuses.badgeClass(null));
	}

	@Test
	void sortsByCanonicalOrderAndUnknownLast() {
		assertEquals(0, ServiceOrderStatuses.ordinal("pending"));
		assertEquals(3, ServiceOrderStatuses.ordinal("canceled"));
		assertEquals(4, ServiceOrderStatuses.ordinal("unknown"));
		assertEquals(4, ServiceOrderStatuses.ordinal(null));
	}

}
