package com.h_group.gvs_terceirizacoes_desktop;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@ConfigurationPropertiesScan
public class GvsTerceirizacoesDesktopApplication {

	public static void main(String[] args) {
		Application.launch(DesktopFxApplication.class, args);
	}

}
