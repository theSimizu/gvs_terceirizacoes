package com.h_group.gvs_terceirizacoes_desktop.ui;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiException;
import com.h_group.gvs_terceirizacoes_desktop.dto.ContractDTO;
import com.h_group.gvs_terceirizacoes_desktop.dto.ServiceOrderDTO;
import com.h_group.gvs_terceirizacoes_desktop.service.AuditLog;
import com.h_group.gvs_terceirizacoes_desktop.service.ContractService;
import com.h_group.gvs_terceirizacoes_desktop.service.ServiceOrderService;
import com.h_group.gvs_terceirizacoes_desktop.validation.ServiceOrderValidator;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class ServiceOrdersPage implements Page {

	private static final int PAGE_SIZE = 10;

	private static final String ALL_STATUSES = "";

	private static final String STYLESHEET = "/css/app.css";

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
			.withZone(ZoneId.systemDefault());

	private final AppShell shell;

	private final ServiceOrderService serviceOrderService;

	private final ServiceOrderValidator serviceOrderValidator;

	private final ContractService contractService;

	private final AuditLog auditLog;

	private final List<ServiceOrderDTO> allOrders = new ArrayList<>();

	private final List<ServiceOrderDTO> visibleOrders = new ArrayList<>();

	private final ObservableList<ServiceOrderDTO> pageItems = FXCollections.observableArrayList();

	private final Map<String, Button> headers = new HashMap<>();

	private final TableView<ServiceOrderDTO> table = new TableView<>(pageItems);

	private final TextField searchField = new TextField();

	private final ComboBox<String> statusFilter = new ComboBox<>();

	private final Label countLabel = new Label();

	private final Label pageLabel = new Label();

	private final Button refreshButton = new Button("Atualizar");

	private final Button newButton = new Button("Nova Ordem");

	private final Button previousButton = new Button("Anterior");

	private final Button nextButton = new Button("Próxima");

	private final HBox actions = new HBox(12, refreshButton, newButton);

	private String sortField = "executionDate";

	private boolean ascending = true;

	private int currentPage = 1;

	private int pageCount = 1;

	public ServiceOrdersPage(AppShell shell, ServiceOrderService serviceOrderService,
			ServiceOrderValidator serviceOrderValidator, ContractService contractService, AuditLog auditLog) {
		this.shell = shell;
		this.serviceOrderService = serviceOrderService;
		this.serviceOrderValidator = serviceOrderValidator;
		this.contractService = contractService;
		this.auditLog = auditLog;
		configureActions();
		configureTable();
		configurePagination();
		configureStatusFilter();
	}

	@Override
	public String title() {
		return "Ordens de Serviço";
	}

	@Override
	public Node build() {
		searchField.getStyleClass().add("search-field");
		searchField.setPromptText("Buscar por nº, descrição, cliente ou prestador");
		searchField.textProperty().addListener((observable, oldValue, newValue) -> {
			currentPage = 1;
			refreshView();
		});

		countLabel.getStyleClass().add("page-label");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		HBox toolbar = new HBox(12, searchField, statusFilter, spacer, countLabel);
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

	private void configureStatusFilter() {
		statusFilter.getStyleClass().add("filter-combo");
		statusFilter.getItems().add(ALL_STATUSES);
		statusFilter.getItems().addAll(ServiceOrderStatuses.all());
		statusFilter.setConverter(new StringConverter<>() {
			@Override
			public String toString(String status) {
				if (status == null || status.isEmpty()) {
					return "Todos os Status";
				}
				return ServiceOrderStatuses.label(status);
			}

			@Override
			public String fromString(String text) {
				return text;
			}
		});
		statusFilter.setValue(ALL_STATUSES);
		statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
			currentPage = 1;
			refreshView();
		});
	}

	private void configureTable() {
		table.getStyleClass().add("data-table");
		table.setEditable(false);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

		TableColumn<ServiceOrderDTO, String> number = sortedColumn("number", "Nº", this::orderNumber);
		number.setPrefWidth(90);
		TableColumn<ServiceOrderDTO, String> client = sortedColumn("client", "Cliente", this::clientName);
		client.setPrefWidth(170);
		TableColumn<ServiceOrderDTO, String> provider = sortedColumn("provider", "Prestador", this::providerName);
		provider.setPrefWidth(150);
		TableColumn<ServiceOrderDTO, String> description = descriptionColumn();
		description.setPrefWidth(220);
		TableColumn<ServiceOrderDTO, LocalDate> executionDate = dateColumn();
		executionDate.setPrefWidth(110);
		TableColumn<ServiceOrderDTO, String> status = statusColumn();
		status.setPrefWidth(120);
		TableColumn<ServiceOrderDTO, Void> actionsColumn = actionsColumn();
		actionsColumn.setPrefWidth(285);

		table.getColumns().setAll(number, client, provider, description, executionDate, status, actionsColumn);
	}

	private void configurePagination() {
		previousButton.getStyleClass().add("btn-secondary");
		nextButton.getStyleClass().add("btn-secondary");
		pageLabel.getStyleClass().add("page-label");

		previousButton.setOnAction(event -> changePage(-1));
		nextButton.setOnAction(event -> changePage(1));
	}

	private TableColumn<ServiceOrderDTO, String> sortedColumn(String field, String title,
			Function<ServiceOrderDTO, String> value) {
		TableColumn<ServiceOrderDTO, String> column = new TableColumn<>();
		column.setGraphic(header(field, title));
		column.setSortable(false);
		column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(value.apply(data.getValue())));
		return column;
	}

	private TableColumn<ServiceOrderDTO, String> descriptionColumn() {
		TableColumn<ServiceOrderDTO, String> column = new TableColumn<>();
		column.setGraphic(header("description", "Descrição"));
		column.setSortable(false);
		column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().description()));
		column.setCellFactory(ignored -> new TableCell<>() {
			@Override
			protected void updateItem(String description, boolean empty) {
				super.updateItem(description, empty);
				if (empty || description == null || description.isBlank()) {
					setText(empty ? null : "—");
					setTooltip(null);
					return;
				}
				setText(shorten(description));
				setTooltip(new Tooltip(description));
			}
		});
		return column;
	}

	private TableColumn<ServiceOrderDTO, LocalDate> dateColumn() {
		TableColumn<ServiceOrderDTO, LocalDate> column = new TableColumn<>();
		column.setGraphic(header("executionDate", "Execução"));
		column.setSortable(false);
		column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().executionDate()));
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

	private TableColumn<ServiceOrderDTO, String> statusColumn() {
		TableColumn<ServiceOrderDTO, String> column = new TableColumn<>();
		column.setGraphic(header("status", "Status"));
		column.setSortable(false);
		column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().status()));
		column.setCellFactory(ignored -> new TableCell<>() {
			@Override
			protected void updateItem(String status, boolean empty) {
				super.updateItem(status, empty);
				if (empty || status == null || status.isBlank()) {
					setGraphic(null);
					setText(null);
					return;
				}
				Label badge = new Label(ServiceOrderStatuses.label(status).toUpperCase(Locale.ROOT));
				String styleClass = ServiceOrderStatuses.badgeClass(status);
				badge.getStyleClass().addAll("status-badge",
						styleClass == null ? "status-inactive" : styleClass);
				setGraphic(badge);
				setText(null);
			}
		});
		return column;
	}

	private TableColumn<ServiceOrderDTO, Void> actionsColumn() {
		TableColumn<ServiceOrderDTO, Void> column = new TableColumn<>();
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
				ServiceOrderDTO order = getTableView().getItems().get(index);
				Button details = linkButton("Detalhes", "btn-link", event -> showDetails(order));
				Button edit = linkButton("Editar", "btn-link", event -> openForm(order));
				MenuButton statusMenu = statusMenu(order);
				Button delete = linkButton("Excluir", "btn-link-danger", event -> delete(order));
				delete.setVisible(shell.canDelete());
				delete.setManaged(shell.canDelete());
				HBox box = new HBox(8, details, edit, statusMenu, delete);
				box.setAlignment(Pos.CENTER_LEFT);
				setGraphic(box);
			}
		});
		return column;
	}

	private MenuButton statusMenu(ServiceOrderDTO order) {
		MenuButton menu = new MenuButton("Status");
		menu.getStyleClass().add("status-menu");
		for (String status : ServiceOrderStatuses.all()) {
			boolean current = status.equals(order.status());
			MenuItem item = new MenuItem(ServiceOrderStatuses.label(status) + (current ? " (atual)" : ""));
			item.setDisable(current);
			item.setOnAction(event -> changeStatus(order, status));
			menu.getItems().add(item);
		}
		return menu;
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

		FxAsync.run(serviceOrderService::findAll, orders -> {
			allOrders.clear();
			allOrders.addAll(orders);
			currentPage = 1;
			refreshView();
			setBusy(false);
			shell.setLoading(false);
		}, failure -> {
			setBusy(false);
			shell.setLoading(false);
			handleFailure(failure, "Não foi possível carregar as ordens de serviço.");
		});
	}

	private void refreshView() {
		String query = searchField.getText() == null ? ""
				: searchField.getText().trim().toLowerCase(Locale.ROOT);
		String status = statusFilter.getValue();

		visibleOrders.clear();
		for (ServiceOrderDTO order : allOrders) {
			if (matches(order, query) && matchesStatus(order, status)) {
				visibleOrders.add(order);
			}
		}

		Comparator<ServiceOrderDTO> comparator = comparator();
		if (comparator != null) {
			visibleOrders.sort(comparator);
		}
		renderPage();
	}

	private void renderPage() {
		pageCount = Math.max(1, (visibleOrders.size() + PAGE_SIZE - 1) / PAGE_SIZE);
		currentPage = Math.max(1, Math.min(currentPage, pageCount));

		int from = (currentPage - 1) * PAGE_SIZE;
		int to = Math.min(from + PAGE_SIZE, visibleOrders.size());
		pageItems.setAll(visibleOrders.subList(from, to));

		int total = visibleOrders.size();
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

	private Comparator<ServiceOrderDTO> comparator() {
		Comparator<ServiceOrderDTO> comparator = switch (sortField) {
			case "number" -> Comparator.comparing(this::orderNumber, String.CASE_INSENSITIVE_ORDER);
			case "client" -> Comparator.comparing(this::clientName, String.CASE_INSENSITIVE_ORDER);
			case "provider" -> Comparator.comparing(this::providerName, String.CASE_INSENSITIVE_ORDER);
			case "description" -> Comparator.comparing(
					(ServiceOrderDTO order) -> order.description() == null ? "" : order.description(),
					String.CASE_INSENSITIVE_ORDER);
			case "executionDate" -> Comparator.comparing(ServiceOrderDTO::executionDate,
					Comparator.nullsLast(Comparator.naturalOrder()));
			case "status" -> Comparator.comparingInt(
					(ServiceOrderDTO order) -> ServiceOrderStatuses.ordinal(order.status()));
			default -> null;
		};
		if (comparator == null) {
			return null;
		}
		return ascending ? comparator : comparator.reversed();
	}

	private boolean matches(ServiceOrderDTO order, String query) {
		if (query.isEmpty()) {
			return true;
		}
		return contains(orderNumber(order), query) || contains(order.description(), query)
				|| contains(clientName(order), query) || contains(providerName(order), query);
	}

	private boolean matchesStatus(ServiceOrderDTO order, String status) {
		if (status == null || status.isEmpty()) {
			return true;
		}
		return status.equalsIgnoreCase(order.status());
	}

	private boolean contains(String value, String query) {
		return value != null && value.toLowerCase(Locale.ROOT).contains(query);
	}

	private String orderNumber(ServiceOrderDTO order) {
		UUID id = order.id();
		if (id == null) {
			return "—";
		}
		String value = id.toString();
		return "#OS-" + value.substring(value.length() - 4).toUpperCase(Locale.ROOT);
	}

	private String clientName(ServiceOrderDTO order) {
		if (order.contract() != null && order.contract().client() != null
				&& order.contract().client().legalName() != null
				&& !order.contract().client().legalName().isBlank()) {
			return order.contract().client().legalName();
		}
		return "—";
	}

	private String providerName(ServiceOrderDTO order) {
		if (order.contract() != null && order.contract().provider() != null
				&& order.contract().provider().name() != null
				&& !order.contract().provider().name().isBlank()) {
			return order.contract().provider().name();
		}
		return "—";
	}

	private String shorten(String text) {
		String singleLine = text.replaceAll("\\s+", " ").trim();
		return singleLine.length() <= 70 ? singleLine : singleLine.substring(0, 67) + "...";
	}

	private void openForm(ServiceOrderDTO existing) {
		shell.clearError();
		setBusy(true);
		shell.setLoading(true);

		FxAsync.run(contractService::findAll, contracts -> {
			setBusy(false);
			shell.setLoading(false);

			if (contracts.isEmpty()) {
				showMissingContracts();
				return;
			}

			boolean creating = existing == null;
			Function<ServiceOrderForm, ServiceOrderDTO> saver;
			if (creating) {
				saver = form -> {
					ServiceOrderDTO saved = serviceOrderService.create(form.toCreateRequest());
					auditLog.record(AuditLog.CREATE, AuditLog.SERVICE_ORDER, idOf(saved), form.toDetails());
					return saved;
				};
			}
			else {
				saver = form -> {
					ServiceOrderDTO saved = serviceOrderService.update(existing.id(), form.toUpdateRequest());
					auditLog.record(AuditLog.UPDATE, AuditLog.SERVICE_ORDER, idOf(saved), form.toDetails());
					return saved;
				};
			}

			ServiceOrderFormDialog dialog = new ServiceOrderFormDialog(shell, serviceOrderValidator,
					creating ? "Nova Ordem de Serviço" : "Editar Ordem de Serviço",
					creating ? "Registrar Ordem" : "Atualizar Ordem",
					existing, contracts, saver);
			if (dialog.showAndWait()) {
				load();
			}
		}, failure -> {
			setBusy(false);
			shell.setLoading(false);
			handleFailure(failure, "Não foi possível carregar os contratos.");
		});
	}

	private void changeStatus(ServiceOrderDTO order, String status) {
		if (status.equalsIgnoreCase(order.status())) {
			return;
		}

		String label = ServiceOrderStatuses.label(status);
		if (!confirm("Alterar status", "Alterar o status da ordem " + orderNumber(order) + "?",
				"Novo status: " + label + ".", "Alterar")) {
			return;
		}

		shell.clearError();
		setBusy(true);
		shell.setLoading(true);
		FxAsync.run(() -> {
			ServiceOrderDTO updated = serviceOrderService.setStatus(order.id(), status);
			auditLog.record(AuditLog.STATUS_CHANGE, AuditLog.SERVICE_ORDER, idOf(order),
					Map.of("status", updated.status()));
			return updated;
		}, updated -> {
			setBusy(false);
			load();
		}, failure -> {
			setBusy(false);
			shell.setLoading(false);
			handleFailure(failure, "Não foi possível atualizar o status da ordem de serviço.");
		});
	}

	private void delete(ServiceOrderDTO order) {
		if (!confirm("Excluir ordem de serviço", "Excluir a ordem " + orderNumber(order) + "?",
				"Esta ação não pode ser desfeita.", "Excluir")) {
			return;
		}

		shell.clearError();
		setBusy(true);
		shell.setLoading(true);
		FxAsync.run(() -> {
			serviceOrderService.delete(order.id());
			auditLog.record(AuditLog.DELETE, AuditLog.SERVICE_ORDER, idOf(order), Map.of());
			return Boolean.TRUE;
		}, done -> {
			setBusy(false);
			load();
		}, failure -> {
			setBusy(false);
			shell.setLoading(false);
			handleFailure(failure, "Não foi possível excluir a ordem de serviço.");
		});
	}

	private void showDetails(ServiceOrderDTO order) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Detalhes da Ordem de Serviço");
		alert.setHeaderText("Ordem " + orderNumber(order));

		Label descriptionLabel = new Label("Descrição da Atividade");
		descriptionLabel.getStyleClass().add("form-label");

		TextArea description = new TextArea(order.description() == null ? "" : order.description());
		description.setEditable(false);
		description.setWrapText(true);
		description.setPrefRowCount(5);
		description.getStyleClass().add("form-input");

		Label status = new Label("Status: " + ServiceOrderStatuses.label(order.status()));
		Label execution = new Label("Data de execução: "
				+ (order.executionDate() == null ? "—" : DATE_FORMAT.format(order.executionDate())));

		VBox content = new VBox(12,
				descriptionLabel,
				description,
				status,
				contractSummary(order.contract()),
				execution,
				timestamps(order));
		content.setPadding(new Insets(4, 0, 0, 0));
		alert.getDialogPane().setContent(content);
		alert.getDialogPane().setPrefWidth(460);
		styleDialog(alert);
		alert.getButtonTypes().setAll(new ButtonType("Fechar", ButtonBar.ButtonData.OK_DONE));
		alert.showAndWait();
	}

	private VBox contractSummary(ContractDTO contract) {
		if (contract == null) {
			return new VBox(4, new Label("Contrato: —"));
		}
		Label client = new Label("Cliente: "
				+ (contract.client() == null || contract.client().legalName() == null
						? "—" : contract.client().legalName()));
		Label provider = new Label("Prestador: "
				+ (contract.provider() == null || contract.provider().name() == null
						? "—" : contract.provider().name()));
		Label value = new Label("Valor: "
				+ (contract.agreedValue() == null ? "—" : InputMasks.formatCurrency(contract.agreedValue())));
		Label period = new Label("Período: " + dateOrDash(contract.startDate()) + " a "
				+ dateOrDash(contract.endDate()));
		return new VBox(4, client, provider, value, period);
	}

	private Label timestamps(ServiceOrderDTO order) {
		StringBuilder text = new StringBuilder();
		if (order.createdAt() != null) {
			text.append("Registrada em: ").append(TIMESTAMP_FORMAT.format(order.createdAt()));
		}
		if (order.updatedAt() != null) {
			if (!text.isEmpty()) {
				text.append("     ");
			}
			text.append("Atualizada em: ").append(TIMESTAMP_FORMAT.format(order.updatedAt()));
		}
		return new Label(text.isEmpty() ? "" : text.toString());
	}

	private String dateOrDash(LocalDate date) {
		return date == null ? "—" : DATE_FORMAT.format(date);
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

	private void showMissingContracts() {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle("Cadastro incompleto");
		alert.setHeaderText("Cadastre um contrato antes de registrar ordens de serviço.");
		alert.setContentText("Acesse a seção Contratos para cadastrá-lo.");
		styleDialog(alert);
		alert.getButtonTypes().setAll(new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));
		alert.showAndWait();
	}

	private void styleDialog(Dialog<?> dialog) {
		DialogPane pane = dialog.getDialogPane();
		pane.getStyleClass().add("app-dialog");
		URL stylesheet = ServiceOrdersPage.class.getResource(STYLESHEET);
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
		statusFilter.setDisable(busy);
	}

	private String idOf(ServiceOrderDTO order) {
		return order == null || order.id() == null ? null : order.id().toString();
	}

}
