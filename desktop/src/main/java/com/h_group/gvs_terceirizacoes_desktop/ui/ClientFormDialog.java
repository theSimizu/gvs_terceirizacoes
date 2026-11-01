package com.h_group.gvs_terceirizacoes_desktop.ui;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiException;
import com.h_group.gvs_terceirizacoes_desktop.dto.ClientDTO;
import com.h_group.gvs_terceirizacoes_desktop.validation.ClientValidator;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class ClientFormDialog {

	private static final String ACTIVE = "active";

	private static final String INACTIVE = "inactive";

	private static final String STYLESHEET = "/css/app.css";

	private final AppShell shell;

	private final ClientValidator validator;

	private final Function<ClientForm, ClientDTO> saver;

	private final Dialog<Boolean> dialog = new Dialog<>();

	private final TextField legalNameField = new TextField();

	private final TextField cnpjField = new TextField();

	private final TextField emailField = new TextField();

	private final TextField phoneField = new TextField();

	private final ComboBox<String> statusBox = new ComboBox<>();

	private final Label errorLabel = new Label();

	private String submitLabel;

	private Button saveButton;

	private Button cancelButton;

	private boolean saved;

	public ClientFormDialog(AppShell shell, ClientValidator validator, String title, String submitLabel,
			ClientDTO existing, Function<ClientForm, ClientDTO> saver) {
		this.shell = shell;
		this.validator = validator;
		this.saver = saver;
		build(title, submitLabel, existing);
	}

	public boolean showAndWait() {
		Optional<Boolean> result = dialog.showAndWait();
		return result.orElse(saved);
	}

	private void build(String title, String submitLabel, ClientDTO existing) {
		this.submitLabel = submitLabel;

		dialog.setTitle(title);
		dialog.setHeaderText(null);
		dialog.setResizable(false);

		DialogPane pane = dialog.getDialogPane();
		pane.getStyleClass().add("app-dialog");
		pane.setPrefWidth(420);
		URL stylesheet = ClientFormDialog.class.getResource(STYLESHEET);
		if (stylesheet != null) {
			pane.getStylesheets().add(stylesheet.toExternalForm());
		}

		cnpjField.setTextFormatter(InputMasks.cnpj());
		phoneField.setTextFormatter(InputMasks.phone());
		cnpjField.setPromptText("00.000.000/0000-00");
		phoneField.setPromptText("(00) 00000-0000");
		emailField.setPromptText("contato@empresa.com");

		statusBox.getItems().setAll(ACTIVE, INACTIVE);
		statusBox.setConverter(new StringConverter<>() {
			@Override
			public String toString(String value) {
				if (value == null) {
					return "";
				}
				return ACTIVE.equals(value) ? "Ativo" : "Inativo";
			}

			@Override
			public String fromString(String text) {
				return "Inativo".equalsIgnoreCase(text) ? INACTIVE : ACTIVE;
			}
		});
		statusBox.setValue(ACTIVE);

		if (existing != null) {
			legalNameField.setText(existing.legalName());
			cnpjField.setText(existing.cnpj());
			emailField.setText(existing.email());
			phoneField.setText(existing.phone());
			statusBox.setValue(ACTIVE.equalsIgnoreCase(existing.status()) ? ACTIVE : INACTIVE);
		}

		for (TextField field : List.of(legalNameField, cnpjField, emailField, phoneField)) {
			field.getStyleClass().add("form-input");
		}
		statusBox.getStyleClass().add("form-input");

		errorLabel.getStyleClass().add("form-error");
		errorLabel.setWrapText(true);
		errorLabel.setVisible(false);
		errorLabel.setManaged(false);

		VBox content = new VBox(12,
				field("Razão Social", legalNameField),
				field("CNPJ", cnpjField),
				field("E-mail", emailField),
				field("Telefone", phoneField),
				field("Status", statusBox),
				errorLabel);
		content.getStyleClass().add("dialog-content");
		content.setPadding(new Insets(8, 0, 0, 0));
		pane.setContent(content);

		ButtonType save = new ButtonType(submitLabel, ButtonBar.ButtonData.OK_DONE);
		ButtonType cancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
		pane.getButtonTypes().setAll(save, cancel);

		saveButton = (Button) pane.lookupButton(save);
		cancelButton = (Button) pane.lookupButton(cancel);
		saveButton.addEventFilter(ActionEvent.ACTION, event -> {
			event.consume();
			submit();
		});

		dialog.setResultConverter(button -> saved);
		Platform.runLater(legalNameField::requestFocus);
	}

	private VBox field(String label, Control control) {
		Label fieldLabel = new Label(label);
		fieldLabel.getStyleClass().add("form-label");
		control.setMaxWidth(Double.MAX_VALUE);
		return new VBox(4, fieldLabel, control);
	}

	private void submit() {
		Optional<String> error = validator.validate(legalNameField.getText(), cnpjField.getText(),
				emailField.getText(), phoneField.getText());
		if (error.isPresent()) {
			showError(error.get());
			return;
		}
		hideError();

		ClientForm form = new ClientForm(
				legalNameField.getText() == null ? "" : legalNameField.getText().trim(),
				cnpjField.getText(),
				emailField.getText() == null ? "" : emailField.getText().trim(),
				phoneField.getText(),
				statusBox.getValue());

		setSaving(true);
		FxAsync.run(() -> saver.apply(form), result -> {
			saved = true;
			dialog.close();
		}, failure -> {
			setSaving(false);
			if (failure instanceof ApiException apiException && apiException.getStatus() == 401) {
				dialog.close();
				shell.sessionExpired();
				return;
			}
			showError(describe(failure));
		});
	}

	private void setSaving(boolean saving) {
		saveButton.setDisable(saving);
		saveButton.setText(saving ? "Salvando..." : submitLabel);
		cancelButton.setDisable(saving);
		legalNameField.setDisable(saving);
		cnpjField.setDisable(saving);
		emailField.setDisable(saving);
		phoneField.setDisable(saving);
		statusBox.setDisable(saving);
	}

	private void showError(String text) {
		errorLabel.setText(text);
		errorLabel.setVisible(true);
		errorLabel.setManaged(true);
	}

	private void hideError() {
		errorLabel.setVisible(false);
		errorLabel.setManaged(false);
	}

	private String describe(Throwable throwable) {
		if (throwable instanceof ApiException apiException
				&& apiException.getMessage() != null && !apiException.getMessage().isBlank()) {
			return apiException.getMessage();
		}
		return "Não foi possível salvar o cliente. Verifique a conexão com o servidor.";
	}

}
