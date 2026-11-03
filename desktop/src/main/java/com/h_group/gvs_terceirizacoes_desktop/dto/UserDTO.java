package com.h_group.gvs_terceirizacoes_desktop.dto;

/**
 * Identificação recebida da API e usada na apresentação da sessão.
 * @param id identificador da conta
 * @param name nome apresentado
 * @param email e-mail da conta
 * @param role perfil informado pelo servidor; arquivo local não é autoridade de acesso
 */
public record UserDTO(String id, String name, String email, String role) {

	public boolean isAdministrator() {
		return "adm".equals(role);
	}
}
