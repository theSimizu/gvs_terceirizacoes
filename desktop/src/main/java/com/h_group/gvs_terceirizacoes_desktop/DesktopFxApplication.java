package com.h_group.gvs_terceirizacoes_desktop;

import com.h_group.gvs_terceirizacoes_desktop.service.AuthService;
import com.h_group.gvs_terceirizacoes_desktop.ui.Navigator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

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
		if (authService.restoreSession()) {
			navigator.showHome();
		}
		else {
			navigator.showLogin();
		}
	}

	@Override
	public void stop() {
		if (context != null) {
			context.close();
		}
		Platform.exit();
	}

}
