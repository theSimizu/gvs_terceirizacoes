package com.h_group.gvs_terceirizacoes_desktop.ui;

record UiSettings(Theme theme, int fontPercent, boolean highContrast, boolean colorSafe, boolean reducedMotion) {

	enum Theme { LIGHT, DARK }

	UiSettings {
		if (theme == null) {
			theme = Theme.LIGHT;
		}
		fontPercent = Math.max(80, Math.min(200, fontPercent / 10 * 10));
	}

	static UiSettings defaults() {
		return new UiSettings(Theme.LIGHT, 100, false, false, false);
	}

	UiSettings withTheme(Theme value) {
		return new UiSettings(value, fontPercent, highContrast, colorSafe, reducedMotion);
	}

	UiSettings withFont(int value) {
		return new UiSettings(theme, value, highContrast, colorSafe, reducedMotion);
	}

	UiSettings withContrast(boolean value) {
		return new UiSettings(theme, fontPercent, value, colorSafe, reducedMotion);
	}

	UiSettings withColors(boolean value) {
		return new UiSettings(theme, fontPercent, highContrast, value, reducedMotion);
	}

	UiSettings withMotion(boolean value) {
		return new UiSettings(theme, fontPercent, highContrast, colorSafe, value);
	}
}
