package com.h_group.gvs_terceirizacoes_desktop.ui;

import java.net.URL;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

final class BrandMark {

	private BrandMark() {
	}

	static StackPane create(double width) {
		URL resource = BrandMark.class.getResource("/images/logo.png");
		ImageView view = resource == null ? new ImageView() : new ImageView(new Image(resource.toExternalForm()));
		view.setFitWidth(width);
		view.setPreserveRatio(true);
		view.setSmooth(true);
		view.setBlendMode(BlendMode.MULTIPLY);
		view.setAccessibleText("Grupo GVS Terceirizações");
		StackPane surface = new StackPane(view);
		surface.getStyleClass().add("brand-logo-surface");
		surface.setMaxWidth(Region.USE_PREF_SIZE);
		return surface;
	}
}
