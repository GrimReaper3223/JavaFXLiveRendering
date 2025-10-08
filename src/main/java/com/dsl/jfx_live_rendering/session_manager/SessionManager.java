package com.dsl.jfx_live_rendering.session_manager;

import java.nio.file.Path;

public class SessionManager {

	private static final SessionManager INSTANCE = new SessionManager();

	private Session currentLoadedSession;

	private SessionManager() {}

	public static SessionManager getInstance() {
		return INSTANCE;
	}

	public void loadSession(Path classPath, Path pomXMLPath) {
		currentLoadedSession = new Session(classPath, pomXMLPath);
	}

	public Session getSession() throws NullPointerException {
		if(currentLoadedSession != null) {
			return currentLoadedSession;
		}
		return (currentLoadedSession = new Session(null, null));
	}
}
