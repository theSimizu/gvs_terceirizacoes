package com.h_group.gvs_terceirizacoes_desktop.session;

import com.h_group.gvs_terceirizacoes_desktop.dto.UserDTO;

public record StoredSession(String token, UserDTO user) {
}
