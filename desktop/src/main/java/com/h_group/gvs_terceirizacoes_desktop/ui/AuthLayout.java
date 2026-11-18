package com.h_group.gvs_terceirizacoes_desktop.ui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

final class AuthLayout {

	private AuthLayout() {
	}

	static Parent wrap(VBox form) {
		Label eyebrow = new Label("GESTÃO DE SERVIÇOS");
		eyebrow.getStyleClass().add("brand-eyebrow");
		Label description = new Label("Clientes, prestadores, contratos e ordens de serviço.");
		description.getStyleClass().add("brand-description");
		description.setWrapText(true);
		Label footer = new Label("GVS Terceirizações  /  Ambiente de gestão");
		footer.getStyleClass().add("brand-footer");
		Region gap = new Region();
		VBox.setVgrow(gap, Priority.ALWAYS);
		VBox brand = new VBox(22, BrandMark.create(180), eyebrow, description, gap, footer);
		brand.getStyleClass().add("auth-brand");
		brand.setPrefWidth(420);
		brand.setMinWidth(300);

		form.setMaxHeight(Region.USE_PREF_SIZE);
		StackPane formArea = new StackPane(form);
		formArea.getStyleClass().add("auth-form-area");
		formArea.setPrefWidth(450);
		formArea.setMinWidth(0);
		Label compactTitle = new Label("GVS Terceirizações");
		compactTitle.setWrapText(true);
		HBox compactBrand = new HBox(18, BrandMark.create(96), compactTitle);
		compactBrand.setAlignment(Pos.CENTER_LEFT);
		compactBrand.getStyleClass().add("auth-brand-compact");
		BorderPane surface = new BorderPane(formArea);
		surface.setLeft(brand);
		surface.getStyleClass().add("auth-surface");
		surface.setMaxWidth(960);
		surface.setMaxHeight(Region.USE_PREF_SIZE);
		Runnable arrange = () -> {
			boolean compact = UiPreferences.get().current().fontPercent() >= 150
					|| (surface.getWidth() > 0 && surface.getWidth() < 820);
			if (compact) {
				surface.setLeft(null);
				surface.setTop(compactBrand);
				if (!surface.getStyleClass().contains("compact-auth")) surface.getStyleClass().add("compact-auth");
			} else {
				surface.setTop(null);
				surface.setLeft(brand);
				surface.getStyleClass().remove("compact-auth");
			}
		};
		surface.widthProperty().addListener((observable, previous, current) -> arrange.run());
		UiPreferences.get().observe(surface, value -> {
			form.setMaxWidth(420.0 * value.fontPercent() / 100);
			arrange.run();
		});
		StackPane root = new StackPane(surface);
		root.setAlignment(Pos.CENTER);
		root.getStyleClass().add("auth-root");
		ScrollPane scroll = new ScrollPane(root);
		scroll.setFitToWidth(true);
		scroll.setFitToHeight(true);
		scroll.getStyleClass().add("auth-scroll");
		return scroll;
	}
}
