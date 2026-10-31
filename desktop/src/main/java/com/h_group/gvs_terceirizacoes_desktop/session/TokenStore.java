package com.h_group.gvs_terceirizacoes_desktop.session;

import java.util.Optional;

public interface TokenStore {

	Optional<StoredSession> load();

	void save(StoredSession session);

	void clear();

}
