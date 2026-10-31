package com.h_group.gvs_terceirizacoes_desktop.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.h_group.gvs_terceirizacoes_desktop.config.SessionProperties;
import com.h_group.gvs_terceirizacoes_desktop.dto.UserDTO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

class FileTokenStoreTest {

	@TempDir
	Path tempDir;

	@Test
	void savesAndLoadsSession() {
		FileTokenStore store = store();

		store.save(new StoredSession("token-value", new UserDTO("id", "Name", "mail@example.com")));

		Optional<StoredSession> loaded = store.load();
		assertTrue(loaded.isPresent());
		assertEquals("token-value", loaded.get().token());
		assertEquals("Name", loaded.get().user().name());
	}

	@Test
	void clearsSession() {
		FileTokenStore store = store();

		store.save(new StoredSession("token-value", new UserDTO("id", "Name", "mail@example.com")));
		store.clear();

		assertTrue(store.load().isEmpty());
	}

	@Test
	void ignoresCorruptFile() throws Exception {
		Path file = tempDir.resolve("session.json");
		Files.writeString(file, "{not-json");

		assertTrue(store().load().isEmpty());
	}

	private FileTokenStore store() {
		SessionProperties properties = new SessionProperties();
		properties.setFile(tempDir.resolve("session.json"));
		return new FileTokenStore(properties, JsonMapper.builder().build());
	}

}
