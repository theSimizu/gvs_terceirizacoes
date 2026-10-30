package com.h_group.gvs_terceirizacoes_desktop.ui;

import com.h_group.gvs_terceirizacoes_desktop.dto.UserDTO;
import com.h_group.gvs_terceirizacoes_desktop.session.AuthSession;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class HomeView {

	private final Navigator navigator;

	private final AuthSession authSession;

	public HomeView(Navigator navigator, AuthSession authSession) {
		this.navigator = navigator;
		this.authSession = authSession;
	}

	public Parent build() {
		StackPane root = new StackPane();
		root.getStyleClass().add("home-root");

		Label title = new Label("Bem-vindo, " + displayName());
		title.getStyleClass().add("home-title");

		Label subtitle = new Label("Área do sistema em construção.");
		subtitle.getStyleClass().add("home-subtitle");

		Button logoutButton = new Button("Sair");
		logoutButton.getStyleClass().add("auth-button");
		logoutButton.setOnAction(event -> {
			authSession.clear();
			navigator.showLogin();
		});

		VBox content = new VBox(16, title, subtitle, logoutButton);
		content.setAlignment(Pos.CENTER);
		root.getChildren().add(content);
		return root;
	}

	private String displayName() {
		UserDTO user = authSession.getUser();
		if (user != null && user.name() != null && !user.name().isBlank()) {
			return user.name();
		}
		return "usuário";
	}

}
