package com.h_group.gvs_terceirizacoes_desktop.ui;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiException;
import com.h_group.gvs_terceirizacoes_desktop.dto.ProviderDTO;
import com.h_group.gvs_terceirizacoes_desktop.service.AuditLog;
import com.h_group.gvs_terceirizacoes_desktop.service.ProviderService;
import com.h_group.gvs_terceirizacoes_desktop.validation.ProviderValidator;
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

public class ProvidersPage implements Page {

	private static final int PAGE_SIZE = 10;

	private static final String STYLESHEET = "/css/app.css";

	private final AppShell shell;

	private final ProviderService providerService;

	private final ProviderValidator providerValidator;

	private final AuditLog auditLog;

	private final List<ProviderDTO> allProviders = new ArrayList<>();

	private final List<ProviderDTO> visibleProviders = new ArrayList<>();

	private final ObservableList<ProviderDTO> pageItems = FXCollections.observableArrayList();

	private final Map<String, Button> headers = new HashMap<>();

	private final TableView<ProviderDTO> table = new TableView<>(pageItems);

	private final TextField searchField = new TextField();

	private final Label countLabel = new Label();

	private final Label pageLabel = new Label();

	private final Button refreshButton = new Button("Atualizar");

	private final Button newButton = new Button("Novo Prestador");

	private final Button previousButton = new Button("Anterior");

	private final Button nextButton = new Button("Próxima");

	private final HBox actions = new HBox(12, refreshButton, newButton);

	private String sortField = "name";

	private boolean ascending = true;

	private int currentPage = 1;

	private int pageCount = 1;

	public ProvidersPage(AppShell shell, ProviderService providerService, ProviderValidator providerValidator,
			AuditLog auditLog) {
		this.shell = shell;
		this.providerService = providerService;
		this.providerValidator = providerValidator;
		this.auditLog = auditLog;
		configureActions();
		configureTable();
		configurePagination();
	}

	@Override
	public String title() {
		return "Prestadores";
	}

	@Override
	public Node build() {
		searchField.getStyleClass().add("search-field");
		searchField.setPromptText("Buscar por nome, CPF/CNPJ, especialidade ou e-mail");
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

		TableColumn<ProviderDTO, String> name = sortedColumn("name", "Nome", ProviderDTO::name);
		name.setPrefWidth(220);
		TableColumn<ProviderDTO, String> taxId = sortedColumn("taxId", "CPF/CNPJ", ProviderDTO::taxId);
		taxId.setPrefWidth(170);
		TableColumn<ProviderDTO, String> specialty =
				sortedColumn("specialty", "Especialidade", ProviderDTO::specialty);
		specialty.setPrefWidth(180);
		TableColumn<ProviderDTO, String> phone = sortedColumn("phone", "Telefone", ProviderDTO::phone);
		phone.setPrefWidth(150);
		TableColumn<ProviderDTO, Boolean> status = statusColumn();
		status.setPrefWidth(120);
		TableColumn<ProviderDTO, Void> actionsColumn = actionsColumn();
		actionsColumn.setPrefWidth(240);

		table.getColumns().setAll(name, taxId, specialty, phone, status, actionsColumn);
	}

	private void configurePagination() {
		previousButton.getStyleClass().add("btn-secondary");
		nextButton.getStyleClass().add("btn-secondary");
		pageLabel.getStyleClass().add("page-label");

		previousButton.setOnAction(event -> changePage(-1));
		nextButton.setOnAction(event -> changePage(1));
	}

