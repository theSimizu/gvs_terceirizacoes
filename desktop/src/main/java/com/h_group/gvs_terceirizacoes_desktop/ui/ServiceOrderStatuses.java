package com.h_group.gvs_terceirizacoes_desktop.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ServiceOrderStatuses {

	public static final String PENDING = "pending";

	public static final String IN_PROGRESS = "in_progress";

	public static final String COMPLETED = "completed";

	public static final String CANCELED = "canceled";

	private static final List<String> ORDER = List.of(PENDING, IN_PROGRESS, COMPLETED, CANCELED);

	private static final Map<String, String> LABELS = new LinkedHashMap<>();

	private static final Map<String, String> BADGES = new LinkedHashMap<>();

	static {
		LABELS.put(PENDING, "Pendente");
		LABELS.put(IN_PROGRESS, "Em Andamento");
		LABELS.put(COMPLETED, "Concluída");
		LABELS.put(CANCELED, "Cancelada");

		BADGES.put(PENDING, "status-pending");
		BADGES.put(IN_PROGRESS, "status-progress");
		BADGES.put(COMPLETED, "status-completed");
		BADGES.put(CANCELED, "status-canceled");
	}

	private ServiceOrderStatuses() {
	}

	public static List<String> all() {
		return ORDER;
	}

	public static String label(String status) {
		if (status == null) {
			return "";
		}
		return LABELS.getOrDefault(status, status);
	}

	public static String badgeClass(String status) {
		if (status == null) {
			return null;
		}
		return BADGES.get(status);
	}

	public static int ordinal(String status) {
		int index = status == null ? -1 : ORDER.indexOf(status);
		return index < 0 ? ORDER.size() : index;
	}

}
