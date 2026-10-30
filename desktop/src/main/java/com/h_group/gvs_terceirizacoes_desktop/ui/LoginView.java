package com.h_group.gvs_terceirizacoes_desktop.ui;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiException;
import com.h_group.gvs_terceirizacoes_desktop.dto.LoginResponse;
import com.h_group.gvs_terceirizacoes_desktop.service.AuthService;
import com.h_group.gvs_terceirizacoes_desktop.validation.AuthValidator;
import java.io.InputStream;
import java.util.Optional;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LoginView {

	private final Navigator navigator;

	private final AuthService authService;

	private final AuthValidator authValidator;

	private final String notice;

	private final TextField emailField = new TextField();

	private final PasswordField passwordField = new PasswordField();

	private final Label messageLabel = new Label();

	private final Button submitButton = new Button();

	public LoginView(Navigator navigator, AuthService authService, AuthValidator authValidator, String notice) {
		this.navigator = navigator;
		this.authService = authService;
		this.authValidator = authValidator;
		this.notice = notice;
	}

	public Parent build() {
		StackPane root = new StackPane();
		root.getStyleClass().add("auth-root");

		VBox card = new VBox();
		card.getStyleClass().add("auth-card");
		card.setAlignment(Pos.TOP_CENTER);
		card.setMaxWidth(400);
		card.setPrefWidth(400);
		card.setFillWidth(true);

		Label title = new Label("Acesso ao Sistema");
		title.getStyleClass().add("auth-title");
		VBox.setMargin(title, new Insets(0, 0, 8, 0));

		messageLabel.setWrapText(true);
		messageLabel.setMaxWidth(Double.MAX_VALUE);
		messageLabel.setVisible(false);
		messageLabel.setManaged(false);

		Label emailLabel = new Label("E-mail");
		emailLabel.getStyleClass().add("auth-label");
		emailField.getStyleClass().add("auth-input");
		emailField.setPromptText("admin@gvs.com");
		emailField.setOnAction(event -> submit());

		Label passwordLabel = new Label("Senha");
		passwordLabel.getStyleClass().add("auth-label");
		passwordField.getStyleClass().add("auth-input");
		passwordField.setOnAction(event -> submit());

		VBox emailGroup = new VBox(6, emailLabel, emailField);
		VBox passwordGroup = new VBox(6, passwordLabel, passwordField);

		submitButton.setText("Entrar");
		submitButton.getStyleClass().add("auth-button");
		submitButton.setMaxWidth(Double.MAX_VALUE);
		submitButton.setDefaultButton(true);
		submitButton.setOnAction(event -> submit());
		VBox.setMargin(submitButton, new Insets(8, 0, 0, 0));

		Hyperlink registerLink = new Hyperlink("Não tem conta? Cadastre-se");
		registerLink.getStyleClass().add("auth-link");
		registerLink.setOnAction(event -> navigator.showRegister());

		card.getChildren().addAll(createLogo(), title, messageLabel, emailGroup, passwordGroup, submitButton,
				registerLink);

		if (notice != null && !notice.isBlank()) {
			showMessage(notice, true);
		}

		root.getChildren().add(card);
		return root;
	}

	private ImageView createLogo() {
		try (InputStream stream = LoginView.class.getResourceAsStream("/images/logo.png")) {
			if (stream != null) {
				ImageView view = new ImageView(new Image(stream));
				view.setFitWidth(150);
				view.setPreserveRatio(true);
				view.setSmooth(true);
				VBox.setMargin(view, new Insets(0, 0, 12, 0));
				return view;
			}
		}
		catch (Exception exception) {
			// Ignore a missing or invalid logo.
		}
		return new ImageView();
	}

	private void submit() {
		String email = normalize(emailField.getText());
		String password = passwordField.getText() == null ? "" : passwordField.getText();

		Optional<String> validationError = authValidator.validateLogin(email, password);
		if (validationError.isPresent()) {
			showMessage(validationError.get(), false);
			return;
		}

		hideMessage();
		setLoading(true);

		Task<LoginResponse> task = new Task<>() {
			@Override
			protected LoginResponse call() {
				return authService.login(email, password);
			}
		};
		task.setOnSucceeded(event -> navigator.showHome());
		task.setOnFailed(event -> {
			setLoading(false);
			showMessage(describe(task.getException()), false);
		});
		start(task);
	}

	private void setLoading(boolean loading) {
		submitButton.setDisable(loading);
		submitButton.setText(loading ? "Entrando..." : "Entrar");
		emailField.setDisable(loading);
		passwordField.setDisable(loading);
	}

	private void showMessage(String text, boolean success) {
		messageLabel.setText(text);
		messageLabel.getStyleClass().removeAll("auth-error", "auth-success");
		messageLabel.getStyleClass().add(success ? "auth-success" : "auth-error");
		messageLabel.setVisible(true);
		messageLabel.setManaged(true);
	}

	private void hideMessage() {
		messageLabel.setVisible(false);
		messageLabel.setManaged(false);
	}

	private String describe(Throwable throwable) {
		if (throwable instanceof ApiException apiException
				&& apiException.getMessage() != null && !apiException.getMessage().isBlank()) {
			return apiException.getMessage();
		}
		return "Não foi possível conectar ao servidor. Tente novamente.";
	}

	private String normalize(String value) {
		return value == null ? "" : value.trim();
	}

	private void start(Task<?> task) {
		Thread thread = new Thread(task, "login-request");
		thread.setDaemon(true);
		thread.start();
	}

}
