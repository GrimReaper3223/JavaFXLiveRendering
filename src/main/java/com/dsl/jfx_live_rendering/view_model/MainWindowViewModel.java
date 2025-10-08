package com.dsl.jfx_live_rendering.view_model;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dsl.jfx_live_rendering.engine.concurrent.ClassPathLoader;
import com.dsl.jfx_live_rendering.gui.ContentTab;
import com.dsl.jfx_live_rendering.gui.FXUtils;
import com.dsl.jfx_live_rendering.session_manager.Session;
import com.dsl.jfx_live_rendering.session_manager.SessionManager;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.scene.control.Tab;

public final class MainWindowViewModel implements ServiceConverter<List<Path>> {

	private ObjectProperty<ObservableList<Path>> classPathFileList = new SimpleObjectProperty<>(FXCollections.observableArrayList());
	private StringProperty log = new SimpleStringProperty();
	private Map<String, ContentTab> contentTabMap = new HashMap<>();

	// background service to load classpath files
	private final Service<List<Path>> classPathLoaderService = toService(new ClassPathLoader());

//	private ObjectProperty<ObservableList<Path>> pomDependenciesPathList = new SimpleObjectProperty<>(FXCollections.observableArrayList());
//	private ObservableMap<WatchEvent.Kind<Path>, List<Path>> changedFiles = FXCollections.observableHashMap();

	// session instance
	private Session session = SessionManager.getInstance().getSession();

	public MainWindowViewModel() {
		classPathLoaderService.setOnSucceeded(_ -> {
			session.setClassPathList(classPathLoaderService.getValue());
			setClassPathFileList(session.getClassPathList());
		});
		classPathLoaderService.setOnFailed(_ -> setLog("Failed to load classpath files: " + classPathLoaderService.getException().getMessage()));
		setClassPathFileList(session.getClassPathList());
	}

	/*
	 * classFileList property methods
	 */
	public ObjectProperty<ObservableList<Path>> classPathFileListProperty() {
		return this.classPathFileList;
	}

	public ObservableList<Path> getClassPathFileList() {
		return this.classPathFileList.get();
	}

	public void setClassPathFileList(final List<Path> classPathFileList) {
		this.classPathFileList.get().setAll(classPathFileList);
	}

	/*
	 * pomDependenciesPathList property methods
	 */
//	public ObjectProperty<ObservableList<Path>> pomDependenciesPathListProperty() {
//		return this.pomDependenciesPathList;
//	}
//
//	public ObservableList<Path> getPomDependenciesPathList() {
//		return this.pomDependenciesPathList.get();
//	}
//
//	public void setPomDependenciesPathList(final List<Path> pomDependenciesPathList) {
//		this.pomDependenciesPathList.get().setAll(pomDependenciesPathList);
//	}

	/*
	 * tabMap methods
	 */
	public ContentTab getContentTabFromMap(String tabName) {
		return contentTabMap.get(tabName);
	}

	public <T extends Tab> void addContentTabToMap(final T tab) {
		contentTabMap.computeIfPresent(tab.getText(), (_, _) -> FXUtils.tabCast(tab));
	}

	public <T extends Tab> void removeContentTabFromMap(final T tab) {
		contentTabMap.remove(tab.getText());
	}

	/*
	 * log property methods
	 */
	public StringProperty logProperty() {
		return this.log;
	}

	public String getLog() {
		return this.log.get();
	}

	public void setLog(final String log) {
		this.log.set(log);
	}

	// internal data handler methods
	public void unloadClassPath() {
		session.setClassPath(null);
		session.getClassPathList().clear();
		getClassPathFileList().clear();
	}

	public Path getClassPath() {
		return session.getClassPath();
	}

	public void setClassPath(Path classPath) {
		session.setClassPath(classPath);
		classPathLoaderService.restart();
	}
}
