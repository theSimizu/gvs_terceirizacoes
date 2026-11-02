package com.h_group.gvs_terceirizacoes_desktop.ui;

import com.h_group.gvs_terceirizacoes_desktop.dto.UserDTO;
import com.h_group.gvs_terceirizacoes_desktop.service.AuthService;
import com.h_group.gvs_terceirizacoes_desktop.session.AuthSession;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class AppShell {

	private static final String DASHBOARD = "dashboard";

	private final Navigator navigator;
	private final AuthService authService;
	private final AuthSession authSession;

	private final Map<String, Page> pages = new LinkedHashMap<>();
	private final Map<String, Node> builtPages = new HashMap<>();
	private final List<Button> navButtons = new ArrayList<>();
	private final Label pageTitle = new Label();
	private final HBox actionBox = new HBox(12);
	private final Label loadingLabel = new Label("Carregando dados...");
	private final Label messageLabel = new Label();
	private final StackPane contentHost = new StackPane();

	public AppShell(
			Navigator navigator,
			AuthService authService,
			AuthSession authSession,
			PageDependencies dependencies) {
		this.navigator = navigator;
		this.authService = authService;
		this.authSession = authSession;

		pages.put(DASHBOARD, new DashboardPage(this, dependencies.dashboardService()));
		pages.put("clientes", new ClientsPage(this, dependencies.clientService(),
				dependencies.clientValidator(), dependencies.auditLog()));
		pages.put("prestadores", new ProvidersPage(this, dependencies.providerService(),
				dependencies.providerValidator(), dependencies.auditLog()));
		pages.put("contratos", new ContractsPage(this, dependencies.contractService(),
				dependencies.contractValidator(), dependencies.clientService(), dependencies.providerService(),
				dependencies.auditLog()));
		pages.put("ordens", new ServiceOrdersPage(this, dependencies.serviceOrderService(),
				dependencies.serviceOrderValidator(), dependencies.contractService(), dependencies.auditLog()));
		pages.put("historico", new HistoryPage(this, dependencies.historyService()));
	}

	public Parent build() {
		BorderPane root = new BorderPane();
		root.getStyleClass().add("dashboard-root");
		root.setLeft(buildSidebar());
		root.setCenter(buildMain());
		navigate(DASHBOARD);
		return root;
	}

	public void navigate(String target) {
		Page page = pages.get(target);
		if (page == null) {
			return;
		}

		for (Button button : navButtons) {
			button.getStyleClass().remove("active");
			if (target.equals(button.getUserData())) {
				button.getStyleClass().add("active");
			}
		}

		pageTitle.setText(page.title());
		actionBox.getChildren().clear();
		Node action = page.action();
		if (action != null) {
			actionBox.getChildren().add(action);
		}

		clearError();
		setLoading(false);
		contentHost.getChildren().setAll(builtPages.computeIfAbsent(target, key -> page.build()));
		page.onShow();
	}

	public void setLoading(boolean loading) {
		loadingLabel.setVisible(loading);
		loadingLabel.setManaged(loading);
	}

	public void showError(String text) {
		messageLabel.setText(text);
		messageLabel.setVisible(true);
		messageLabel.setManaged(true);
	}

	public void clearError() {
		messageLabel.setVisible(false);
		messageLabel.setManaged(false);
	}

	public void sessionExpired() {
		authService.logout();
		navigator.showLogin("Sessão expirada. Faça login novamente.");
	}

	public boolean canManageRegistries() {
		return authSession.isAdministrator();
	}

	public boolean canDelete() {
		return authSession.isAdministrator();
	}

	private VBox buildSidebar() {
		VBox header = new VBox(createLogo());
		header.getStyleClass().add("sidebar-header");
		header.setAlignment(Pos.CENTER);

		VBox nav = new VBox(8);
		nav.getStyleClass().add("sidebar-nav");
		VBox.setVgrow(nav, Priority.ALWAYS);
		pages.forEach((target, page) -> nav.getChildren().add(navButton(page.title(), target)));

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
		button.setOnAction(event -> navigate(target));
		navButtons.add(button);
		return button;
	}

	private HBox buildUserInfo() {
		Label avatar = new Label(initials());
		avatar.getStyleClass().add("avatar");

		Label name = new Label(displayName());
		name.getStyleClass().add("user-name");

		Label role = new Label(authSession.isAdministrator() ? "Administrador" : "Usuário");
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

		actionBox.setAlignment(Pos.CENTER_RIGHT);

		HBox header = new HBox(16, pageTitle, spacer, actionBox);
		header.getStyleClass().add("top-header");
		header.setAlignment(Pos.CENTER_LEFT);

		loadingLabel.getStyleClass().add("dashboard-loading");
		loadingLabel.setVisible(false);
		loadingLabel.setManaged(false);

		messageLabel.getStyleClass().add("dashboard-error");
		messageLabel.setWrapText(true);
		messageLabel.setVisible(false);
		messageLabel.setManaged(false);

		contentHost.getStyleClass().add("content-area");
		VBox.setVgrow(contentHost, Priority.ALWAYS);

		VBox main = new VBox(12, header, loadingLabel, messageLabel, contentHost);
		main.getStyleClass().add("main-content");
		return main;
	}

	private String displayName() {
		UserDTO user = authSession.getUser();
		if (user != null && user.name() != null && !user.name().isBlank()) {
			return user.name();
		}
		return "Usuário";
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
		try (InputStream stream = AppShell.class.getResourceAsStream("/images/logo.png")) {
			if (stream != null) {
				ImageView view = new ImageView(new Image(stream));
				view.setFitWidth(140);
				view.setPreserveRatio(true);
				view.setSmooth(true);
				return view;
			}
		} catch (Exception exception) {
			// Ignore a missing or invalid logo.
		}
		return new ImageView();
	}

}
