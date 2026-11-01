package com.h_group.gvs_terceirizacoes_desktop.ui;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiException;
import com.h_group.gvs_terceirizacoes_desktop.dto.HistoryDTO;
import com.h_group.gvs_terceirizacoes_desktop.service.HistoryService;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

public class HistoryPage implements Page {

	private static final int PAGE_SIZE = 10;

	private static final String ALL = "";

	private static final String STYLESHEET = "/css/app.css";

	private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
			.withZone(ZoneId.systemDefault());

	private static final List<String> ACTIONS = List.of("create", "update", "delete", "status_change");

	private static final List<String> ENTITIES = List.of("Client", "Provider", "Contract", "ServiceOrder");

	private final AppShell shell;

	private final HistoryService historyService;

	private final List<HistoryDTO> allEntries = new ArrayList<>();

	private final List<HistoryDTO> visibleEntries = new ArrayList<>();

	private final ObservableList<HistoryDTO> pageItems = FXCollections.observableArrayList();

	private final Map<String, Button> headers = new HashMap<>();

	private final TableView<HistoryDTO> table = new TableView<>(pageItems);

	private final TextField searchField = new TextField();

	private final ComboBox<String> actionFilter = new ComboBox<>();

	private final ComboBox<String> entityFilter = new ComboBox<>();

	private final Label countLabel = new Label();

	private final Label pageLabel = new Label();

	private final Button refreshButton = new Button("Atualizar");

	private final Button previousButton = new Button("Anterior");

	private final Button nextButton = new Button("Próxima");

	private final HBox actions = new HBox(12, refreshButton);

	private String sortField = "date";

	private boolean ascending = false;

	private int currentPage = 1;

	private int pageCount = 1;

	public HistoryPage(AppShell shell, HistoryService historyService) {
		this.shell = shell;
		this.historyService = historyService;
		configureActions();
		configureFilters();
		configureTable();
		configurePagination();
	}

	@Override
	public String title() {
		return "Auditoria";
	}

	@Override
	public Node build() {
		searchField.getStyleClass().add("search-field");
		searchField.setPromptText("Buscar por ação, entidade, documento ou detalhes");
		searchField.textProperty().addListener((observable, oldValue, newValue) -> {
			currentPage = 1;
			refreshView();
		});

		countLabel.getStyleClass().add("page-label");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		HBox toolbar = new HBox(12, searchField, actionFilter, entityFilter, spacer, countLabel);
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
		actions.setAlignment(Pos.CENTER_RIGHT);
	}

