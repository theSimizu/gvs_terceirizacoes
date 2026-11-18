package com.h_group.gvs_terceirizacoes_desktop.ui;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UiPresentationTest {

	@TempDir Path directory;

	@BeforeAll
	static void toolkit() throws Exception {
		CountDownLatch ready = new CountDownLatch(1);
		try { Platform.startup(ready::countDown); }
		catch (IllegalStateException alreadyStarted) { ready.countDown(); }
		assertTrue(ready.await(15, TimeUnit.SECONDS));
	}

	@Test
	void toolbarUpdatesSameFormAndFontReallyDoubles() throws Exception {
		onFx(() -> {
			UiPreferences preferences = new UiPreferences(directory.resolve("toolbar.properties"));
			TextField draft = new TextField("Rascunho que deve permanecer");
			draft.getStyleClass().add("auth-input");
			Parent root = AppearanceControls.wrap(new VBox(draft), null, preferences);
			Scene scene = new Scene(root, 1000, 600);
			styles(scene);
			root.applyCss();
			double initial = draft.getFont().getSize();
			Button larger = (Button) root.lookup("#appearance-larger");
			for (int i = 0; i < 10; i++) larger.fire();
			root.applyCss();
			assertEquals(200, preferences.current().fontPercent());
			assertTrue(larger.isDisabled());
			assertEquals(initial * 2, draft.getFont().getSize(), 0.1);
			preferences.update(value -> value.withTheme(UiSettings.Theme.DARK).withContrast(true).withColors(true));
			root.applyCss();
			assertEquals("Rascunho que deve permanecer", draft.getText());
			assertSame(draft, root.lookup(".auth-input"));
			assertTrue(root.getStyleClass().contains("theme-dark"));
			assertTrue(root.getStyleClass().contains("contrast-high"));
			assertTrue(root.getStyleClass().contains("color-safe"));
			scene.getAccelerators().get(new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.SUBTRACT,
					javafx.scene.input.KeyCombination.SHORTCUT_DOWN)).run();
			assertEquals(190, preferences.current().fontPercent());
			scene.getAccelerators().get(new javafx.scene.input.KeyCodeCombination(javafx.scene.input.KeyCode.NUMPAD0,
					javafx.scene.input.KeyCombination.SHORTCUT_DOWN)).run();
			assertEquals(new UiSettings(UiSettings.Theme.DARK, 100, true, true, false), preferences.current());
			assertEquals("Rascunho que deve permanecer", draft.getText());
		});
	}

	@Test
	void textAndStatusLabelsKeepContrastInAllThemeCombinations() throws Exception {
		onFx(() -> {
			UiPreferences preferences = new UiPreferences(directory.resolve("contrast.properties"));
			Label description = new Label("Orientação de leitura");
			description.getStyleClass().add("auth-subtitle");
			Button action = new Button("Entrar");
			action.getStyleClass().add("auth-button");
			Label[] statuses = {new Label("Ativo"), new Label("Cancelado"), new Label("Pendente")};
			String[] classes = {"status-active", "status-canceled", "status-pending"};
			for (int i = 0; i < statuses.length; i++) statuses[i].getStyleClass().addAll("status-badge", classes[i]);
			VBox form = new VBox(description, action, statuses[0], statuses[1], statuses[2]);
			form.getStyleClass().add("auth-form-area");
			Parent root = AppearanceControls.wrap(form, null, preferences);
			Scene scene = new Scene(root, 1000, 650);
			styles(scene);
			for (UiSettings.Theme theme : UiSettings.Theme.values()) {
				for (boolean contrast : new boolean[] {false, true}) {
					for (boolean colors : new boolean[] {false, true}) {
						UiSettings settings = new UiSettings(theme, 100, contrast, colors, false);
						preferences.update(previous -> settings);
						root.applyCss();
						assertTrue(ratio((Color) description.getTextFill(), (Color) form.getBackground().getFills().getFirst().getFill()) >= 4.5, settings.toString());
						assertTrue(ratio((Color) action.getTextFill(), (Color) action.getBackground().getFills().getFirst().getFill()) >= 4.5, settings.toString());
						for (Label status : statuses) {
							assertFalse(status.getText().isBlank());
							assertTrue(ratio((Color) status.getTextFill(), (Color) status.getBackground().getFills().getFirst().getFill()) >= 4.5, settings + " / " + status.getText());
						}
					}
				}
			}
		});
	}

	private static void styles(Scene scene) {
		scene.getStylesheets().add(UiPresentationTest.class.getResource("/css/auth.css").toExternalForm());
		scene.getStylesheets().add(UiPresentationTest.class.getResource("/css/app.css").toExternalForm());
	}

	private static double ratio(Color foreground, Color background) {
		double first = luminance(foreground);
		double second = luminance(background);
		return (Math.max(first, second) + 0.05) / (Math.min(first, second) + 0.05);
	}

	private static double luminance(Color color) {
		return 0.2126 * linear(color.getRed()) + 0.7152 * linear(color.getGreen()) + 0.0722 * linear(color.getBlue());
	}

	private static double linear(double value) {
		return value <= 0.04045 ? value / 12.92 : Math.pow((value + 0.055) / 1.055, 2.4);
	}

	private static void onFx(Runnable action) throws Exception {
		FutureTask<Void> task = new FutureTask<>(() -> { action.run(); return null; });
		Platform.runLater(task);
		task.get(30, TimeUnit.SECONDS);
	}
}
