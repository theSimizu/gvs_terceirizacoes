package com.h_group.gvs_terceirizacoes_desktop.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RegisterRequest(@JsonProperty("name") String name, String email,
		@JsonProperty("password") String password) {
}
