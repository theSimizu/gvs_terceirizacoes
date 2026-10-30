package com.h_group.gvs_terceirizacoes_desktop.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RegisterRequest(@JsonProperty("nome") String name, String email,
		@JsonProperty("senha") String password) {
}
