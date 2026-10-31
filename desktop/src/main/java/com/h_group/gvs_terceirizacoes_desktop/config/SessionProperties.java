package com.h_group.gvs_terceirizacoes_desktop.config;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gvs.session")
public class SessionProperties {

	private Path file = Path.of(System.getProperty("user.home"), ".gvs-terceirizacoes", "session.json");

	public Path getFile() {
		return file;
	}

	public void setFile(Path file) {
		this.file = file;
	}

}
