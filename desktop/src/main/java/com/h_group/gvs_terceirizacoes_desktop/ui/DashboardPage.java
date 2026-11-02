package com.h_group.gvs_terceirizacoes_desktop.ui;

import com.h_group.gvs_terceirizacoes_desktop.api.ApiException;
import com.h_group.gvs_terceirizacoes_desktop.dto.DashboardSummary;
import com.h_group.gvs_terceirizacoes_desktop.service.DashboardService;

import java.util.LinkedHashMap;
import java.util.Map;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class DashboardPage implements Page {

	private final AppShell shell;

	private final DashboardService dashboardService;

	private final Button refreshButton = new Button("Atualizar");

	private final Label clientsValue = new Label("0");

	private final Label providersValue = new Label("0");

	private final Label contractsValue = new Label("0");

	private final PieChart ordersChart = new PieChart();

	private final BarChart<String, Number> providersChart = new BarChart<>(new CategoryAxis(), new NumberAxis());

	public DashboardPage(AppShell shell, DashboardService dashboardService) {
		this.shell = shell;
		this.dashboardService = dashboardService;
		refreshButton.getStyleClass().add("btn-primary");
		refreshButton.setOnAction(event -> load());
		configureCharts();
	}

	@Override
	public String title() {
		return "Dashboard";
	}

	@Override
	public Node build() {
		HBox stats = new HBox(24,
				statCard("Clientes Cadastrados", clientsValue),
				statCard("Prestadores Disponíveis", providersValue),
				statCard("Contratos Ativos", contractsValue));

		VBox ordersCard = chartCard("Status das Ordens de Serviço", ordersChart);
		VBox providersCard = chartCard("Especialidades dos Prestadores", providersChart);

		HBox charts = new HBox(24, ordersCard, providersCard);
		return new VBox(24, stats, charts);
	}

	@Override
	public Node action() {
		return refreshButton;
	}

	@Override
	public void onShow() {
		load();
	}

	private void configureCharts() {
		ordersChart.setLegendSide(Side.BOTTOM);
		ordersChart.setLabelsVisible(true);
		ordersChart.setMinHeight(260);
		ordersChart.setPrefHeight(280);
		ordersChart.setMaxWidth(Double.MAX_VALUE);

		providersChart.setLegendSide(Side.BOTTOM);
		providersChart.setMinHeight(260);
		providersChart.setPrefHeight(280);
		providersChart.setMaxWidth(Double.MAX_VALUE);
	}

	private void load() {
		shell.clearError();
		shell.setLoading(true);
		refreshButton.setDisable(true);

		FxAsync.run(dashboardService::loadSummary, summary -> {
			refreshButton.setDisable(false);
			shell.setLoading(false);
			applySummary(summary);
		}, failure -> {
			refreshButton.setDisable(false);
			shell.setLoading(false);
			if (failure instanceof ApiException apiException && apiException.getStatus() == 401) {
				shell.sessionExpired();
				return;
			}
			shell.showError(describe(failure));
		});
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

	private String describe(Throwable throwable) {
		if (throwable instanceof ApiException apiException
				&& apiException.getMessage() != null && !apiException.getMessage().isBlank()) {
			return apiException.getMessage();
		}
		return "Não foi possível carregar os dados do dashboard. Verifique a conexão com o servidor.";
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

}
