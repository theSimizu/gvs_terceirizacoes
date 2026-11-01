package com.h_group.gvs_terceirizacoes_desktop.ui;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiException;
import com.h_group.gvs_terceirizacoes_desktop.dto.ClientDTO;
import com.h_group.gvs_terceirizacoes_desktop.service.AuditLog;
import com.h_group.gvs_terceirizacoes_desktop.service.ClientService;
import com.h_group.gvs_terceirizacoes_desktop.validation.ClientValidator;
import java.net.URL;
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

public class ClientsPage implements Page {

	private static final int PAGE_SIZE = 10;

	private static final String ACTIVE = "active";

	private static final String INACTIVE = "inactive";

	private static final String STYLESHEET = "/css/app.css";

	private final AppShell shell;

	private final ClientService clientService;

	private final ClientValidator clientValidator;

	private final AuditLog auditLog;

	private final List<ClientDTO> allClients = new ArrayList<>();

	private final List<ClientDTO> visibleClients = new ArrayList<>();

	private final ObservableList<ClientDTO> pageItems = FXCollections.observableArrayList();

	private final Map<String, Button> headers = new HashMap<>();

	private final TableView<ClientDTO> table = new TableView<>(pageItems);

	private final TextField searchField = new TextField();

	private final Label countLabel = new Label();

	private final Label pageLabel = new Label();

	private final Button refreshButton = new Button("Atualizar");

	private final Button newButton = new Button("Novo Cliente");

	private final Button previousButton = new Button("Anterior");

	private final Button nextButton = new Button("Próxima");

	private final HBox actions = new HBox(12, refreshButton, newButton);

	private String sortField = "legalName";

	private boolean ascending = true;

	private int currentPage = 1;

	private int pageCount = 1;

	public ClientsPage(AppShell shell, ClientService clientService, ClientValidator clientValidator,
			AuditLog auditLog) {
		this.shell = shell;
		this.clientService = clientService;
		this.clientValidator = clientValidator;
		this.auditLog = auditLog;
		configureActions();
		configureTable();
		configurePagination();
	}

	@Override
	public String title() {
		return "Clientes";
	}

	@Override
	public Node build() {
		searchField.getStyleClass().add("search-field");
		searchField.setPromptText("Buscar por razão social, CNPJ ou e-mail");
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

		TableColumn<ClientDTO, String> legalName = sortedColumn("legalName", "Razão Social", ClientDTO::legalName);
		legalName.setPrefWidth(260);
		TableColumn<ClientDTO, String> cnpj = sortedColumn("cnpj", "CNPJ", ClientDTO::cnpj);
		cnpj.setPrefWidth(170);
		TableColumn<ClientDTO, String> email = sortedColumn("email", "E-mail", ClientDTO::email);
		email.setPrefWidth(220);
		TableColumn<ClientDTO, String> phone = sortedColumn("phone", "Telefone", ClientDTO::phone);
		phone.setPrefWidth(150);
		TableColumn<ClientDTO, String> status = statusColumn();
		status.setPrefWidth(110);
		TableColumn<ClientDTO, Void> actionsColumn = actionsColumn();
		actionsColumn.setPrefWidth(220);

		table.getColumns().setAll(legalName, cnpj, email, phone, status, actionsColumn);
	}

	private void configurePagination() {
		previousButton.getStyleClass().add("btn-secondary");
		nextButton.getStyleClass().add("btn-secondary");
		pageLabel.getStyleClass().add("page-label");

		previousButton.setOnAction(event -> changePage(-1));
		nextButton.setOnAction(event -> changePage(1));
	}

