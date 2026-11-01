package com.h_group.gvs_terceirizacoes_desktop.ui;

import javafx.scene.Node;

public interface Page {

	String title();

	Node build();

	default void onShow() {
	}

	default Node action() {
		return null;
	}

}
