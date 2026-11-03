package com.h_group.gvs_terceirizacoes_desktop.session;

import java.util.Optional;

import com.h_group.gvs_terceirizacoes_desktop.dto.StoredSession;

/**
 * Contrato de persistência local da sessão usado pelo serviço de autenticação.
 * A abstração permite testar o serviço sem gravar o arquivo pessoal de sessão.
 */
public interface TokenStore {

	/**
	 * Obtém a sessão persistida, quando disponível e legível.
	 * @return sessão local ou resultado vazio
	 */
	Optional<StoredSession> load();

	/**
	 * Persiste uma sessão no armazenamento da implementação.
	 * @param session sessão que será armazenada
	 */
	void save(StoredSession session);

	/**
	 * Remove a sessão do armazenamento local.
	 */
	void clear();

}
