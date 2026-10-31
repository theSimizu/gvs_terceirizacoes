package com.h_group.gvs_terceirizacoes_desktop.ui;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiException;
import com.h_group.gvs_terceirizacoes_desktop.dto.UserDTO;
import com.h_group.gvs_terceirizacoes_desktop.service.AuthService;
import com.h_group.gvs_terceirizacoes_desktop.service.DashboardService;
import com.h_group.gvs_terceirizacoes_desktop.service.DashboardSummary;
import com.h_group.gvs_terceirizacoes_desktop.session.AuthSession;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class DashboardView {

	private static final String DASHBOARD = "dashboard";

	private final Navigator navigator;

	private final AuthService authService;

	private final AuthSession authSession;

	private final DashboardService dashboardService;

	private final List<Button> navButtons = new ArrayList<>();

	private final Label pageTitle = new Label("Dashboard");

	private final Label loadingLabel = new Label("Carregando dados...");

	private final Label messageLabel = new Label();

	private final VBox contentArea = new VBox(24);

	private final Button refreshButton = new Button("Atualizar");

	private final Label clientsValue = new Label("0");

	private final Label providersValue = new Label("0");

	private final Label contractsValue = new Label("0");

	private final PieChart ordersChart = new PieChart();

	private final BarChart<String, Number> providersChart = new BarChart<>(new CategoryAxis(), new NumberAxis());

	public DashboardView(Navigator navigator, AuthService authService, AuthSession authSession,
			DashboardService dashboardService) {
		this.navigator = navigator;
		this.authService = authService;
		this.authSession = authSession;
		this.dashboardService = dashboardService;
	}

	public Parent build() {
		BorderPane root = new BorderPane();
		root.getStyleClass().add("dashboard-root");
		root.setLeft(buildSidebar());
		root.setCenter(buildMain());
		showRoute(DASHBOARD, "Dashboard");
		return root;
	}

	private VBox buildSidebar() {
		VBox header = new VBox(createLogo());
		header.getStyleClass().add("sidebar-header");
		header.setAlignment(Pos.CENTER);

		VBox nav = new VBox(8);
		nav.getStyleClass().add("sidebar-nav");
		VBox.setVgrow(nav, Priority.ALWAYS);
		nav.getChildren().addAll(
				navButton("Dashboard", DASHBOARD),
				navButton("Clientes", "clientes"),
				navButton("Prestadores", "prestadores"),
				navButton("Contratos", "contratos"),
				navButton("Ordens de Serviço", "ordens"),
				navButton("Auditoria", "historico"),
				navButton("Usuários do Sistema", "usuarios"));

		VBox footer = new VBox(12);
		footer.getStyleClass().add("sidebar-footer");
		footer.getChildren().addAll(buildUserInfo(), buildLogoutButton());

		VBox sidebar = new VBox(header, nav, footer);
		sidebar.getStyleClass().add("sidebar");
		sidebar.setPrefWidth(280);
		sidebar.setMinWidth(280);
		sidebar.setMaxWidth(280);
		return sidebar;
	}

	private Button navButton(String text, String target) {
		Button button = new Button(text);
		button.getStyleClass().add("nav-item");
		button.setUserData(target);
		button.setMaxWidth(Double.MAX_VALUE);
		button.setAlignment(Pos.CENTER_LEFT);
		button.setOnAction(event -> showRoute(target, text));
		navButtons.add(button);
		return button;
	}

	private HBox buildUserInfo() {
		Label avatar = new Label(initials());
		avatar.getStyleClass().add("avatar");

		Label name = new Label(displayName());
		name.getStyleClass().add("user-name");

		Label role = new Label("GVS Terceirizações");
		role.getStyleClass().add("user-role");

		VBox details = new VBox(2, name, role);
		HBox.setHgrow(details, Priority.ALWAYS);

		HBox info = new HBox(12, avatar, details);
		info.setAlignment(Pos.CENTER_LEFT);
		return info;
	}

	private Button buildLogoutButton() {
		Button logoutButton = new Button("Sair");
		logoutButton.getStyleClass().add("logout-button");
		logoutButton.setMaxWidth(Double.MAX_VALUE);
		logoutButton.setOnAction(event -> {
			authService.logout();
			navigator.showLogin();
		});
		return logoutButton;
	}

	private VBox buildMain() {
		pageTitle.getStyleClass().add("page-title");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		refreshButton.getStyleClass().add("btn-primary");
		refreshButton.setOnAction(event -> loadDashboard());

		HBox header = new HBox(16, pageTitle, spacer, refreshButton);
		header.getStyleClass().add("top-header");
		header.setAlignment(Pos.CENTER_LEFT);

		loadingLabel.getStyleClass().add("dashboard-loading");
		loadingLabel.setVisible(false);
		loadingLabel.setManaged(false);

		messageLabel.getStyleClass().add("dashboard-error");
		messageLabel.setWrapText(true);
		messageLabel.setVisible(false);
		messageLabel.setManaged(false);

		contentArea.getStyleClass().add("content-area");
		VBox.setVgrow(contentArea, Priority.ALWAYS);

		VBox main = new VBox(12, header, loadingLabel, messageLabel, contentArea);
		main.getStyleClass().add("main-content");
		return main;
	}

	private void showRoute(String target, String title) {
		for (Button button : navButtons) {
			button.getStyleClass().remove("active");
			if (target.equals(button.getUserData())) {
				button.getStyleClass().add("active");
			}
		}
		if (DASHBOARD.equals(target)) {
			showDashboard();
		}
		else {
			showPlaceholder(title);
		}
	}

	private void showDashboard() {
		pageTitle.setText("Dashboard");
		refreshButton.setVisible(true);
		refreshButton.setManaged(true);
		hideMessage();
		contentArea.getChildren().clear();
		contentArea.getChildren().add(buildDashboardContent());
		loadDashboard();
	}

	private void showPlaceholder(String title) {
		pageTitle.setText(title);
		refreshButton.setVisible(false);
		refreshButton.setManaged(false);
		hideMessage();
		setLoading(false);
		Label placeholder = new Label("Em construção");
		placeholder.getStyleClass().add("placeholder-text");
		contentArea.getChildren().setAll(placeholder);
	}

	private VBox buildDashboardContent() {
		HBox stats = new HBox(24,
				statCard("Clientes Cadastrados", clientsValue),
				statCard("Prestadores Disponíveis", providersValue),
				statCard("Contratos Ativos", contractsValue));

		ordersChart.setLegendSide(Side.BOTTOM);
		ordersChart.setLabelsVisible(true);
		ordersChart.setMinHeight(260);
		ordersChart.setPrefHeight(280);
		ordersChart.setMaxWidth(Double.MAX_VALUE);

		providersChart.setLegendSide(Side.BOTTOM);
		providersChart.setMinHeight(260);
		providersChart.setPrefHeight(280);
		providersChart.setMaxWidth(Double.MAX_VALUE);

		VBox ordersCard = chartCard("Status das Ordens de Serviço", ordersChart);
		VBox providersCard = chartCard("Especialidades dos Prestadores", providersChart);

		HBox charts = new HBox(24, ordersCard, providersCard);
		return new VBox(24, stats, charts);
	}

	private VBox statCard(String title, Label value) {
		Label titleLabel = new Label(title);
		titleLabel.getStyleClass().add("stat-title");
		value.getStyleClass().add("stat-value");

		VBox card = new VBox(6, titleLabel, value);
		card.getStyleClass().add("stat-card");
		card.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(card, Priority.ALWAYS);
		return card;
	}

	private VBox chartCard(String title, Region chart) {
		Label titleLabel = new Label(title);
		titleLabel.getStyleClass().add("chart-title");

		VBox card = new VBox(12, titleLabel, chart);
		card.getStyleClass().add("chart-card");
		card.setMaxWidth(Double.MAX_VALUE);
		VBox.setVgrow(chart, Priority.ALWAYS);
		HBox.setHgrow(card, Priority.ALWAYS);
		return card;
	}

	private void loadDashboard() {
		hideMessage();
		setLoading(true);
		refreshButton.setDisable(true);

		Task<DashboardSummary> task = new Task<>() {
			@Override
			protected DashboardSummary call() {
				return dashboardService.loadSummary();
			}
		};
		task.setOnSucceeded(event -> {
			refreshButton.setDisable(false);
			setLoading(false);
			applySummary(task.getValue());
		});
		task.setOnFailed(event -> {
			refreshButton.setDisable(false);
			setLoading(false);
			if (isUnauthorized(task.getException())) {
				authService.logout();
				navigator.showLogin("Sessão expirada. Faça login novamente.");
				return;
			}
			showMessage(describe(task.getException()));
		});

		Thread thread = new Thread(task, "dashboard-request");
		thread.setDaemon(true);
		thread.start();
	}

	private void applySummary(DashboardSummary summary) {
		clientsValue.setText(Long.toString(summary.clientsCount()));
		providersValue.setText(Long.toString(summary.availableProvidersCount()));
		contractsValue.setText(Long.toString(summary.activeContractsCount()));

		ordersChart.getData().clear();
		Map<String, String> orderLabels = new LinkedHashMap<>();
		orderLabels.put("pending", "Pendentes");
		orderLabels.put("completed", "Concluídas");
		orderLabels.put("canceled", "Canceladas");
		orderLabels.forEach((status, label) -> {
			long count = summary.ordersByStatus().getOrDefault(status, 0L);
			if (count > 0) {
				ordersChart.getData().add(new PieChart.Data(label + " (" + count + ")", count));
			}
		});

		providersChart.getData().clear();
		XYChart.Series<String, Number> series = new XYChart.Series<>();
		series.setName("Quantidade");
		summary.providersBySpecialty()
				.forEach((specialty, count) -> series.getData().add(new XYChart.Data<>(specialty, count)));
		providersChart.getData().add(series);
	}

	private void setLoading(boolean loading) {
		loadingLabel.setVisible(loading);
		loadingLabel.setManaged(loading);
	}

	private void showMessage(String text) {
		messageLabel.setText(text);
		messageLabel.setVisible(true);
		messageLabel.setManaged(true);
	}

	private void hideMessage() {
		messageLabel.setVisible(false);
		messageLabel.setManaged(false);
	}

	private boolean isUnauthorized(Throwable throwable) {
		return throwable instanceof ApiException apiException && apiException.getStatus() == 401;
	}

	private String describe(Throwable throwable) {
		if (throwable instanceof ApiException apiException
				&& apiException.getMessage() != null && !apiException.getMessage().isBlank()) {
			return apiException.getMessage();
		}
		return "Não foi possível carregar os dados do dashboard. Verifique a conexão com o servidor.";
	}

	private String displayName() {
		UserDTO user = authSession.getUser();
		if (user != null && user.name() != null && !user.name().isBlank()) {
			return user.name();
		}
		return "Administrador";
	}

	private String initials() {
		UserDTO user = authSession.getUser();
		if (user == null || user.name() == null || user.name().isBlank()) {
			return "GVS";
		}
		StringBuilder initials = new StringBuilder();
		for (String part : user.name().trim().split("\\s+")) {
			initials.append(Character.toUpperCase(part.charAt(0)));
			if (initials.length() == 2) {
				break;
			}
		}
		return initials.toString();
	}

	private ImageView createLogo() {
		try (InputStream stream = DashboardView.class.getResourceAsStream("/images/logo.png")) {
			if (stream != null) {
				ImageView view = new ImageView(new Image(stream));
				view.setFitWidth(140);
				view.setPreserveRatio(true);
				view.setSmooth(true);
				return view;
			}
		}
		catch (Exception exception) {
			// Ignore a missing or invalid logo.
		}
		return new ImageView();
	}

}
