package com.dsl.jfx_live_rendering.engine.io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import com.dsl.jfx_live_rendering.engine.impl.ExceptionHandlerImpl;
import com.dsl.jfx_live_rendering.session_manager.Session;
import com.dsl.jfx_live_rendering.session_manager.SessionManager;

public class SessionRWOperations {

	private final Path sessionsDir;
	private final String serialFileExt;

	public SessionRWOperations() {
		this.sessionsDir = SessionManager.getInstance().getSessionsDir();
		this.serialFileExt = SessionManager.getInstance().getSerialFileExt();

		try {
			Files.createDirectories(sessionsDir);
		} catch (IOException e) {
			ExceptionHandlerImpl.logException(e);
		}
	}

	public Session read(Path path) throws IOException, ClassNotFoundException {
		try(ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
			return Session.class.cast(ois.readObject());
		}
	}

	public void write(Session session) throws IOException {
		Path sessionFile = sessionsDir.resolve(session.getApplicationModuleName() + serialFileExt);
		Files.deleteIfExists(sessionFile);
		try(ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(sessionFile))) {
			oos.writeObject(session);
		}
	}

	public List<Session> loadSerializedSessions() throws IOException {
		try(Stream<Path> files = Files.list(sessionsDir)) {
			return files.<Session>mapMulti((path, consumer) -> {
				try {
					consumer.accept(this.read(path));
				} catch (ClassNotFoundException | IOException _) {}
			}).toList();
		}
	}
}
