package com.h_group.gvs_terceirizacoes_desktop.session;

import com.h_group.gvs_terceirizacoes_desktop.config.SessionProperties;
import com.h_group.gvs_terceirizacoes_desktop.dto.StoredSession;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class FileTokenStore implements TokenStore {
	private static final Logger log = LoggerFactory.getLogger(FileTokenStore.class);
	private static final Set<PosixFilePermission> OWNER_ONLY = PosixFilePermissions.fromString("rw-------");

	private final SessionProperties properties;
	private final ObjectMapper objectMapper;

	@Override
	public Optional<StoredSession> load() {
		Path file = properties.getFile();
		if (file == null || !Files.isRegularFile(file)) {
			return Optional.empty();
		}
		try {
			String content = Files.readString(file);
			StoredSession session = objectMapper.readValue(content, StoredSession.class);
			if (session == null || session.token() == null || session.token().isBlank()) {
				return Optional.empty();
			}
			return Optional.of(session);
		} catch (Exception exception) {
			log.warn("Could not read the stored session; ignoring it: {}", exception.getMessage());
			return Optional.empty();
		}
	}

	@Override
	public void save(StoredSession session) {
		Path file = properties.getFile();
		try {
			Path parent = file.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			String content = objectMapper.writeValueAsString(session);
			Files.writeString(file, content);
			restrictPermissions(file);
		} catch (IOException exception) {
			throw new UncheckedIOException("Could not store the session.", exception);
		}
	}

	@Override
	public void clear() {
		Path file = properties.getFile();
		try {
			Files.deleteIfExists(file);
		} catch (IOException exception) {
			log.warn("Could not delete the stored session.", exception);
		}
	}

	private void restrictPermissions(Path file) {
		try {
			if (!Files.getFileStore(file).supportsFileAttributeView("posix")) {
				return;
			}
			Files.setPosixFilePermissions(file, OWNER_ONLY);
		} catch (IOException exception) {
			log.warn("Could not restrict the stored session permissions.", exception);
		}
	}

}
