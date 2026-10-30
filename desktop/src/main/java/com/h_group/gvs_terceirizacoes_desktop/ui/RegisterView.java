package com.h_group.gvs_terceirizacoes_desktop.ui;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiException;
import com.h_group.gvs_terceirizacoes_desktop.dto.RegisterResponse;
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

public class RegisterView {

	private final Navigator navigator;

	private final AuthService authService;

	private final AuthValidator authValidator;

	private final TextField nameField = new TextField();

	private final TextField emailField = new TextField();

	private final PasswordField passwordField = new PasswordField();

	private final PasswordField confirmationField = new PasswordField();

	private final Label messageLabel = new Label();

	private final Button submitButton = new Button();

	public RegisterView(Navigator navigator, AuthService authService, AuthValidator authValidator) {
		this.navigator = navigator;
		this.authService = authService;
		this.authValidator = authValidator;
	}

	public Parent build() {
		StackPane root = new StackPane();
		root.getStyleClass().add("auth-root");

		VBox card = new VBox();
		card.getStyleClass().addAll("auth-card", "auth-card-register");
		card.setAlignment(Pos.TOP_CENTER);
		card.setMaxWidth(420);
		card.setPrefWidth(420);
		card.setFillWidth(true);

		Label title = new Label("Criar Conta");
		title.getStyleClass().add("auth-title");

		messageLabel.setWrapText(true);
		messageLabel.setMaxWidth(Double.MAX_VALUE);
		messageLabel.setVisible(false);
		messageLabel.setManaged(false);

		VBox nameGroup = fieldGroup("Nome completo", nameField, "Seu nome");
		VBox emailGroup = fieldGroup("E-mail", emailField, "novo@gvs.com");
		VBox passwordGroup = fieldGroup("Senha", passwordField, "");
		VBox confirmationGroup = fieldGroup("Confirmar Senha", confirmationField, "");

		passwordField.setOnAction(event -> submit());
		confirmationField.setOnAction(event -> submit());

		Label hint = new Label("Requisitos: Mín. 8 caracteres, 1 Maiúscula, 1 Número, "
				+ "1 Caractere Especial (!@#$).");
		hint.getStyleClass().add("auth-hint");
		hint.setWrapText(true);
		hint.setMaxWidth(Double.MAX_VALUE);

		submitButton.setText("Cadastrar");
		submitButton.getStyleClass().add("auth-button");
		submitButton.setMaxWidth(Double.MAX_VALUE);
		submitButton.setDefaultButton(true);
		submitButton.setOnAction(event -> submit());
		VBox.setMargin(submitButton, new Insets(4, 0, 0, 0));

		Hyperlink loginLink = new Hyperlink("Já tem conta? Entrar");
		loginLink.getStyleClass().add("auth-link");
		loginLink.setOnAction(event -> navigator.showLogin());

		card.getChildren().addAll(createLogo(), title, messageLabel, nameGroup, emailGroup, passwordGroup,
				confirmationGroup, hint, submitButton, loginLink);

		root.getChildren().add(card);
		return root;
	}

	private VBox fieldGroup(String labelText, TextField input, String prompt) {
		Label label = new Label(labelText);
		label.getStyleClass().add("auth-label");
		input.getStyleClass().add("auth-input");
		if (prompt != null && !prompt.isBlank()) {
			input.setPromptText(prompt);
		}
		input.setOnAction(event -> submit());
		return new VBox(4, label, input);
	}

	private ImageView createLogo() {
		try (InputStream stream = RegisterView.class.getResourceAsStream("/images/logo.png")) {
			if (stream != null) {
				ImageView view = new ImageView(new Image(stream));
				view.setFitWidth(110);
				view.setPreserveRatio(true);
				view.setSmooth(true);
				VBox.setMargin(view, new Insets(0, 0, 6, 0));
				return view;
			}
		}
		catch (Exception exception) {
			// Ignore a missing or invalid logo.
		}
		return new ImageView();
	}

	private void submit() {
		String name = normalize(nameField.getText());
		String email = normalize(emailField.getText());
		String password = passwordField.getText() == null ? "" : passwordField.getText();
		String confirmation = confirmationField.getText() == null ? "" : confirmationField.getText();

		Optional<String> validationError =
				authValidator.validateRegistration(name, email, password, confirmation);
		if (validationError.isPresent()) {
			showMessage(validationError.get(), false);
			return;
		}

		hideMessage();
		setLoading(true);

		Task<RegisterResponse> task = new Task<>() {
			@Override
			protected RegisterResponse call() {
				return authService.register(name, email, password);
			}
		};
		task.setOnSucceeded(event -> {
			RegisterResponse response = task.getValue();
			if (response != null && response.token() != null && !response.token().isBlank()) {
				navigator.showHome();
			}
			else {
				navigator.showLogin("Usuário criado com sucesso. Faça login para continuar.");
			}
		});
		task.setOnFailed(event -> {
			setLoading(false);
			showMessage(describe(task.getException()), false);
		});
		start(task);
	}

	private void setLoading(boolean loading) {
		submitButton.setDisable(loading);
		submitButton.setText(loading ? "Cadastrando..." : "Cadastrar");
		nameField.setDisable(loading);
		emailField.setDisable(loading);
		passwordField.setDisable(loading);
		confirmationField.setDisable(loading);
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
		Thread thread = new Thread(task, "register-request");
		thread.setDaemon(true);
		thread.start();
	}

}