	private TableColumn<ProviderDTO, String> sortedColumn(String field, String title,
			Function<ProviderDTO, String> value) {
		TableColumn<ProviderDTO, String> column = new TableColumn<>();
		column.setGraphic(header(field, title));
		column.setSortable(false);
		column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(value.apply(data.getValue())));
		return column;
	}

	private TableColumn<ProviderDTO, Boolean> statusColumn() {
		TableColumn<ProviderDTO, Boolean> column = new TableColumn<>();
		column.setGraphic(header("available", "Status"));
		column.setSortable(false);
		column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().available()));
		column.setCellFactory(ignored -> new TableCell<>() {
			@Override
			protected void updateItem(Boolean available, boolean empty) {
				super.updateItem(available, empty);
				if (empty || available == null) {
					setGraphic(null);
					setText(null);
					return;
				}
				Label badge = new Label(available ? "DISPONÍVEL" : "OCUPADO");
				badge.getStyleClass().addAll("status-badge", available ? "status-active" : "status-inactive");
				setGraphic(badge);
				setText(null);
			}
		});
		return column;
	}

	private TableColumn<ProviderDTO, Void> actionsColumn() {
		TableColumn<ProviderDTO, Void> column = new TableColumn<>();
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
				ProviderDTO provider = getTableView().getItems().get(index);
				Button edit = linkButton("Editar", "btn-link", event -> openForm(provider));
				Button toggle = linkButton(provider.available() ? "Marcar Ocupado" : "Marcar Disponível",
						"btn-link-warning", event -> toggleAvailability(provider));
				Button delete = linkButton("Excluir", "btn-link-danger", event -> delete(provider));
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

		FxAsync.run(providerService::findAll, providers -> {
			allProviders.clear();
			allProviders.addAll(providers);
			currentPage = 1;
			refreshView();
			setBusy(false);
			shell.setLoading(false);
		}, failure -> {
			setBusy(false);
			shell.setLoading(false);
			handleFailure(failure, "Não foi possível carregar os prestadores. Verifique a conexão com o servidor.");
		});
	}

	private void refreshView() {
		String query = searchField.getText() == null ? ""
				: searchField.getText().trim().toLowerCase(Locale.ROOT);

		visibleProviders.clear();
		for (ProviderDTO provider : allProviders) {
			if (matches(provider, query)) {
				visibleProviders.add(provider);
			}
		}

		Comparator<ProviderDTO> comparator = comparator();
		if (comparator != null) {
			visibleProviders.sort(comparator);
		}
		renderPage();
	}

	private void renderPage() {
		pageCount = Math.max(1, (visibleProviders.size() + PAGE_SIZE - 1) / PAGE_SIZE);
		currentPage = Math.max(1, Math.min(currentPage, pageCount));

		int from = (currentPage - 1) * PAGE_SIZE;
		int to = Math.min(from + PAGE_SIZE, visibleProviders.size());
		pageItems.setAll(visibleProviders.subList(from, to));

		int total = visibleProviders.size();
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

	private Comparator<ProviderDTO> comparator() {
		Comparator<ProviderDTO> comparator = switch (sortField) {
			case "name" -> Comparator.comparing((ProviderDTO provider) -> text(provider.name()),
					String.CASE_INSENSITIVE_ORDER);
			case "taxId" -> Comparator.comparing((ProviderDTO provider) -> digits(provider.taxId()));
			case "specialty" -> Comparator.comparing((ProviderDTO provider) -> text(provider.specialty()),
					String.CASE_INSENSITIVE_ORDER);
			case "phone" -> Comparator.comparing((ProviderDTO provider) -> digits(provider.phone()));
			case "available" -> Comparator.comparing((ProviderDTO provider) -> provider.available() ? 0 : 1);
			default -> null;
		};
		if (comparator == null) {
			return null;
		}
		return ascending ? comparator : comparator.reversed();
	}

	private boolean matches(ProviderDTO provider, String query) {
		if (query.isEmpty()) {
			return true;
		}
		if (contains(provider.name(), query) || contains(provider.specialty(), query)
				|| contains(provider.email(), query)) {
			return true;
		}
		if (contains(provider.taxId(), query)) {
			return true;
		}
		String digits = query.replaceAll("\\D", "");
		return !digits.isEmpty() && digits(provider.taxId()).contains(digits);
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

	private String idOf(ProviderDTO provider) {
		return provider == null || provider.id() == null ? null : provider.id().toString();
	}

	private void openForm(ProviderDTO existing) {
		boolean creating = existing == null;
		Function<ProviderForm, ProviderDTO> saver;
		if (creating) {
			saver = form -> {
				ProviderDTO saved = providerService.create(form.toCreateRequest());
				auditLog.record(AuditLog.CREATE, AuditLog.PROVIDER, idOf(saved), form.toDetails());
				return saved;
			};
		}
		else {
			saver = form -> {
				ProviderDTO saved = providerService.update(existing.id(), form.toUpdateRequest());
				auditLog.record(AuditLog.UPDATE, AuditLog.PROVIDER, idOf(saved), form.toDetails());
				return saved;
			};
		}

		ProviderFormDialog dialog = new ProviderFormDialog(shell, providerValidator,
				creating ? "Novo Prestador" : "Editar Prestador",
				creating ? "Salvar Prestador" : "Atualizar Prestador",
				existing, saver);
		if (dialog.showAndWait()) {
			load();
		}
	}

	private void toggleAvailability(ProviderDTO provider) {
		shell.clearError();
		setBusy(true);
		shell.setLoading(true);
		FxAsync.run(() -> {
			ProviderDTO updated = providerService.setAvailable(provider.id(), !provider.available());
			auditLog.record(AuditLog.STATUS_CHANGE, AuditLog.PROVIDER, idOf(provider),
					Map.of("available", !provider.available()));
			return updated;
		}, updated -> {
			setBusy(false);
			load();
		}, failure -> {
			setBusy(false);
			shell.setLoading(false);
			handleFailure(failure, "Não foi possível atualizar a disponibilidade do prestador.");
		});
	}

	private void delete(ProviderDTO provider) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Excluir prestador");
		alert.setHeaderText("Excluir \"" + provider.name() + "\"?");
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
			providerService.delete(provider.id());
			auditLog.record(AuditLog.DELETE, AuditLog.PROVIDER, idOf(provider), Map.of());
			return Boolean.TRUE;
		}, done -> {
			setBusy(false);
			load();
		}, failure -> {
			setBusy(false);
			shell.setLoading(false);
			handleFailure(failure, "Não foi possível excluir o prestador.");
		});
	}

	private void styleDialog(Dialog<?> dialog) {
		DialogPane pane = dialog.getDialogPane();
		pane.getStyleClass().add("app-dialog");
		URL stylesheet = ProvidersPage.class.getResource(STYLESHEET);
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

}
