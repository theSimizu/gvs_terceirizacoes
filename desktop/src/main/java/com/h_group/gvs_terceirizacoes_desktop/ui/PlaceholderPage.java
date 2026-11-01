package com.h_group.gvs_terceirizacoes_desktop.ui;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PlaceholderPage implements Page {

	private final String title;

	public PlaceholderPage(String title) {
		this.title = title;
	}

	@Override
	public String title() {
		return title;
	}

	@Override
	public Node build() {
		Label placeholder = new Label("Em construção");
		placeholder.getStyleClass().add("placeholder-text");
		return new VBox(placeholder);
	}

}