	private TableColumn<ClientDTO, String> sortedColumn(String field, String title,
			Function<ClientDTO, String> value) {
		TableColumn<ClientDTO, String> column = new TableColumn<>();
		column.setGraphic(header(field, title));
		column.setSortable(false);
		column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(value.apply(data.getValue())));
		return column;
	}

	private TableColumn<ClientDTO, String> statusColumn() {
		TableColumn<ClientDTO, String> column = new TableColumn<>();
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
				Label badge = new Label(active ? "ATIVO" : "INATIVO");
				badge.getStyleClass().addAll("status-badge", active ? "status-active" : "status-inactive");
				setGraphic(badge);
				setText(null);
			}
		});
		return column;
	}

	private TableColumn<ClientDTO, Void> actionsColumn() {
		TableColumn<ClientDTO, Void> column = new TableColumn<>();
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
				ClientDTO client = getTableView().getItems().get(index);
				Button edit = linkButton("Editar", "btn-link", event -> openForm(client));
				Button toggle = linkButton(isActive(client.status()) ? "Desativar" : "Ativar",
						"btn-link-warning", event -> toggleStatus(client));
				Button delete = linkButton("Excluir", "btn-link-danger", event -> delete(client));
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

		FxAsync.run(clientService::findAll, clients -> {
			allClients.clear();
			allClients.addAll(clients);
			currentPage = 1;
			refreshView();
			setBusy(false);
			shell.setLoading(false);
		}, failure -> {
			setBusy(false);
			shell.setLoading(false);
			handleFailure(failure, "Não foi possível carregar os clientes. Verifique a conexão com o servidor.");
		});
	}

	private void refreshView() {
		String query = searchField.getText() == null ? ""
				: searchField.getText().trim().toLowerCase(Locale.ROOT);

		visibleClients.clear();
		for (ClientDTO client : allClients) {
			if (matches(client, query)) {
				visibleClients.add(client);
			}
		}

		Comparator<ClientDTO> comparator = comparator();
		if (comparator != null) {
			visibleClients.sort(comparator);
		}
		renderPage();
	}

	private void renderPage() {
		pageCount = Math.max(1, (visibleClients.size() + PAGE_SIZE - 1) / PAGE_SIZE);
		currentPage = Math.max(1, Math.min(currentPage, pageCount));

		int from = (currentPage - 1) * PAGE_SIZE;
		int to = Math.min(from + PAGE_SIZE, visibleClients.size());
		pageItems.setAll(visibleClients.subList(from, to));

		int total = visibleClients.size();
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

	private Comparator<ClientDTO> comparator() {
		Comparator<ClientDTO> comparator = switch (sortField) {
			case "legalName" -> Comparator.comparing((ClientDTO client) -> text(client.legalName()),
					String.CASE_INSENSITIVE_ORDER);
			case "cnpj" -> Comparator.comparing((ClientDTO client) -> digits(client.cnpj()));
			case "email" -> Comparator.comparing((ClientDTO client) -> text(client.email()),
					String.CASE_INSENSITIVE_ORDER);
			case "phone" -> Comparator.comparing((ClientDTO client) -> digits(client.phone()));
			case "status" -> Comparator.comparing((ClientDTO client) -> isActive(client.status()) ? 0 : 1);
			default -> null;
		};
		if (comparator == null) {
			return null;
		}
		return ascending ? comparator : comparator.reversed();
	}

	private boolean matches(ClientDTO client, String query) {
		if (query.isEmpty()) {
			return true;
		}
		if (contains(client.legalName(), query) || contains(client.email(), query)) {
			return true;
		}
		if (contains(client.cnpj(), query)) {
			return true;
		}
		String digits = query.replaceAll("\\D", "");
		return !digits.isEmpty() && digits(client.cnpj()).contains(digits);
	}

	private boolean contains(String value, String query) {
		return value != null && value.toLowerCase(Locale.ROOT).contains(query);
	}

	private String text(String value) {
		return value == null ? "" : value;
	}

	private String digits(String value) {
		return value == null ? "" : value.replaceAll("\\D", "");
	}

	private void openForm(ClientDTO existing) {
		boolean creating = existing == null;
		Function<ClientForm, ClientDTO> saver;
		if (creating) {
			saver = form -> {
				ClientDTO saved = clientService.create(form.toCreateRequest());
				auditLog.record(AuditLog.CREATE, AuditLog.CLIENT, idOf(saved), form.toDetails());
				return saved;
			};
		}
		else {
			saver = form -> {
				ClientDTO saved = clientService.update(existing.id(), form.toUpdateRequest());
				auditLog.record(AuditLog.UPDATE, AuditLog.CLIENT, idOf(saved), form.toDetails());
				return saved;
			};
		}

		ClientFormDialog dialog = new ClientFormDialog(shell, clientValidator,
				creating ? "Novo Cliente" : "Editar Cliente",
				creating ? "Salvar Cliente" : "Atualizar Cliente",
				existing, saver);
		if (dialog.showAndWait()) {
			load();
		}
	}

	private void toggleStatus(ClientDTO client) {
		String nextStatus = isActive(client.status()) ? INACTIVE : ACTIVE;
		ClientForm form = ClientForm.from(client, nextStatus);

		shell.clearError();
		setBusy(true);
		shell.setLoading(true);
		FxAsync.run(() -> {
			ClientDTO updated = clientService.update(client.id(), form.toUpdateRequest());
			auditLog.record(AuditLog.STATUS_CHANGE, AuditLog.CLIENT, idOf(client),
					Map.of("status", nextStatus));
			return updated;
		}, updated -> {
			setBusy(false);
			load();
		}, failure -> {
			setBusy(false);
			shell.setLoading(false);
			handleFailure(failure, "Não foi possível atualizar o status do cliente.");
		});
	}

	private void delete(ClientDTO client) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Excluir cliente");
		alert.setHeaderText("Excluir \"" + client.legalName() + "\"?");
		alert.setContentText("Esta ação não pode ser desfeita.");
		styleDialog(alert);

		ButtonType confirm = new ButtonType("Excluir", ButtonBar.ButtonData.OK_DONE);
		ButtonType cancel = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
		alert.getButtonTypes().setAll(confirm, cancel);

		Optional<ButtonType> choice = alert.showAndWait();
		if (choice.isEmpty() || choice.get() != confirm) {
			return;
		}

		shell.clearError();
		setBusy(true);
		shell.setLoading(true);
		FxAsync.run(() -> {
			clientService.delete(client.id());
			auditLog.record(AuditLog.DELETE, AuditLog.CLIENT, idOf(client), Map.of());
			return Boolean.TRUE;
		}, done -> {
			setBusy(false);
			load();
		}, failure -> {
			setBusy(false);
			shell.setLoading(false);
			handleFailure(failure, "Não foi possível excluir o cliente.");
		});
	}

	private void styleDialog(Dialog<?> dialog) {
		DialogPane pane = dialog.getDialogPane();
		pane.getStyleClass().add("app-dialog");
		URL stylesheet = ClientsPage.class.getResource(STYLESHEET);
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

	private String idOf(ClientDTO client) {
		return client == null || client.id() == null ? null : client.id().toString();
	}

}
