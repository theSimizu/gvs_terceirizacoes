package com.h_group.gvs_terceirizacoes_desktop;

import com.h_group.gvs_terceirizacoes_desktop.service.AuthService;
import com.h_group.gvs_terceirizacoes_desktop.ui.Navigator;
import com.h_group.gvs_terceirizacoes_desktop.ui.FxAsync;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Integra o ciclo de vida JavaFX ao contexto Spring sem iniciar servidor web.
 * A verificação de sessão ocorre em segundo plano antes de apresentar a área autenticada.
 */
public class DesktopFxApplication extends Application {

	private ConfigurableApplicationContext context;

	@Override
	public void init() {
		context = new SpringApplicationBuilder(GvsTerceirizacoesDesktopApplication.class)
				.web(WebApplicationType.NONE)
				.headless(false)
				.run(getParameters().getRaw().toArray(new String[0]));
	}

	@Override
	public void start(Stage stage) {
		Navigator navigator = context.getBean(Navigator.class);
		AuthService authService = context.getBean(AuthService.class);
		navigator.initialize(stage);
		VBox loading = new VBox(16, new ProgressIndicator(), new Label("Verificando sua sessão…"));
		loading.setAlignment(Pos.CENTER);
		stage.setScene(new Scene(loading));
		stage.show();
		FxAsync.run(authService::restoreSession, restored -> {
			if (restored) {
				navigator.showHome();
			} else {
				navigator.showLogin();
			}
		}, failure -> navigator.showLogin("Não foi possível verificar a sessão. Entre novamente."));
	}

	@Override
	public void stop() {
		if (context != null) {
			context.close();
		}
		Platform.exit();
	}

}
