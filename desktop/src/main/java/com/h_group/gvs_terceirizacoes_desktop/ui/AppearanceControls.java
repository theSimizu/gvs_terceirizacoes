package com.h_group.gvs_terceirizacoes_desktop.ui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.StringConverter;

final class AppearanceControls {

	private AppearanceControls() {
	}

	static Parent wrap(Parent content, Window owner) {
		return wrap(content, owner, UiPreferences.get());
	}

	static Parent wrap(Parent content, Window owner, UiPreferences preferences) {
		ComboBox<UiSettings.Theme> theme = new ComboBox<>();
		theme.setId("appearance-theme");
		theme.getItems().setAll(UiSettings.Theme.values());
		theme.setConverter(new StringConverter<>() {
			public String toString(UiSettings.Theme value) { return value == UiSettings.Theme.DARK ? "Escuro" : "Claro"; }
			public UiSettings.Theme fromString(String value) { return "Escuro".equals(value) ? UiSettings.Theme.DARK : UiSettings.Theme.LIGHT; }
		});
		Label themeLabel = new Label("Tema");
		themeLabel.setLabelFor(theme);
		theme.setOnAction(event -> {
			if (theme.getValue() != null) preferences.update(value -> value.withTheme(theme.getValue()));
		});
		Button smaller = button("A−", "Diminuir o tamanho da fonte", "appearance-smaller");
		Button larger = button("A+", "Aumentar o tamanho da fonte", "appearance-larger");
		Label percent = new Label();
		percent.setId("appearance-percent");
		smaller.setOnAction(event -> preferences.update(value -> value.withFont(value.fontPercent() - 10)));
		larger.setOnAction(event -> preferences.update(value -> value.withFont(value.fontPercent() + 10)));
		Button accessibility = button("Acessibilidade", "Opções de acessibilidade visual", "appearance-accessibility");
		accessibility.setOnAction(event -> showAccessibility(owner));
		FlowPane bar = new FlowPane(10, 8, themeLabel, theme, smaller, percent, larger, accessibility);
		bar.getStyleClass().add("appearance-bar");
		bar.setAlignment(Pos.CENTER_RIGHT);
		Label notice = new Label();
		notice.getStyleClass().add("appearance-notice");
		notice.textProperty().bind(preferences.noticeProperty());
		notice.setWrapText(true);
		notice.visibleProperty().bind(notice.textProperty().isNotEmpty());
		notice.managedProperty().bind(notice.visibleProperty());
		VBox controls = new VBox(bar, notice);
		BorderPane frame = new BorderPane(content);
		frame.setTop(controls);
		frame.getStyleClass().add("application-frame");
		preferences.attach(frame);
		preferences.observe(frame, value -> {
			if (theme.getValue() != value.theme()) theme.setValue(value.theme());
			percent.setText(value.fontPercent() + "%");
			percent.setAccessibleText("Tamanho da fonte: " + value.fontPercent() + " por cento");
			smaller.setDisable(value.fontPercent() <= 80);
			larger.setDisable(value.fontPercent() >= 200);
		});
		return frame;
	}

	static void shortcuts(Scene scene, Window owner) {
		UiPreferences preferences = UiPreferences.get();
		fontShortcuts(scene, preferences);
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN), () -> showAccessibility(owner));
	}

	static void fontShortcuts(Scene scene, UiPreferences preferences) {
		for (KeyCode code : new KeyCode[] {KeyCode.PLUS, KeyCode.EQUALS, KeyCode.ADD}) {
			scene.getAccelerators().put(new KeyCodeCombination(code, KeyCombination.SHORTCUT_DOWN),
					() -> preferences.update(value -> value.withFont(value.fontPercent() + 10)));
		}
		for (KeyCode code : new KeyCode[] {KeyCode.MINUS, KeyCode.SUBTRACT}) {
			scene.getAccelerators().put(new KeyCodeCombination(code, KeyCombination.SHORTCUT_DOWN),
					() -> preferences.update(value -> value.withFont(value.fontPercent() - 10)));
		}
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN),
				() -> preferences.update(value -> value.withFont(value.fontPercent() + 10)));
		for (KeyCode code : new KeyCode[] {KeyCode.DIGIT0, KeyCode.NUMPAD0}) {
			scene.getAccelerators().put(new KeyCodeCombination(code, KeyCombination.SHORTCUT_DOWN),
					() -> preferences.update(value -> value.withFont(100)));
		}
	}

	private static Button button(String text, String description, String id) {
		Button button = new Button(text);
		button.setId(id);
		button.getStyleClass().add("appearance-button");
		button.setAccessibleText(description);
		button.setTooltip(new Tooltip(description));
		return button;
	}

	private static void showAccessibility(Window owner) {
		UiPreferences preferences = UiPreferences.get();
		Dialog<Void> dialog = new Dialog<>();
		var pane = DialogDesign.configure(dialog, owner, "Acessibilidade", "Ajuste a apresentação para facilitar a leitura. As mudanças são aplicadas na hora.");
		CheckBox contrast = new CheckBox("Alto contraste");
		contrast.setId("accessibility-contrast");
		CheckBox colors = new CheckBox("Paleta alternativa para distinguir cores");
		colors.setId("accessibility-colors");
		CheckBox motion = new CheckBox("Reduzir animações dos gráficos");
		motion.setId("accessibility-motion");
		contrast.setWrapText(true);
		colors.setWrapText(true);
		motion.setWrapText(true);
		Label explanation = new Label("A paleta alternativa usa azul, laranja e roxo. Os status continuam identificados por texto.\n\nUse Tab para navegar e Enter ou Espaço para acionar os controles. Ctrl + e Ctrl − ajustam a fonte; Ctrl 0 volta a 100%.");
		explanation.setWrapText(true);
		explanation.getStyleClass().add("dialog-subtitle");
		Button reset = button("Restaurar padrão", "Restaurar tema claro, fonte em 100% e opções padrão", "accessibility-reset");
		reset.setOnAction(event -> preferences.update(value -> UiSettings.defaults()));
		Label saved = new Label();
		saved.textProperty().bind(javafx.beans.binding.Bindings.when(preferences.noticeProperty().isEmpty())
				.then("As preferências valem neste computador.").otherwise(preferences.noticeProperty()));
		saved.setWrapText(true);
		saved.getStyleClass().add("dialog-subtitle");
		VBox content = new VBox(18, contrast, colors, motion, explanation, reset, saved);
		ScrollPane scroll = new ScrollPane(content);
		scroll.setFitToWidth(true);
		scroll.setMinHeight(0);
		scroll.getStyleClass().add("dialog-scroll");
		pane.setContent(scroll);
		contrast.setOnAction(event -> preferences.update(value -> value.withContrast(contrast.isSelected())));
		colors.setOnAction(event -> preferences.update(value -> value.withColors(colors.isSelected())));
		motion.setOnAction(event -> preferences.update(value -> value.withMotion(motion.isSelected())));
		preferences.observe(pane, value -> {
			contrast.setSelected(value.highContrast());
			colors.setSelected(value.colorSafe());
			motion.setSelected(value.reducedMotion());
		});
		pane.getButtonTypes().setAll(new ButtonType("Concluir", ButtonBar.ButtonData.CANCEL_CLOSE));
		dialog.showAndWait();
	}
}
