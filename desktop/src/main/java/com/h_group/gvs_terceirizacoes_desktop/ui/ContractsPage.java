package com.h_group.gvs_terceirizacoes_desktop.ui;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiException;
import com.h_group.gvs_terceirizacoes_desktop.dto.ClientDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.ContractDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.ProviderDTO;
import com.h_group.gvs_terceirizacoes_desktop.service.AuditLog;
import com.h_group.gvs_terceirizacoes_desktop.service.ClientService;
import com.h_group.gvs_terceirizacoes_desktop.service.ContractService;
import com.h_group.gvs_terceirizacoes_desktop.service.ProviderService;
import com.h_group.gvs_terceirizacoes_desktop.validation.ContractValidator;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ContractsPage implements Page {

	private static final int PAGE_SIZE = 10;

	private static final String ACTIVE = "active";

	private static final String STYLESHEET = "/css/app.css";

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final AppShell shell;

	private final ContractService contractService;

	private final ContractValidator contractValidator;

	private final ClientService clientService;

	private final ProviderService providerService;

	private final AuditLog auditLog;

	private final List<ContractDTO> allContracts = new ArrayList<>();

	private final List<ContractDTO> visibleContracts = new ArrayList<>();

	private final ObservableList<ContractDTO> pageItems = FXCollections.observableArrayList();

	private final Map<String, Button> headers = new HashMap<>();

	private final TableView<ContractDTO> table = new TableView<>(pageItems);

	private final TextField searchField = new TextField();

	private final Label countLabel = new Label();

	private final Label pageLabel = new Label();

	private final Button refreshButton = new Button("Atualizar");

	private final Button newButton = new Button("Novo Contrato");

	private final Button previousButton = new Button("Anterior");

	private final Button nextButton = new Button("Próxima");

	private final HBox actions = new HBox(12, refreshButton, newButton);

	private String sortField = "client";

	private boolean ascending = true;

	private int currentPage = 1;

	private int pageCount = 1;

	public ContractsPage(AppShell shell, ContractService contractService, ContractValidator contractValidator,
			ClientService clientService, ProviderService providerService, AuditLog auditLog) {
		this.shell = shell;
		this.contractService = contractService;
		this.contractValidator = contractValidator;
		this.clientService = clientService;
		this.providerService = providerService;
		this.auditLog = auditLog;
		configureActions();
		configureTable();
		configurePagination();
	}

	@Override
	public String title() {
		return "Contratos";
	}

	@Override
	public Node build() {
		searchField.getStyleClass().add("search-field");
		searchField.setPromptText("Buscar por cliente ou prestador");
		searchField.textProperty().addListener((observable, oldValue, newValue) -> {
			currentPage = 1;
			refreshView();
		});

		countLabel.getStyleClass().add("page-label");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		HBox toolbar = new HBox(12, searchField, spacer, countLabel);
		toolbar.getStyleClass().add("toolbar");
		toolbar.setAlignment(Pos.CENTER_LEFT);

		Label placeholder = new Label("Nenhum registro encontrado.");
		placeholder.getStyleClass().add("table-placeholder");
		table.setPlaceholder(placeholder);
		VBox.setVgrow(table, Priority.ALWAYS);

		HBox pagination = new HBox(12, previousButton, pageLabel, nextButton);
		pagination.getStyleClass().add("pagination-bar");
		pagination.setAlignment(Pos.CENTER_RIGHT);

		refreshView();
		return new VBox(16, toolbar, table, pagination);
	}

	@Override
	public Node action() {
		return actions;
	}

	@Override
	public void onShow() {
		load();
	}

	private void configureActions() {
		refreshButton.getStyleClass().add("btn-secondary");
		refreshButton.setOnAction(event -> load());

		newButton.getStyleClass().add("btn-primary");
		newButton.setOnAction(event -> openForm(null));

		actions.setAlignment(Pos.CENTER_RIGHT);
	}

	private void configureTable() {
		table.getStyleClass().add("data-table");
		table.setEditable(false);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

		TableColumn<ContractDTO, String> client = sortedColumn("client", "Cliente",
				contract -> clientName(contract));
		client.setPrefWidth(200);
		TableColumn<ContractDTO, String> provider = sortedColumn("provider", "Prestador",
				contract -> providerName(contract));
		provider.setPrefWidth(200);
		TableColumn<ContractDTO, LocalDate> startDate = dateColumn("startDate", "Início",
				ContractDTO::startDate);
		startDate.setPrefWidth(110);
		TableColumn<ContractDTO, LocalDate> endDate = dateColumn("endDate", "Término",
				ContractDTO::endDate);
		endDate.setPrefWidth(110);
		TableColumn<ContractDTO, BigDecimal> agreedValue = valueColumn();
		agreedValue.setPrefWidth(130);
		TableColumn<ContractDTO, String> status = statusColumn();
		status.setPrefWidth(120);
		TableColumn<ContractDTO, Void> actionsColumn = actionsColumn();
		actionsColumn.setPrefWidth(220);

		table.getColumns().setAll(client, provider, startDate, endDate, agreedValue, status, actionsColumn);
	}

	private void configurePagination() {
		previousButton.getStyleClass().add("btn-secondary");
		nextButton.getStyleClass().add("btn-secondary");
		pageLabel.getStyleClass().add("page-label");

		previousButton.setOnAction(event -> changePage(-1));
		nextButton.setOnAction(event -> changePage(1));
	}

	private TableColumn<ContractDTO, String> sortedColumn(String field, String title,
			Function<ContractDTO, String> value) {
		TableColumn<ContractDTO, String> column = new TableColumn<>();
		column.setGraphic(header(field, title));
		column.setSortable(false);
		column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(value.apply(data.getValue())));
		return column;
	}

	private TableColumn<ContractDTO, LocalDate> dateColumn(String field, String title,
			Function<ContractDTO, LocalDate> value) {
		TableColumn<ContractDTO, LocalDate> column = new TableColumn<>();
		column.setGraphic(header(field, title));
		column.setSortable(false);
		column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(value.apply(data.getValue())));
		column.setCellFactory(ignored -> new TableCell<>() {
			@Override
			protected void updateItem(LocalDate date, boolean empty) {
				super.updateItem(date, empty);
				if (empty) {
					setText(null);
					return;
				}
				setText(date == null ? "—" : DATE_FORMAT.format(date));
			}
		});
		return column;
	}

	private TableColumn<ContractDTO, BigDecimal> valueColumn() {
		TableColumn<ContractDTO, BigDecimal> column = new TableColumn<>();
		column.setGraphic(header("agreedValue", "Valor"));
		column.setSortable(false);
		column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().agreedValue()));
		column.setCellFactory(ignored -> new TableCell<>() {
			@Override
			protected void updateItem(BigDecimal value, boolean empty) {
				super.updateItem(value, empty);
				if (empty) {
					setText(null);
					return;
				}
				setText(value == null ? "—" : InputMasks.formatCurrency(value));
			}
		});
		return column;
	}

	private TableColumn<ContractDTO, String> statusColumn() {
		TableColumn<ContractDTO, String> column = new TableColumn<>();
		column.setGraphic(header("status", "Status"));
		column.setSortable(false);
		column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().status()));
		column.setCellFactory(ignored -> new TableCell<>() {
			@Override
			protected void updateItem(String status, boolean empty) {
				super.updateItem(status, empty);
				if (empty || status == null) {
					setGraphic(null);
					setText(null);
					return;
				}
				boolean active = isActive(status);
				Label badge = new Label(active ? "ATIVO" : "ENCERRADO");
				badge.getStyleClass().addAll("status-badge", active ? "status-active" : "status-inactive");
				setGraphic(badge);
				setText(null);
			}
		});
		return column;
	}

	private TableColumn<ContractDTO, Void> actionsColumn() {
		TableColumn<ContractDTO, Void> column = new TableColumn<>();
		Label title = new Label("Ações");
		title.getStyleClass().add("column-header-label");
		column.setGraphic(title);
		column.setSortable(false);
		column.setCellFactory(ignored -> new TableCell<>() {
			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				int index = getIndex();
				if (empty || index < 0 || index >= getTableView().getItems().size()) {
					setGraphic(null);
					return;
				}
				ContractDTO contract = getTableView().getItems().get(index);
				Button edit = linkButton("Editar", "btn-link", event -> openForm(contract));
				Button toggle = linkButton(isActive(contract.status()) ? "Encerrar" : "Reativar",
						"btn-link-warning", event -> toggleStatus(contract));
				Button delete = linkButton("Excluir", "btn-link-danger", event -> delete(contract));
				HBox box = new HBox(10, edit, toggle, delete);
				box.setAlignment(Pos.CENTER_LEFT);
				setGraphic(box);
			}
		});
		return column;
	}

	private Button header(String field, String title) {
		Button button = new Button(title);
		button.getStyleClass().add("column-sort-button");
		button.setUserData(title);
		button.setOnAction(event -> sortBy(field));
		headers.put(field, button);
		return button;
	}

	private Button linkButton(String text, String styleClass, EventHandler<ActionEvent> handler) {
		Button button = new Button(text);
		button.getStyleClass().add(styleClass);
		button.setOnAction(handler);
		return button;
	}

	private void sortBy(String field) {
		if (field.equals(sortField)) {
			ascending = !ascending;
		}
		else {
			sortField = field;
			ascending = true;
		}
		currentPage = 1;
		refreshView();
	}

	private void changePage(int delta) {
		int target = currentPage + delta;
		if (target < 1 || target > pageCount) {
			return;
		}
		currentPage = target;
		renderPage();
	}

	private void load() {
		shell.clearError();
		setBusy(true);
		shell.setLoading(true);

		FxAsync.run(contractService::findAll, contracts -> {
			allContracts.clear();
			allContracts.addAll(contracts);
			currentPage = 1;
			refreshView();
			setBusy(false);
			shell.setLoading(false);
		}, failure -> {
			setBusy(false);
			shell.setLoading(false);
			handleFailure(failure, "Não foi possível carregar os contratos. Verifique a conexão com o servidor.");
		});
	}

	private void refreshView() {
		String query = searchField.getText() == null ? ""
				: searchField.getText().trim().toLowerCase(Locale.ROOT);

		visibleContracts.clear();
		for (ContractDTO contract : allContracts) {
			if (matches(contract, query)) {
				visibleContracts.add(contract);
			}
		}

		Comparator<ContractDTO> comparator = comparator();
		if (comparator != null) {
			visibleContracts.sort(comparator);
		}
		renderPage();
	}

	private void renderPage() {
		pageCount = Math.max(1, (visibleContracts.size() + PAGE_SIZE - 1) / PAGE_SIZE);
		currentPage = Math.max(1, Math.min(currentPage, pageCount));

		int from = (currentPage - 1) * PAGE_SIZE;
		int to = Math.min(from + PAGE_SIZE, visibleContracts.size());
		pageItems.setAll(visibleContracts.subList(from, to));

		int total = visibleContracts.size();
		countLabel.setText(total + (total == 1 ? " registro" : " registros"));
		pageLabel.setText("Página " + currentPage + " de " + pageCount);
		previousButton.setDisable(currentPage <= 1);
		nextButton.setDisable(currentPage >= pageCount);
		updateHeaders();
	}

	private void updateHeaders() {
		headers.forEach((field, button) -> {
			String base = String.valueOf(button.getUserData());
			button.getStyleClass().remove("sorted");
			if (field.equals(sortField)) {
				button.setText(base + (ascending ? "  ▲" : "  ▼"));
				button.getStyleClass().add("sorted");
			}
			else {
				button.setText(base);
			}
		});
	}

	private Comparator<ContractDTO> comparator() {
		Comparator<ContractDTO> comparator = switch (sortField) {
			case "client" -> Comparator.comparing((ContractDTO contract) -> clientName(contract),
					String.CASE_INSENSITIVE_ORDER);
			case "provider" -> Comparator.comparing((ContractDTO contract) -> providerName(contract),
					String.CASE_INSENSITIVE_ORDER);
			case "startDate" -> Comparator.comparing(ContractDTO::startDate,
					Comparator.nullsLast(Comparator.naturalOrder()));
			case "endDate" -> Comparator.comparing(ContractDTO::endDate,
					Comparator.nullsLast(Comparator.naturalOrder()));
			case "agreedValue" -> Comparator.comparing(ContractDTO::agreedValue,
					Comparator.nullsLast(Comparator.naturalOrder()));
			case "status" -> Comparator.comparing((ContractDTO contract) -> isActive(contract.status()) ? 0 : 1);
			default -> null;
		};
		if (comparator == null) {
			return null;
		}
		return ascending ? comparator : comparator.reversed();
	}

	private boolean matches(ContractDTO contract, String query) {
		if (query.isEmpty()) {
			return true;
		}
		return contains(clientName(contract), query) || contains(providerName(contract), query);
	}

	private boolean contains(String value, String query) {
		return value != null && value.toLowerCase(Locale.ROOT).contains(query);
	}

	private String clientName(ContractDTO contract) {
		if (contract.client() != null && contract.client().legalName() != null
				&& !contract.client().legalName().isBlank()) {
			return contract.client().legalName();
		}
		return "—";
	}

	private String providerName(ContractDTO contract) {
		if (contract.provider() != null && contract.provider().name() != null
				&& !contract.provider().name().isBlank()) {
			return contract.provider().name();
		}
		return "—";
	}

	private void openForm(ContractDTO existing) {
		shell.clearError();
		setBusy(true);
		shell.setLoading(true);

		FxAsync.run(() -> new Lookups(clientService.findAll(), providerService.findAll()), lookups -> {
			setBusy(false);
			shell.setLoading(false);

			if (lookups.clients().isEmpty() || lookups.providers().isEmpty()) {
				showMissingLookups();
				return;
			}

			boolean creating = existing == null;
			Function<ContractForm, ContractDTO> saver;
			if (creating) {
				saver = form -> {
					ContractDTO saved = contractService.create(form.toCreateRequest());
					auditLog.record(AuditLog.CREATE, AuditLog.CONTRACT, idOf(saved), form.toDetails());
					return saved;
				};
			}
			else {
				saver = form -> {
					ContractDTO saved = contractService.update(existing.id(), form.toUpdateRequest());
					auditLog.record(AuditLog.UPDATE, AuditLog.CONTRACT, idOf(saved), form.toDetails());
					return saved;
				};
			}

			ContractFormDialog dialog = new ContractFormDialog(shell, contractValidator,
					creating ? "Novo Contrato" : "Editar Contrato",
					creating ? "Criar Contrato" : "Atualizar Contrato",
					existing, lookups.clients(), lookups.providers(), saver);
			if (dialog.showAndWait()) {
				load();
			}
		}, failure -> {
			setBusy(false);
			shell.setLoading(false);
			handleFailure(failure, "Não foi possível carregar clientes e prestadores.");
		});
	}

	private void toggleStatus(ContractDTO contract) {
		boolean active = isActive(contract.status());
		String action = active ? "Encerrar" : "Reativar";
		String content = active ? "O contrato será marcado como encerrado."
				: "O contrato voltará a ficar ativo.";

		if (!confirm(action + " contrato", action + " o contrato de \"" + clientName(contract) + "\"?",
				content, action)) {
			return;
		}

		shell.clearError();
		setBusy(true);
		shell.setLoading(true);
		FxAsync.run(() -> {
			ContractDTO updated = contractService.toggleStatus(contract.id(), contract.status());
			auditLog.record(AuditLog.STATUS_CHANGE, AuditLog.CONTRACT, idOf(contract),
					Map.of("status", updated.status()));
			return updated;
		}, updated -> {
			setBusy(false);
			load();
		}, failure -> {
			setBusy(false);
			shell.setLoading(false);
			handleFailure(failure, "Não foi possível atualizar o status do contrato.");
		});
	}

	private void delete(ContractDTO contract) {
		if (!confirm("Excluir contrato", "Excluir o contrato de \"" + clientName(contract) + "\"?",
				"Esta ação não pode ser desfeita.", "Excluir")) {
			return;
		}

		shell.clearError();
		setBusy(true);
		shell.setLoading(true);
		FxAsync.run(() -> {
			contractService.delete(contract.id());
			auditLog.record(AuditLog.DELETE, AuditLog.CONTRACT, idOf(contract), Map.of());
			return Boolean.TRUE;
		}, done -> {
			setBusy(false);
			load();
		}, failure -> {
			setBusy(false);
			shell.setLoading(false);
			handleFailure(failure, "Não foi possível excluir o contrato.");
		});
	}

	private boolean confirm(String title, String header, String content, String confirmText) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		styleDialog(alert);

		ButtonType confirm = new ButtonType(confirmText, ButtonBar.ButtonData.OK_DONE);
		ButtonType cancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().setAll(confirm, cancel);

		Optional<ButtonType> choice = alert.showAndWait();
		return choice.isPresent() && choice.get() == confirm;
	}

	private void showMissingLookups() {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle("Cadastro incompleto");
		alert.setHeaderText("Cadastre um cliente e um prestador antes de criar contratos.");
		alert.setContentText("Acesse as seções Clientes e Prestadores para cadastrá-los.");
		styleDialog(alert);
		alert.getButtonTypes().setAll(new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));
		alert.showAndWait();
	}

	private void styleDialog(Dialog<?> dialog) {
		DialogPane pane = dialog.getDialogPane();
		pane.getStyleClass().add("app-dialog");
		URL stylesheet = ContractsPage.class.getResource(STYLESHEET);
		if (stylesheet != null) {
			pane.getStylesheets().add(stylesheet.toExternalForm());
		}
	}

	private void handleFailure(Throwable failure, String fallback) {
		if (failure instanceof ApiException apiException && apiException.getStatus() == 401) {
			shell.sessionExpired();
			return;
		}
		shell.showError(describe(failure, fallback));
	}

	private String describe(Throwable failure, String fallback) {
		if (failure instanceof ApiException apiException
				&& apiException.getMessage() != null && !apiException.getMessage().isBlank()) {
			return apiException.getMessage();
		}
		return fallback;
	}

	private void setBusy(boolean busy) {
		refreshButton.setDisable(busy);
		newButton.setDisable(busy);
		table.setDisable(busy);
	}

	private boolean isActive(String status) {
		return ACTIVE.equalsIgnoreCase(status);
	}

	private String idOf(ContractDTO contract) {
		return contract == null || contract.id() == null ? null : contract.id().toString();
	}

	private record Lookups(List<ClientDTO> clients, List<ProviderDTO> providers) {
	}

}
