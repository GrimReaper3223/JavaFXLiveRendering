package com.dsl.jfx_live_rendering.view_model;

import com.dsl.jfx_live_rendering.engine.Context;
import com.dsl.jfx_live_rendering.engine.concurrent.ClassPathLoader;
import com.dsl.jfx_live_rendering.engine.concurrent.PomDependencyResolver;
import com.dsl.jfx_live_rendering.engine.impl.ExceptionHandlerImpl;
import com.dsl.jfx_live_rendering.session_manager.Session;
import com.dsl.jfx_live_rendering.session_manager.SessionManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class StartWindowViewModel implements ServiceConverter<List<Path>> {

	private final ObjectProperty<Path> classPath = new SimpleObjectProperty<>(Path.of(""));
	private final ObjectProperty<Path> pomXMLPath = new SimpleObjectProperty<>(Path.of(""));
	private final ObjectProperty<ObservableList<Session>> sessionList = new SimpleObjectProperty<>(FXCollections.observableArrayList());
//	private StringProperty classPathCheckingStatus = new SimpleStringProperty();
//	private StringProperty pomXMLCheckingStatus = new SimpleStringProperty();

	private final Service<List<Path>> classPathLoaderService = toService(new ClassPathLoader());
	private final Service<List<Path>> pomDependenciesLoaderService = toService(new PomDependencyResolver());

	private final SessionManager sessionManager = SessionManager.getInstance();

	public StartWindowViewModel() {
		Context.registerService(ClassPathLoader.class, classPathLoaderService);
		Context.registerService(PomDependencyResolver.class, pomDependenciesLoaderService);

		// set service event handlers
		classPathLoaderService.setOnSucceeded(_ -> sessionManager.getSession().setJavaFXClassList(classPathLoaderService.getValue()));
		classPathLoaderService.setOnFailed(wst -> ExceptionHandlerImpl.logException(wst.getSource().getException()));
		pomDependenciesLoaderService.setOnSucceeded(_ -> sessionManager.getSession().setPomDependenciesPathList(pomDependenciesLoaderService.getValue()));
		pomDependenciesLoaderService.setOnFailed(wst -> ExceptionHandlerImpl.logException(wst.getSource().getException()));
	}

	/*
	 * classPath methods
	 */
	public ObjectProperty<Path> classPathProperty() {
		return this.classPath;
	}

	public Path getClassPath() {
		return this.classPathProperty().get();
	}

	public <T> void setClassPath(final T classPath) {
		this.classPathProperty().set(Path.of(classPath.toString()));
	}

	/*
	 * pomXMLPath methods
	 */
	public ObjectProperty<Path> pomXMLPathProperty() {
		return this.pomXMLPath;
	}

	public Path getPomXMLPath() {
		return this.pomXMLPathProperty().get();
	}

	public <T> void setPomXMLPath(final T pomXMLPath) {
		this.pomXMLPathProperty().set(Path.of(pomXMLPath.toString()));
	}

	/*
	 * sessionList methods
	 */
	public ObjectProperty<ObservableList<Session>> sessionListProperty() {
		return this.sessionList;
	}

	public List<Session> getSessionList() {
		return this.sessionList.get();
	}

	public void loadSessions() {
		this.sessionList.get().setAll(sessionManager.getLoadedSessions());
	}

	/*
	 * classPathCheckingStatus methods
	 */
//	public StringProperty classPathCheckingStatusProperty() {
//		return this.classPathCheckingStatus;
//	}
//
//	public String getClassPathCheckingStatus() {
//		return this.classPathCheckingStatusProperty().get();
//	}
//
//	public void setClassPathCheckingStatus(final String classPathCheckingStatus) {
//		this.classPathCheckingStatusProperty().set(classPathCheckingStatus);
//	}

	/*
	 * pomXMLCheckingStatus methods
	 */
//	public StringProperty pomXMLCheckingStatusProperty() {
//		return this.pomXMLCheckingStatus;
//	}
//
//	public String getPomXMLCheckingStatus() {
//		return this.pomXMLCheckingStatusProperty().get();
//	}
//
//	public void setPomXMLCheckingStatus(final String pomXMLCheckingStatus) {
//		this.pomXMLCheckingStatusProperty().set(pomXMLCheckingStatus);
//	}

	/*
	 * init method
	 */
	public void init(Session session) {
		Optional.ofNullable(session).ifPresentOrElse(sessionManager::loadSession,
				() -> sessionManager.createNewSession(getClassPath(), getPomXMLPath()));
		classPathLoaderService.start();
		pomDependenciesLoaderService.start();
	}

	public void defineSessionModuleFinders() {
		sessionManager.getSession().defineSessionModuleFinders();
		sessionManager.saveActiveSession();
	}

	public void deleteSessionFile(final Session session) {
		sessionManager.deleteSessionFile(session);
        sessionList.get().remove(session);
	}

	public void loadClassPath(final File dir) {
		buildPathLoader(dir, true);
	}

	public void loadPomXMLPath(final File file) {
		buildPathLoader(file, false);
	}

	private void buildPathLoader(File file, boolean isClassPathLoading) {
		Optional.ofNullable(file)
        		.ifPresent(response -> {
        			Path path = response.toPath();
        			if(isClassPathLoading) {
        				setClassPath(path);
        			} else {
        				setPomXMLPath(path);
        			}
        		});
	}
}
