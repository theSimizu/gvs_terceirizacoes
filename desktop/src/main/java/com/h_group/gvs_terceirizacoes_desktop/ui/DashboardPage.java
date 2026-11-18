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
	private final Label ordersSummary = new Label();
	private final Label providersSummary = new Label();

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

		ordersCard.getChildren().add(ordersSummary);
		providersCard.getChildren().add(providersSummary);
		HBox charts = new HBox(24, ordersCard, providersCard);
		VBox stackedCharts = new VBox(24);
		VBox stackedStats = new VBox(16);
		var statCards = java.util.List.copyOf(stats.getChildren());
		VBox root = new VBox(24, stats, charts);
		UiPreferences.get().observe(root, value -> {
			var targetCharts = value.fontPercent() >= 150 ? stackedCharts : charts;
			var targetStats = value.fontPercent() >= 150 ? stackedStats : stats;
			if (root.getChildren().get(1) != targetCharts) {
				charts.getChildren().clear();
				stackedCharts.getChildren().clear();
				targetCharts.getChildren().addAll(ordersCard, providersCard);
				root.getChildren().set(1, targetCharts);
				stats.getChildren().clear();
				stackedStats.getChildren().clear();
				targetStats.getChildren().addAll(statCards);
				root.getChildren().set(0, targetStats);
			}
		});
		return root;
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
		for (Label summary : new Label[] {ordersSummary, providersSummary}) {
			summary.setWrapText(true);
			summary.getStyleClass().add("chart-summary");
		}
		UiPreferences.get().observe(ordersChart, value -> {
			ordersChart.setAnimated(!value.reducedMotion());
			providersChart.setAnimated(!value.reducedMotion());
		});
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
		orderLabels.put("in_progress", "Em andamento");
		String orderText = orderLabels.entrySet().stream()
				.map(entry -> entry.getValue() + ": " + summary.ordersByStatus().getOrDefault(entry.getKey(), 0L))
				.collect(java.util.stream.Collectors.joining(" · "));
		ordersSummary.setText(orderText);
		ordersChart.setAccessibleText("Ordens de serviço. " + orderText);
		String providerText = summary.providersBySpecialty().entrySet().stream()
				.map(entry -> entry.getKey() + ": " + entry.getValue())
				.collect(java.util.stream.Collectors.joining(" · "));
		providersSummary.setText(providerText.isEmpty() ? "Nenhum prestador por especialidade." : providerText);
		providersChart.setAccessibleText("Especialidades dos prestadores. " + providersSummary.getText());
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
		titleLabel.setWrapText(true);
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