	private void configureFilters() {
		actionFilter.getStyleClass().add("filter-combo");
		actionFilter.getItems().add(ALL);
		actionFilter.getItems().addAll(ACTIONS);
		actionFilter.setConverter(filterConverter("Todas as Ações", HistoryFormats::actionLabel));
		actionFilter.setValue(ALL);
		actionFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
			currentPage = 1;
			refreshView();
		});

		entityFilter.getStyleClass().add("filter-combo");
		entityFilter.getItems().add(ALL);
		entityFilter.getItems().addAll(ENTITIES);
		entityFilter.setConverter(filterConverter("Todas as Entidades", HistoryFormats::entityLabel));
		entityFilter.setValue(ALL);
		entityFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
			currentPage = 1;
			refreshView();
		});
	}

	private StringConverter<String> filterConverter(String allLabel, Function<String, String> label) {
		return new StringConverter<>() {
			@Override
			public String toString(String value) {
				if (value == null || value.isEmpty()) {
					return allLabel;
				}
				return label.apply(value);
			}

			@Override
			public String fromString(String text) {
				return text;
			}
		};
	}

	private void configureTable() {
		table.getStyleClass().add("data-table");
		table.setEditable(false);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

		TableColumn<HistoryDTO, Instant> date = dateColumn();
		date.setPrefWidth(150);
		TableColumn<HistoryDTO, String> action = actionColumn();
		action.setPrefWidth(150);
		TableColumn<HistoryDTO, String> entity = sortedColumn("entity", "Entidade",
				entry -> HistoryFormats.entityLabel(entry.entity()));
		entity.setPrefWidth(150);
		TableColumn<HistoryDTO, String> document = documentColumn();
		document.setPrefWidth(110);
		TableColumn<HistoryDTO, String> details = detailsColumn();
		details.setPrefWidth(300);
		TableColumn<HistoryDTO, Void> actionsColumn = actionsColumn();
		actionsColumn.setPrefWidth(80);

		table.getColumns().setAll(date, action, entity, document, details, actionsColumn);
	}

	private void configurePagination() {
		previousButton.getStyleClass().add("btn-secondary");
		nextButton.getStyleClass().add("btn-secondary");
		pageLabel.getStyleClass().add("page-label");

		previousButton.setOnAction(event -> changePage(-1));
		nextButton.setOnAction(event -> changePage(1));
	}

	private TableColumn<HistoryDTO, Instant> dateColumn() {
		TableColumn<HistoryDTO, Instant> column = new TableColumn<>();
		column.setGraphic(header("date", "Data/Hora"));
		column.setSortable(false);
		column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().date()));
		column.setCellFactory(ignored -> new TableCell<>() {
			@Override
			protected void updateItem(Instant date, boolean empty) {
				super.updateItem(date, empty);
				if (empty) {
					setText(null);
					return;
				}
				setText(date == null ? "—" : TIMESTAMP_FORMAT.format(date));
			}
		});
		return column;
	}

	private TableColumn<HistoryDTO, String> actionColumn() {
		TableColumn<HistoryDTO, String> column = new TableColumn<>();
		column.setGraphic(header("action", "Ação"));
		column.setSortable(false);
		column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().action()));
		column.setCellFactory(ignored -> new TableCell<>() {
			@Override
			protected void updateItem(String action, boolean empty) {
				super.updateItem(action, empty);
				if (empty || action == null || action.isBlank()) {
					setGraphic(null);
					setText(empty ? null : "—");
					return;
				}
				Label badge = new Label(HistoryFormats.actionLabel(action));
				badge.getStyleClass().addAll("status-badge", HistoryFormats.actionBadgeClass(action));
				setGraphic(badge);
				setText(null);
			}
		});
		return column;
	}

	private TableColumn<HistoryDTO, String> sortedColumn(String field, String title,
			Function<HistoryDTO, String> value) {
		TableColumn<HistoryDTO, String> column = new TableColumn<>();
		column.setGraphic(header(field, title));
		column.setSortable(false);
		column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(value.apply(data.getValue())));
		return column;
	}

	private TableColumn<HistoryDTO, String> documentColumn() {
		TableColumn<HistoryDTO, String> column = sortedColumn("document", "Documento",
				entry -> HistoryFormats.documentLabel(entry.documentId()));
		column.setCellFactory(ignored -> new TableCell<>() {
			@Override
			protected void updateItem(String document, boolean empty) {
				super.updateItem(document, empty);
				if (empty) {
					setText(null);
					setTooltip(null);
					return;
				}
				int index = getIndex();
				HistoryDTO entry = index >= 0 && index < getTableView().getItems().size()
						? getTableView().getItems().get(index) : null;
				setText(document);
				setTooltip(entry == null || entry.documentId() == null ? null
						: new Tooltip(entry.documentId()));
			}
		});
		return column;
	}

	private TableColumn<HistoryDTO, String> detailsColumn() {
		TableColumn<HistoryDTO, String> column = new TableColumn<>();
		column.setGraphic(header("details", "Detalhes"));
		column.setSortable(false);
		column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(
				HistoryFormats.summary(data.getValue().details())));
		column.setCellFactory(ignored -> new TableCell<>() {
			@Override
			protected void updateItem(String summary, boolean empty) {
				super.updateItem(summary, empty);
				if (empty) {
					setText(null);
					setTooltip(null);
					return;
				}
				setText(shorten(summary));
				setTooltip(summary == null || "—".equals(summary) ? null : new Tooltip(summary));
			}
		});
		return column;
	}

	private TableColumn<HistoryDTO, Void> actionsColumn() {
		TableColumn<HistoryDTO, Void> column = new TableColumn<>();
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
				HistoryDTO entry = getTableView().getItems().get(index);
				setGraphic(linkButton("Ver", "btn-link", event -> showDetails(entry)));
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

		FxAsync.run(historyService::findAll, entries -> {
			allEntries.clear();
			allEntries.addAll(entries);
			currentPage = 1;
			refreshView();
			setBusy(false);
			shell.setLoading(false);
		}, failure -> {
			setBusy(false);
			shell.setLoading(false);
			handleFailure(failure);
		});
	}

	private void refreshView() {
		String query = searchField.getText() == null ? ""
				: searchField.getText().trim().toLowerCase(Locale.ROOT);
		String action = actionFilter.getValue();
		String entity = entityFilter.getValue();

		visibleEntries.clear();
		for (HistoryDTO entry : allEntries) {
			if (matches(entry, query) && matchesFilter(entry.action(), action)
					&& matchesFilter(entry.entity(), entity)) {
				visibleEntries.add(entry);
			}
		}

		Comparator<HistoryDTO> comparator = comparator();
		if (comparator != null) {
			visibleEntries.sort(comparator);
		}
		renderPage();
	}

	private void renderPage() {
		pageCount = Math.max(1, (visibleEntries.size() + PAGE_SIZE - 1) / PAGE_SIZE);
		currentPage = Math.max(1, Math.min(currentPage, pageCount));

		int from = (currentPage - 1) * PAGE_SIZE;
		int to = Math.min(from + PAGE_SIZE, visibleEntries.size());
		pageItems.setAll(visibleEntries.subList(from, to));

		int total = visibleEntries.size();
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

	private Comparator<HistoryDTO> comparator() {
		Comparator<HistoryDTO> comparator = switch (sortField) {
			case "date" -> Comparator.comparing(HistoryDTO::date,
					Comparator.nullsLast(Comparator.naturalOrder()));
			case "action" -> Comparator.comparing(
					(HistoryDTO entry) -> HistoryFormats.actionLabel(entry.action()),
					String.CASE_INSENSITIVE_ORDER);
			case "entity" -> Comparator.comparing(
					(HistoryDTO entry) -> HistoryFormats.entityLabel(entry.entity()),
					String.CASE_INSENSITIVE_ORDER);
			case "document" -> Comparator.comparing(
					(HistoryDTO entry) -> HistoryFormats.documentLabel(entry.documentId()),
					String.CASE_INSENSITIVE_ORDER);
			case "details" -> Comparator.comparing(
					(HistoryDTO entry) -> HistoryFormats.summary(entry.details()),
					String.CASE_INSENSITIVE_ORDER);
			default -> null;
		};
		if (comparator == null) {
			return null;
		}
		return ascending ? comparator : comparator.reversed();
	}

	private boolean matches(HistoryDTO entry, String query) {
		if (query.isEmpty()) {
			return true;
		}
		return contains(HistoryFormats.actionLabel(entry.action()), query)
				|| contains(HistoryFormats.entityLabel(entry.entity()), query)
				|| contains(entry.documentId(), query)
				|| contains(HistoryFormats.summary(entry.details()), query);
	}

	private boolean matchesFilter(String value, String filter) {
		if (filter == null || filter.isEmpty()) {
			return true;
		}
		return filter.equalsIgnoreCase(value);
	}

	private boolean contains(String value, String query) {
		return value != null && value.toLowerCase(Locale.ROOT).contains(query);
	}

	private String shorten(String text) {
		if (text == null) {
			return "";
		}
		String singleLine = text.replaceAll("\\s+", " ").trim();
		return singleLine.length() <= 80 ? singleLine : singleLine.substring(0, 77) + "...";
	}

	private void showDetails(HistoryDTO entry) {
		Label document = new Label("Documento: "
				+ (entry.documentId() == null || entry.documentId().isBlank() ? "—" : entry.documentId()));
		Label date = new Label("Data: "
				+ (entry.date() == null ? "—" : TIMESTAMP_FORMAT.format(entry.date())));

		Label detailsLabel = new Label("Detalhes");
		detailsLabel.getStyleClass().add("form-label");

		TextArea details = new TextArea(String.join("\n", HistoryFormats.detailLines(entry.details())));
		details.setEditable(false);
		details.setWrapText(true);
		details.setPrefRowCount(8);
		details.getStyleClass().add("form-input");

		VBox content = new VBox(12, document, date, detailsLabel, details);
		content.setPadding(new Insets(4, 0, 0, 0));

		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Detalhes da Auditoria");
		alert.setHeaderText(HistoryFormats.actionLabel(entry.action()) + " · "
				+ HistoryFormats.entityLabel(entry.entity()));
		alert.getDialogPane().setContent(content);
		alert.getDialogPane().setPrefWidth(480);
		styleDialog(alert);
		alert.getButtonTypes().setAll(new ButtonType("Fechar", ButtonBar.ButtonData.OK_DONE));
		alert.showAndWait();
	}

	private void styleDialog(Dialog<?> dialog) {
		DialogPane pane = dialog.getDialogPane();
		pane.getStyleClass().add("app-dialog");
		URL stylesheet = HistoryPage.class.getResource(STYLESHEET);
		if (stylesheet != null) {
			pane.getStylesheets().add(stylesheet.toExternalForm());
		}
	}

	private void handleFailure(Throwable failure) {
		if (failure instanceof ApiException apiException && apiException.getStatus() == 401) {
			shell.sessionExpired();
			return;
		}
		shell.showError(describe(failure));
	}

	private String describe(Throwable failure) {
		if (failure instanceof ApiException apiException
				&& apiException.getMessage() != null && !apiException.getMessage().isBlank()) {
			return apiException.getMessage();
		}
		return "Não foi possível carregar a auditoria.";
	}

	private void setBusy(boolean busy) {
		refreshButton.setDisable(busy);
		table.setDisable(busy);
		actionFilter.setDisable(busy);
		entityFilter.setDisable(busy);
	}

}
