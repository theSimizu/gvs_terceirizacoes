package com.h_group.gvs_terceirizacoes_desktop.ui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
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
		Label headline = new Label("Tudo conectado.\nTrabalho organizado.");
		headline.getStyleClass().add("brand-headline");
		Label description = new Label("Clientes, prestadores e serviços em um só lugar.\nMais clareza para acompanhar sua operação.");
		description.getStyleClass().add("brand-description");
		description.setWrapText(true);
		Label footer = new Label("GVS Terceirizações  /  Ambiente de gestão");
		footer.getStyleClass().add("brand-footer");
		Region gap = new Region();
		VBox.setVgrow(gap, Priority.ALWAYS);
		VBox brand = new VBox(22, BrandMark.create(180), eyebrow, headline, description, gap, footer);
		brand.getStyleClass().add("auth-brand");
		brand.setPrefWidth(420);
		brand.setMinWidth(300);
		HBox.setHgrow(brand, Priority.ALWAYS);

		form.setMaxHeight(Region.USE_PREF_SIZE);
		StackPane formArea = new StackPane(form);
		formArea.getStyleClass().add("auth-form-area");
		formArea.setPrefWidth(450);
		formArea.setMinWidth(360);
		HBox.setHgrow(formArea, Priority.ALWAYS);
		HBox surface = new HBox(brand, formArea);
		surface.getStyleClass().add("auth-surface");
		surface.setMaxWidth(960);
		surface.setMaxHeight(Region.USE_PREF_SIZE);
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
