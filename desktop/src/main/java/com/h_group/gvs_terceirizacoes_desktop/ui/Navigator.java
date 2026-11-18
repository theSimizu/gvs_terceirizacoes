package com.h_group.gvs_terceirizacoes_desktop.ui;

import com.h_group.gvs_terceirizacoes_desktop.service.AuthService;
import com.h_group.gvs_terceirizacoes_desktop.session.AuthSession;
import com.h_group.gvs_terceirizacoes_desktop.validation.AuthValidator;
import java.io.InputStream;
import java.net.URL;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

/**
 * Alterna as cenas de login, cadastro e área autenticada no Stage principal.
 * Centraliza estilos e montagem da navegação JavaFX.
 */
@Component
@RequiredArgsConstructor
public class Navigator {
	private static final String TITLE = "GVS Terceirizações";
	private static final String AUTH_STYLESHEET = "/css/auth.css";
	private static final String APP_STYLESHEET = "/css/app.css";
	private static final String LOGO = "/images/logo.png";

	private final AuthService authService;
	private final AuthSession authSession;
	private final AuthValidator authValidator;
	private final PageDependencies dependencies;

	private Stage stage;

	public void initialize(Stage stage) {
		this.stage = stage;
		stage.initStyle(StageStyle.DECORATED);
		stage.setResizable(true);
		stage.setTitle(TITLE);
		loadIcon();
		Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
		double availableWidth = Math.max(1, bounds.getWidth() - 32);
		double availableHeight = Math.max(1, bounds.getHeight() - 32);
		stage.setMinWidth(Math.min(1024, availableWidth));
		stage.setMinHeight(Math.min(700, availableHeight));
		stage.setWidth(Math.min(1280, availableWidth));
		stage.setHeight(Math.min(800, availableHeight));
	}

	public void showLogin() {
		showLogin(null);
	}

	public void showLoading() {
		Label title = new Label("Preparando seu ambiente");
		title.getStyleClass().add("auth-title");
		Label description = new Label("Verificando sua sessão…");
		description.getStyleClass().add("auth-subtitle");
		VBox loading = new VBox(18, title, description, new ProgressIndicator());
		loading.getStyleClass().add("auth-card");
		display(AuthLayout.wrap(loading));
	}

	public void showLogin(String notice) {
		display(new LoginView(this, authService, authValidator, notice).build());
	}

	public void showRegister() {
		display(new RegisterView(this, authService, authValidator).build());
	}

	public void showHome() {
		display(new AppShell(this, authService, authSession, dependencies).build());
	}

	private void display(Parent root) {
		Scene scene = new Scene(AppearanceControls.wrap(root, stage));
		addStylesheet(scene, AUTH_STYLESHEET);
		addStylesheet(scene, APP_STYLESHEET);
		AppearanceControls.shortcuts(scene, stage);
		stage.setScene(scene);
		if (!stage.isShowing()) {
			stage.show();
			stage.centerOnScreen();
		}
	}

	private void addStylesheet(Scene scene, String path) {
		URL stylesheet = Navigator.class.getResource(path);
		if (stylesheet != null) {
			scene.getStylesheets().add(stylesheet.toExternalForm());
		}
	}

	private void loadIcon() {
		try (InputStream stream = Navigator.class.getResourceAsStream(LOGO)) {
			if (stream != null) {
				stage.getIcons().add(new Image(stream));
			}
		} catch (Exception exception) {
			// The icon is decorative; ignore failures.
		}
	}

}
