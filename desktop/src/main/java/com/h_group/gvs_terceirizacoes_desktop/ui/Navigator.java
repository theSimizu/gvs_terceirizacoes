package com.h_group.gvs_terceirizacoes_desktop.ui;

import com.h_group.gvs_terceirizacoes_desktop.service.AuthService;
import com.h_group.gvs_terceirizacoes_desktop.session.AuthSession;
import com.h_group.gvs_terceirizacoes_desktop.validation.AuthValidator;
import java.io.InputStream;
import java.net.URL;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class Navigator {

	private static final String TITLE = "GVS Terceirizações";

	private static final String STYLESHEET = "/css/auth.css";

	private static final String LOGO = "/images/logo.png";

	private final AuthService authService;

	private final AuthSession authSession;

	private final AuthValidator authValidator;

	private Stage stage;

	public Navigator(AuthService authService, AuthSession authSession, AuthValidator authValidator) {
		this.authService = authService;
		this.authSession = authSession;
		this.authValidator = authValidator;
	}

	public void initialize(Stage stage) {
		this.stage = stage;
		stage.setTitle(TITLE);
		loadIcon();
		stage.setWidth(900);
		stage.setHeight(650);
		stage.setMinWidth(800);
		stage.setMinHeight(600);
	}

	public void showLogin() {
		showLogin(null);
	}

	public void showLogin(String notice) {
		display(new LoginView(this, authService, authValidator, notice).build());
	}

	public void showRegister() {
		display(new RegisterView(this, authService, authValidator).build());
	}

	public void showHome() {
		display(new HomeView(this, authSession).build());
	}

	private void display(Parent root) {
		Scene scene = new Scene(root);
		URL stylesheet = Navigator.class.getResource(STYLESHEET);
		if (stylesheet != null) {
			scene.getStylesheets().add(stylesheet.toExternalForm());
		}
		stage.setScene(scene);
		if (!stage.isShowing()) {
			stage.show();
			stage.centerOnScreen();
		}
	}

	private void loadIcon() {
		try (InputStream stream = Navigator.class.getResourceAsStream(LOGO)) {
			if (stream != null) {
				stage.getIcons().add(new Image(stream));
			}
		}
		catch (Exception exception) {
			// The icon is decorative; ignore failures.
		}
	}

}
