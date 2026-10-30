package com.h_group.gvs_terceirizacoes_desktop.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponse(String token, @JsonProperty("usuario") UserDTO user) {
}
