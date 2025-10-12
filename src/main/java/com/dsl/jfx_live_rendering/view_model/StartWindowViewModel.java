package com.dsl.jfx_live_rendering.view_model;

import java.nio.file.Path;
import java.util.List;

import com.dsl.jfx_live_rendering.engine.Context;
import com.dsl.jfx_live_rendering.engine.concurrent.ClassPathLoader;
import com.dsl.jfx_live_rendering.engine.concurrent.PomDependencyResolver;
import com.dsl.jfx_live_rendering.engine.impl.ExceptionHandlerImpl;
import com.dsl.jfx_live_rendering.session_manager.Session;
import com.dsl.jfx_live_rendering.session_manager.SessionManager;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;

public final class StartWindowViewModel implements ServiceConverter<List<Path>> {

	private ObjectProperty<Path> classPath = new SimpleObjectProperty<>(Path.of(""));
	private ObjectProperty<Path> pomXMLPath = new SimpleObjectProperty<>(Path.of(""));
//	private StringProperty classPathCheckingStatus = new SimpleStringProperty();
//	private StringProperty pomXMLCheckingStatus = new SimpleStringProperty();

	private Session session = SessionManager.getInstance().getSession();

	private Service<List<Path>> classPathLoaderService = toService(new ClassPathLoader());
	private Service<List<Path>> pomDependenciesLoaderService = toService(new PomDependencyResolver());

	public StartWindowViewModel() {
		Context.registerService(ClassPathLoader.class, classPathLoaderService);
		Context.registerService(PomDependencyResolver.class, pomDependenciesLoaderService);
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

	public void setClassPath(final Path classPath) {
		this.classPathProperty().set(classPath);
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

	public void setPomXMLPath(final Path pomXMLPath) {
		this.pomXMLPathProperty().set(pomXMLPath);
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
	 * get services
	 */
	public Service<List<Path>> getClassPathloaderService() {
		return this.classPathLoaderService;
	}

	public Service<List<Path>> getPomModuleDependencyLoaderService() {
		return this.pomDependenciesLoaderService;
	}

	/*
	 * init method
	 */
	public void init() {
		session.setClassPath(getClassPath());
		session.setPomXMLPath(getPomXMLPath());

		// set service event handlers
		classPathLoaderService.setOnSucceeded(_ -> session.setJavaFXClassList(classPathLoaderService.getValue()));
		classPathLoaderService.setOnFailed(wst -> ExceptionHandlerImpl.logException(wst.getSource().getException()));
		pomDependenciesLoaderService.setOnSucceeded(_ -> session.setPomDependenciesPathList(pomDependenciesLoaderService.getValue()));
		pomDependenciesLoaderService.setOnFailed(wst -> ExceptionHandlerImpl.logException(wst.getSource().getException()));

		// start services
		classPathLoaderService.start();
		pomDependenciesLoaderService.start();
	}
}
