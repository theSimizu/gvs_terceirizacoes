package com.h_group.gvs_terceirizacoes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Inicializa o contexto Spring Boot da API HTTP e da persistência relacional. */
@SpringBootApplication
public class GvsTerceirizacoesApplication {

	public static void main(String[] args) {
		SpringApplication.run(GvsTerceirizacoesApplication.class, args);
	}

}
