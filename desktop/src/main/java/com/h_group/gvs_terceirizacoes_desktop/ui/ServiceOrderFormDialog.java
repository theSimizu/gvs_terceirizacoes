package com.h_group.gvs_terceirizacoes_desktop.ui;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiException;
import com.h_group.gvs_terceirizacoes_desktop.dto.ContractDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.ServiceOrderDTO;
import com.h_group.gvs_terceirizacoes_desktop.validation.ServiceOrderValidator;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class ServiceOrderFormDialog {

	private static final String STYLESHEET = "/css/app.css";

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final AppShell shell;

	private final ServiceOrderValidator validator;

	private final Function<ServiceOrderForm, ServiceOrderDTO> saver;

	private final Dialog<Boolean> dialog = new Dialog<>();

	private final ComboBox<ContractDTO> contractBox = new ComboBox<>();

	private final TextArea descriptionArea = new TextArea();

	private final DatePicker executionDatePicker = new DatePicker();

	private final ComboBox<String> statusBox = new ComboBox<>();

	private final Label errorLabel = new Label();

	private String submitLabel;

	private Button saveButton;

	private Button cancelButton;

	private boolean saved;

	public ServiceOrderFormDialog(AppShell shell, ServiceOrderValidator validator, String title, String submitLabel,
			ServiceOrderDTO existing, List<ContractDTO> contracts,
			Function<ServiceOrderForm, ServiceOrderDTO> saver) {
		this.shell = shell;
		this.validator = validator;
		this.saver = saver;
		build(title, submitLabel, existing, contracts);
	}

	public boolean showAndWait() {
		Optional<Boolean> result = dialog.showAndWait();
		return result.orElse(saved);
	}

	private void build(String title, String submitLabel, ServiceOrderDTO existing, List<ContractDTO> contracts) {
		this.submitLabel = submitLabel;

		dialog.setTitle(title);
		dialog.setHeaderText(null);
		dialog.setResizable(false);

		DialogPane pane = dialog.getDialogPane();
		pane.getStyleClass().add("app-dialog");
		pane.setPrefWidth(460);
		URL stylesheet = ServiceOrderFormDialog.class.getResource(STYLESHEET);
		if (stylesheet != null) {
			pane.getStylesheets().add(stylesheet.toExternalForm());
		}

		contractBox.getItems().setAll(contracts);
		contractBox.setConverter(new StringConverter<>() {
			@Override
			public String toString(ContractDTO contract) {
				return contractLabel(contract);
			}

			@Override
			public ContractDTO fromString(String text) {
				return null;
			}
		});

		descriptionArea.setPromptText("Descreva a atividade a ser executada");
		descriptionArea.setPrefRowCount(4);
		descriptionArea.setWrapText(true);

		executionDatePicker.setConverter(dateConverter());
		executionDatePicker.setPromptText("dd/mm/aaaa");

		statusBox.getItems().setAll(ServiceOrderStatuses.all());
		statusBox.setConverter(new StringConverter<>() {
			@Override
			public String toString(String status) {
				return ServiceOrderStatuses.label(status);
			}

			@Override
			public String fromString(String text) {
				return ServiceOrderStatuses.PENDING;
			}
		});
		statusBox.setValue(ServiceOrderStatuses.PENDING);

		if (existing != null) {
			selectContract(existing);
			descriptionArea.setText(existing.description());
			executionDatePicker.setValue(existing.executionDate());
			statusBox.setValue(existing.status() == null ? ServiceOrderStatuses.PENDING : existing.status());
		}

		contractBox.getStyleClass().add("form-input");
		descriptionArea.getStyleClass().add("form-input");
		executionDatePicker.getStyleClass().add("form-input");
		statusBox.getStyleClass().add("form-input");

		errorLabel.getStyleClass().add("form-error");
		errorLabel.setWrapText(true);
		errorLabel.setVisible(false);
		errorLabel.setManaged(false);

		VBox content = new VBox(12,
				field("Contrato Vinculado", contractBox),
				field("Descrição da Atividade", descriptionArea),
				field("Data de Execução", executionDatePicker),
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
		Platform.runLater(contractBox::requestFocus);
	}

	private VBox field(String label, Control control) {
		Label fieldLabel = new Label(label);
		fieldLabel.getStyleClass().add("form-label");
		control.setMaxWidth(Double.MAX_VALUE);
		return new VBox(4, fieldLabel, control);
	}

	private StringConverter<LocalDate> dateConverter() {
		return new StringConverter<>() {
			@Override
			public String toString(LocalDate date) {
				return date == null ? "" : DATE_FORMAT.format(date);
			}

			@Override
			public LocalDate fromString(String text) {
				if (text == null || text.isBlank()) {
					return null;
				}
				try {
					return LocalDate.parse(text.trim(), DATE_FORMAT);
				}
				catch (DateTimeParseException exception) {
					return null;
				}
			}
		};
	}

	private void selectContract(ServiceOrderDTO existing) {
		UUID id = existing.contractId() != null ? existing.contractId()
				: existing.contract() != null ? existing.contract().id() : null;
		if (id == null) {
			return;
		}
		for (ContractDTO contract : contractBox.getItems()) {
			if (id.equals(contract.id())) {
				contractBox.setValue(contract);
				return;
			}
		}
		if (existing.contract() != null) {
			contractBox.getItems().add(existing.contract());
			contractBox.setValue(existing.contract());
		}
	}

	private String contractLabel(ContractDTO contract) {
		if (contract == null) {
			return "";
		}
		String client = contract.client() != null && contract.client().legalName() != null
				? contract.client().legalName() : "Cliente não informado";
		String provider = contract.provider() != null && contract.provider().name() != null
				? contract.provider().name() : "Prestador não informado";
		String value = contract.agreedValue() == null ? "valor não informado"
				: InputMasks.formatCurrency(contract.agreedValue());
		String label = client + " — " + provider + " — " + value;
		if ("closed".equalsIgnoreCase(contract.status())) {
			label = label + " (encerrado)";
		}
		return label;
	}

	private void submit() {
		UUID contractId = contractBox.getValue() == null ? null : contractBox.getValue().id();
		String description = descriptionArea.getText();

		Optional<String> error = validator.validate(contractId, description, executionDatePicker.getValue());
		if (error.isPresent()) {
			showError(error.get());
			return;
		}
		hideError();

		ServiceOrderForm form = new ServiceOrderForm(contractId, description.trim(),
				executionDatePicker.getValue(),
				statusBox.getValue() == null ? ServiceOrderStatuses.PENDING : statusBox.getValue());
		save(form);
	}

	private void save(ServiceOrderForm form) {
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
		contractBox.setDisable(saving);
		descriptionArea.setDisable(saving);
		executionDatePicker.setDisable(saving);
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
		return "Não foi possível salvar a ordem de serviço. Verifique a conexão com o servidor.";
	}

}
