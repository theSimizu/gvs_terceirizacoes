package com.h_group.gvs_terceirizacoes_desktop.ui;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiException;
import com.h_group.gvs_terceirizacoes_desktop.dto.ProviderDTO;
import com.h_group.gvs_terceirizacoes_desktop.validation.ProviderValidator;
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

public class ProviderFormDialog {

	private static final String STYLESHEET = "/css/app.css";

	private final AppShell shell;

	private final ProviderValidator validator;

	private final Function<ProviderForm, ProviderDTO> saver;

	private final Dialog<Boolean> dialog = new Dialog<>();

	private final TextField nameField = new TextField();

	private final TextField taxIdField = new TextField();

	private final TextField specialtyField = new TextField();

	private final TextField emailField = new TextField();

	private final TextField phoneField = new TextField();

	private final ComboBox<Boolean> availabilityBox = new ComboBox<>();

	private final Label errorLabel = new Label();

	private String submitLabel;

	private Button saveButton;

	private Button cancelButton;

	private boolean saved;

	public ProviderFormDialog(AppShell shell, ProviderValidator validator, String title, String submitLabel,
			ProviderDTO existing, Function<ProviderForm, ProviderDTO> saver) {
		this.shell = shell;
		this.validator = validator;
		this.saver = saver;
		build(title, submitLabel, existing);
	}

	public boolean showAndWait() {
		Optional<Boolean> result = dialog.showAndWait();
		return result.orElse(saved);
	}

	private void build(String title, String submitLabel, ProviderDTO existing) {
		this.submitLabel = submitLabel;

		dialog.setTitle(title);
		dialog.setHeaderText(null);
		dialog.setResizable(false);

		DialogPane pane = dialog.getDialogPane();
		pane.getStyleClass().add("app-dialog");
		pane.setPrefWidth(420);
		URL stylesheet = ProviderFormDialog.class.getResource(STYLESHEET);
		if (stylesheet != null) {
			pane.getStylesheets().add(stylesheet.toExternalForm());
		}

		taxIdField.setTextFormatter(InputMasks.cpfCnpj());
		phoneField.setTextFormatter(InputMasks.phone());
		taxIdField.setPromptText("CPF ou CNPJ");
		phoneField.setPromptText("(00) 00000-0000");
		emailField.setPromptText("contato@empresa.com");
		specialtyField.setPromptText("Ex.: Elétrica, Hidráulica, Limpeza");

		availabilityBox.getItems().setAll(true, false);
		availabilityBox.setConverter(new StringConverter<>() {
			@Override
			public String toString(Boolean value) {
				if (value == null) {
					return "";
				}
				return value ? "Disponível" : "Ocupado";
			}

			@Override
			public Boolean fromString(String text) {
				return !"Ocupado".equalsIgnoreCase(text);
			}
		});
		availabilityBox.setValue(true);

		if (existing != null) {
			nameField.setText(existing.name());
			taxIdField.setText(existing.taxId());
			specialtyField.setText(existing.specialty());
			emailField.setText(existing.email());
			phoneField.setText(existing.phone());
			availabilityBox.setValue(existing.available());
		}

		for (TextField field : List.of(nameField, taxIdField, specialtyField, emailField, phoneField)) {
			field.getStyleClass().add("form-input");
		}
		availabilityBox.getStyleClass().add("form-input");

		errorLabel.getStyleClass().add("form-error");
		errorLabel.setWrapText(true);
		errorLabel.setVisible(false);
		errorLabel.setManaged(false);

		VBox content = new VBox(12,
				field("Nome Completo", nameField),
				field("CPF/CNPJ", taxIdField),
				field("Especialidade / Serviço", specialtyField),
				field("E-mail", emailField),
				field("Telefone", phoneField),
				field("Disponibilidade", availabilityBox),
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
		Platform.runLater(nameField::requestFocus);
	}

	private VBox field(String label, Control control) {
		Label fieldLabel = new Label(label);
		fieldLabel.getStyleClass().add("form-label");
		control.setMaxWidth(Double.MAX_VALUE);
		return new VBox(4, fieldLabel, control);
	}

	private void submit() {
		Optional<String> error = validator.validate(nameField.getText(), taxIdField.getText(),
				specialtyField.getText(), emailField.getText(), phoneField.getText());
		if (error.isPresent()) {
			showError(error.get());
			return;
		}
		hideError();

		ProviderForm form = new ProviderForm(
				nameField.getText().trim(),
				taxIdField.getText(),
				specialtyField.getText().trim(),
				emailField.getText().trim(),
				phoneField.getText(),
				available());
		save(form);
	}

	private void save(ProviderForm form) {
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

	private boolean available() {
		return !Boolean.FALSE.equals(availabilityBox.getValue());
	}

	private void setSaving(boolean saving) {
		saveButton.setDisable(saving);
		saveButton.setText(saving ? "Salvando..." : submitLabel);
		cancelButton.setDisable(saving);
		nameField.setDisable(saving);
		taxIdField.setDisable(saving);
		specialtyField.setDisable(saving);
		emailField.setDisable(saving);
		phoneField.setDisable(saving);
		availabilityBox.setDisable(saving);
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
		return "Não foi possível salvar o prestador. Verifique a conexão com o servidor.";
	}

}
