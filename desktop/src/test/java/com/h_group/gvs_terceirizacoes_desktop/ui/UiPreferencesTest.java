package com.h_group.gvs_terceirizacoes_desktop.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UiPreferencesTest {

	@TempDir Path directory;

	@Test
	void missingFileUsesDefaultsWithoutCreatingFiles() {
		Path file = directory.resolve("appearance.properties");
		assertEquals(UiSettings.defaults(), new UiPreferences(file).current());
		assertFalse(Files.exists(file));
	}

	@Test
	void allChoicesSurviveRestartWithoutAccountData() throws Exception {
		Path file = directory.resolve("preferences/appearance.properties");
		UiPreferences preferences = new UiPreferences(file);
		UiSettings expected = new UiSettings(UiSettings.Theme.DARK, 170, true, true, true);
		preferences.update(previous -> expected);
		assertEquals(expected, new UiPreferences(file).current());
		Properties properties = new Properties();
		try (var input = Files.newInputStream(file)) { properties.load(input); }
		assertEquals(java.util.Set.of("theme", "fontPercent", "highContrast", "colorSafe", "reducedMotion"), properties.stringPropertyNames());
		assertEquals("", preferences.noticeProperty().get());
	}

	@Test
	void malformedPreferencesDoNotPreventStartup() throws Exception {
		Path file = directory.resolve("appearance.properties");
		Files.writeString(file, "theme=invalid\nfontPercent=large\n");
		UiPreferences preferences = new UiPreferences(file);
		assertEquals(UiSettings.defaults(), preferences.current());
		assertFalse(preferences.noticeProperty().get().isBlank());
	}

	@Test
	void partialFileKeepsDefaultsForMissingOptions() throws Exception {
		Path file = directory.resolve("appearance.properties");
		Files.writeString(file, "theme=DARK\n");
		assertEquals(UiSettings.defaults().withTheme(UiSettings.Theme.DARK), new UiPreferences(file).current());
	}

	@Test
	void failedSaveKeepsChoiceActiveAndDoesNotOverwriteOtherFile() throws Exception {
		Path obstacle = directory.resolve("not-a-directory");
		Files.writeString(obstacle, "preserve");
		UiPreferences preferences = new UiPreferences(obstacle.resolve("appearance.properties"));
		assertDoesNotThrow(() -> preferences.update(value -> value.withTheme(UiSettings.Theme.DARK)));
		assertEquals(UiSettings.Theme.DARK, preferences.current().theme());
		assertFalse(preferences.noticeProperty().get().isBlank());
		assertEquals("preserve", Files.readString(obstacle));
	}

	@Test
	void fontChangesHaveSafeLimitsAndResetPreservesOtherChoices() {
		UiSettings settings = new UiSettings(UiSettings.Theme.DARK, 5000, true, true, true);
		assertEquals(200, settings.fontPercent());
		assertEquals(80, settings.withFont(-20).fontPercent());
		assertEquals(new UiSettings(UiSettings.Theme.DARK, 100, true, true, true), settings.withFont(100));
	}
}
