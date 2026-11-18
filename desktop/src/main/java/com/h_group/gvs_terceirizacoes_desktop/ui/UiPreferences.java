package com.h_group.gvs_terceirizacoes_desktop.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.Node;
import javafx.scene.Parent;

final class UiPreferences {

	private static final String BASE_STYLE = UiPreferences.class.getName() + ".baseStyle";
	private final Path file;
	private final ReadOnlyObjectWrapper<UiSettings> settings = new ReadOnlyObjectWrapper<>();
	private final ReadOnlyStringWrapper notice = new ReadOnlyStringWrapper("");
	private final Map<Parent, Boolean> roots = new WeakHashMap<>();

	private static class Holder {
		private static final UiPreferences INSTANCE = new UiPreferences(Path.of(System.getProperty(
				"gvs.appearance.file", Path.of(System.getProperty("user.home"), ".gvs-terceirizacoes", "appearance.properties").toString())));
	}

	static UiPreferences get() {
		return Holder.INSTANCE;
	}

	UiPreferences(Path file) {
		this.file = file.toAbsolutePath();
		settings.set(load());
		settings.addListener((observable, previous, current) -> {
			for (Parent root : new ArrayList<>(roots.keySet())) {
				apply(root);
			}
		});
	}

	UiSettings current() {
		return settings.get();
	}

	ReadOnlyObjectProperty<UiSettings> settingsProperty() {
		return settings.getReadOnlyProperty();
	}

	ReadOnlyStringProperty noticeProperty() {
		return notice.getReadOnlyProperty();
	}

	void update(UnaryOperator<UiSettings> change) {
		UiSettings next = change.apply(current());
		if (next != null && !next.equals(current())) {
			settings.set(next);
			save();
		}
	}

	void attach(Parent root) {
		if (roots.put(root, Boolean.TRUE) == null) {
			root.getProperties().put(BASE_STYLE, root.getStyle());
			root.getStyleClass().add("ui-preferences");
			root.getStylesheets().add(UiPreferences.class.getResource("/css/preferences.css").toExternalForm());
			root.sceneProperty().addListener((observable, previous, scene) -> {
				if (scene != null) AppearanceControls.fontShortcuts(scene, this);
			});
			if (root.getScene() != null) AppearanceControls.fontShortcuts(root.getScene(), this);
		}
		apply(root);
	}

	void observe(Node owner, Consumer<UiSettings> action) {
		ChangeListener<UiSettings> listener = (observable, previous, current) -> action.accept(current);
		owner.getProperties().put(new Object(), listener);
		settings.addListener(new WeakChangeListener<>(listener));
		action.accept(current());
	}

	private void apply(Parent root) {
		UiSettings value = current();
		root.getStyleClass().removeAll("theme-dark", "contrast-high", "color-safe", "reduced-motion", "large-text");
		if (value.theme() == UiSettings.Theme.DARK) root.getStyleClass().add("theme-dark");
		if (value.highContrast()) root.getStyleClass().add("contrast-high");
		if (value.colorSafe()) root.getStyleClass().add("color-safe");
		if (value.reducedMotion()) root.getStyleClass().add("reduced-motion");
		if (value.fontPercent() >= 150) root.getStyleClass().add("large-text");
		String base = (String) root.getProperties().get(BASE_STYLE);
		root.setStyle((base == null || base.isBlank() ? "" : base + "; ")
				+ String.format(Locale.ROOT, "-fx-font-size: %.2fpx;", 14.0 * value.fontPercent() / 100));
	}

	private UiSettings load() {
		if (!Files.exists(file)) return UiSettings.defaults();
		try (InputStream input = Files.newInputStream(file)) {
			Properties properties = new Properties();
			properties.load(input);
			return new UiSettings(UiSettings.Theme.valueOf(properties.getProperty("theme", "LIGHT")),
					Integer.parseInt(properties.getProperty("fontPercent", "100")),
					Boolean.parseBoolean(properties.getProperty("highContrast", "false")),
					Boolean.parseBoolean(properties.getProperty("colorSafe", "false")),
					Boolean.parseBoolean(properties.getProperty("reducedMotion", "false")));
		} catch (IOException | IllegalArgumentException exception) {
			notice.set("Não foi possível ler as preferências. Usando o padrão nesta sessão.");
			return UiSettings.defaults();
		}
	}

	private void save() {
		Path temporary = null;
		try {
			Files.createDirectories(file.getParent());
			temporary = Files.createTempFile(file.getParent(), "appearance-", ".tmp");
			UiSettings value = current();
			Properties properties = new Properties();
			properties.setProperty("theme", value.theme().name());
			properties.setProperty("fontPercent", Integer.toString(value.fontPercent()));
			properties.setProperty("highContrast", Boolean.toString(value.highContrast()));
			properties.setProperty("colorSafe", Boolean.toString(value.colorSafe()));
			properties.setProperty("reducedMotion", Boolean.toString(value.reducedMotion()));
			try (OutputStream output = Files.newOutputStream(temporary)) {
				properties.store(output, "GVS appearance preferences");
			}
			try {
				Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException exception) {
				Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
			}
			notice.set("");
		} catch (IOException exception) {
			notice.set("A preferência vale nesta sessão, mas não foi possível salvá-la neste computador.");
		} finally {
			if (temporary != null) {
				try { Files.deleteIfExists(temporary); } catch (IOException ignored) { }
			}
		}
	}
}
