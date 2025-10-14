package com.dsl.jfx_live_rendering.session_manager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.dsl.jfx_live_rendering.engine.impl.ExceptionHandlerImpl;
import com.dsl.jfx_live_rendering.engine.io.SessionRWOperations;

public class SessionManager {

	private static final SessionManager INSTANCE = new SessionManager();
	private static final SessionRWOperations SRWO = new SessionRWOperations();

	private final Path sessionsDir = Path.of("sessions");
	private final String serialFileExt = ".ser";

	private Session currentLoadedSession;

	private SessionManager() {}

	public static SessionManager getInstance() {
		return INSTANCE;
	}

	public void loadSession(Session session) {
		currentLoadedSession = session;
	}

	public void saveActiveSession() {
		try {
			SRWO.write(currentLoadedSession);
		} catch (IOException e) {
			ExceptionHandlerImpl.logException(e);
		}
	}

	public void createNewSession(Path classPath, Path pomXMLPath) {
		currentLoadedSession = new Session(classPath, pomXMLPath);
	}

	public Session getSession() {
		return currentLoadedSession;
	}

	public Path getSessionsDir() {
		return sessionsDir;
	}

	public String getSerialFileExt() {
		return serialFileExt;
	}

	public List<Session> getLoadedSessions() throws IOException {
		return SRWO.loadSerializedSessions();
	}
}
