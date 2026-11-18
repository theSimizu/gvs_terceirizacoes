package com.h_group.gvs_terceirizacoes_desktop.ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import javafx.scene.Node;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Window;

final class DialogDesign {

	private DialogDesign() {
	}

	static DialogPane configure(Dialog<?> dialog, Window owner, String title, String description) {
		dialog.setTitle(title);
		dialog.setResizable(true);
		if (owner != null) {
			dialog.initOwner(owner);
		}
		DialogPane pane = dialog.getDialogPane();
		pane.getStyleClass().add("app-dialog");
		double width = Math.min(660, Screen.getPrimary().getVisualBounds().getWidth() - 64);
		pane.setPrefWidth(width);
		pane.setMinWidth(Math.min(520, width));
		URL stylesheet = DialogDesign.class.getResource("/css/app.css");
		if (stylesheet != null) {
			pane.getStylesheets().add(stylesheet.toExternalForm());
		}
		UiPreferences.get().attach(pane);
		UiPreferences.get().observe(pane, value -> {
			double available = Screen.getPrimary().getVisualBounds().getWidth() - 64;
			pane.setPrefWidth(Math.min(660.0 * value.fontPercent() / 100, available));
			pane.setMaxHeight(Screen.getPrimary().getVisualBounds().getHeight() - 64);
		});
		Label eyebrow = new Label("GVS TERCEIRIZAÇÕES");
		eyebrow.getStyleClass().add("dialog-eyebrow");
		Label heading = new Label(title);
		heading.getStyleClass().add("dialog-title");
		heading.setWrapText(true);
		Label subtitle = new Label(description);
		subtitle.getStyleClass().add("dialog-subtitle");
		subtitle.setWrapText(true);
		VBox header = new VBox(7, eyebrow, heading, subtitle);
		header.getStyleClass().add("dialog-heading");
		pane.setHeader(header);
		return pane;
	}

	static ScrollPane form(VBox fields, int... fullWidthFields) {
		GridPane grid = new GridPane();
		grid.getStyleClass().add("dialog-grid");
		grid.setHgap(18);
		grid.setVgap(16);
		for (int i = 0; i < 2; i++) {
			ColumnConstraints column = new ColumnConstraints();
			column.setPercentWidth(50);
			column.setHgrow(Priority.ALWAYS);
			grid.getColumnConstraints().add(column);
		}
		var children = new ArrayList<>(fields.getChildren());
		fields.getChildren().clear();
		int row = 0;
		int column = 0;
		for (int index = 0; index < children.size(); index++) {
			Node field = children.get(index);
			boolean fullWidth = index == children.size() - 1 || Arrays.binarySearch(fullWidthFields, index) >= 0;
			if (fullWidth) {
				if (column != 0) {
					row++;
				}
				grid.add(field, 0, row++, 2, 1);
				column = 0;
			} else {
				grid.add(field, column++, row);
				if (column == 2) {
					column = 0;
					row++;
				}
			}
		}
		ScrollPane scroll = new ScrollPane(grid);
		scroll.getStyleClass().add("dialog-scroll");
		scroll.setFitToWidth(true);
		scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scroll.setMinHeight(0);
		scroll.setMaxHeight(Math.max(180, Screen.getPrimary().getVisualBounds().getHeight() - 270));
		return scroll;
	}
}
