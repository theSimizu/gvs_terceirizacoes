package com.h_group.gvs_terceirizacoes_desktop.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserDTO(String id, @JsonProperty("nome") String name, String email) {
}
