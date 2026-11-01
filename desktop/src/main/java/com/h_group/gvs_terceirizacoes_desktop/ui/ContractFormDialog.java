package com.h_group.gvs_terceirizacoes_desktop.ui;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiException;
import com.h_group.gvs_terceirizacoes_desktop.dto.ClientDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.ContractDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.ProviderDTO;
import com.h_group.gvs_terceirizacoes_desktop.validation.ContractValidator;
import java.math.BigDecimal;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class ContractFormDialog {

	private static final String ACTIVE = "active";

	private static final String CLOSED = "closed";

	private static final String STYLESHEET = "/css/app.css";

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final AppShell shell;

	private final ContractValidator validator;

	private final Function<ContractForm, ContractDTO> saver;

	private final Dialog<Boolean> dialog = new Dialog<>();

	private final ComboBox<ClientDTO> clientBox = new ComboBox<>();

	private final ComboBox<ProviderDTO> providerBox = new ComboBox<>();

	private final TextField valueField = new TextField();

	private final DatePicker startDatePicker = new DatePicker();

	private final DatePicker endDatePicker = new DatePicker();

	private final ComboBox<String> statusBox = new ComboBox<>();

	private final Label errorLabel = new Label();

	private String submitLabel;

	private Button saveButton;

	private Button cancelButton;

	private boolean saved;

	public ContractFormDialog(AppShell shell, ContractValidator validator, String title, String submitLabel,
			ContractDTO existing, List<ClientDTO> clients, List<ProviderDTO> providers,
			Function<ContractForm, ContractDTO> saver) {
		this.shell = shell;
		this.validator = validator;
		this.saver = saver;
		build(title, submitLabel, existing, clients, providers);
	}

	public boolean showAndWait() {
		Optional<Boolean> result = dialog.showAndWait();
		return result.orElse(saved);
	}

	private void build(String title, String submitLabel, ContractDTO existing, List<ClientDTO> clients,
			List<ProviderDTO> providers) {
		this.submitLabel = submitLabel;

		dialog.setTitle(title);
		dialog.setHeaderText(null);
		dialog.setResizable(false);

		DialogPane pane = dialog.getDialogPane();
		pane.getStyleClass().add("app-dialog");
		pane.setPrefWidth(440);
		URL stylesheet = ContractFormDialog.class.getResource(STYLESHEET);
		if (stylesheet != null) {
			pane.getStylesheets().add(stylesheet.toExternalForm());
		}

		clientBox.getItems().setAll(clients);
		clientBox.setConverter(new StringConverter<>() {
			@Override
			public String toString(ClientDTO client) {
				return client == null || client.legalName() == null ? "" : client.legalName();
			}

			@Override
			public ClientDTO fromString(String text) {
				return null;
			}
		});

		providerBox.getItems().setAll(providers);
		providerBox.setConverter(new StringConverter<>() {
			@Override
			public String toString(ProviderDTO provider) {
				if (provider == null || provider.name() == null) {
					return "";
				}
				if (provider.specialty() == null || provider.specialty().isBlank()) {
					return provider.name();
				}
				return provider.name() + " (" + provider.specialty() + ")";
			}

			@Override
			public ProviderDTO fromString(String text) {
				return null;
			}
		});

		valueField.setTextFormatter(InputMasks.currency());
		valueField.setPromptText("R$ 0,00");

		startDatePicker.setConverter(dateConverter());
		startDatePicker.setPromptText("dd/mm/aaaa");
		endDatePicker.setConverter(dateConverter());
		endDatePicker.setPromptText("dd/mm/aaaa");

		statusBox.getItems().setAll(ACTIVE, CLOSED);
		statusBox.setConverter(new StringConverter<>() {
			@Override
			public String toString(String value) {
				if (value == null) {
					return "";
				}
				return CLOSED.equals(value) ? "Encerrado" : "Ativo";
			}

			@Override
			public String fromString(String text) {
				return "Encerrado".equalsIgnoreCase(text) ? CLOSED : ACTIVE;
			}
		});
		statusBox.setValue(ACTIVE);

		if (existing != null) {
			selectClient(existing);
			selectProvider(existing);
			valueField.setText(InputMasks.formatCurrency(existing.agreedValue()));
			startDatePicker.setValue(existing.startDate());
			endDatePicker.setValue(existing.endDate());
			statusBox.setValue(CLOSED.equalsIgnoreCase(existing.status()) ? CLOSED : ACTIVE);
		}

		clientBox.getStyleClass().add("form-input");
		providerBox.getStyleClass().add("form-input");
		valueField.getStyleClass().add("form-input");
		startDatePicker.getStyleClass().add("form-input");
		endDatePicker.getStyleClass().add("form-input");
		statusBox.getStyleClass().add("form-input");

		errorLabel.getStyleClass().add("form-error");
		errorLabel.setWrapText(true);
		errorLabel.setVisible(false);
		errorLabel.setManaged(false);

		VBox content = new VBox(12,
				field("Cliente", clientBox),
				field("Prestador", providerBox),
				field("Valor Acordado", valueField),
				field("Data de Início", startDatePicker),
				field("Data de Término", endDatePicker),
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
		Platform.runLater(clientBox::requestFocus);
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

	private void selectClient(ContractDTO existing) {
		UUID id = existing.clientId() != null ? existing.clientId()
				: existing.client() != null ? existing.client().id() : null;
		if (id == null) {
			return;
		}
		for (ClientDTO client : clientBox.getItems()) {
			if (id.equals(client.id())) {
				clientBox.setValue(client);
				return;
			}
		}
		if (existing.client() != null) {
			clientBox.getItems().add(existing.client());
			clientBox.setValue(existing.client());
		}
	}

	private void selectProvider(ContractDTO existing) {
		UUID id = existing.providerId() != null ? existing.providerId()
				: existing.provider() != null ? existing.provider().id() : null;
		if (id == null) {
			return;
		}
		for (ProviderDTO provider : providerBox.getItems()) {
			if (id.equals(provider.id())) {
				providerBox.setValue(provider);
				return;
			}
		}
		if (existing.provider() != null) {
			providerBox.getItems().add(existing.provider());
			providerBox.setValue(existing.provider());
		}
	}

	private void submit() {
		UUID clientId = clientBox.getValue() == null ? null : clientBox.getValue().id();
		UUID providerId = providerBox.getValue() == null ? null : providerBox.getValue().id();
		BigDecimal agreedValue = InputMasks.parseCurrency(valueField.getText());

		Optional<String> error = validator.validate(clientId, providerId,
				startDatePicker.getValue(), endDatePicker.getValue(), agreedValue);
		if (error.isPresent()) {
			showError(error.get());
			return;
		}
		hideError();

		ContractForm form = new ContractForm(clientId, providerId, startDatePicker.getValue(),
				endDatePicker.getValue(), agreedValue, statusBox.getValue());
		save(form);
	}

	private void save(ContractForm form) {
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
		clientBox.setDisable(saving);
		providerBox.setDisable(saving);
		valueField.setDisable(saving);
		startDatePicker.setDisable(saving);
		endDatePicker.setDisable(saving);
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
		return "Não foi possível salvar o contrato. Verifique a conexão com o servidor.";
	}

}
