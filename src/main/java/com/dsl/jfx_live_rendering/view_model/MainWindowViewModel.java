package com.dsl.jfx_live_rendering.view_model;

import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.dsl.jfx_live_rendering.engine.Context;
import com.dsl.jfx_live_rendering.engine.concurrent.ClassPathLoader;
import com.dsl.jfx_live_rendering.engine.impl.LoggerImpl;
import com.dsl.jfx_live_rendering.engine.impl.WatchServiceImpl;
import com.dsl.jfx_live_rendering.gui.ContentTab;
import com.dsl.jfx_live_rendering.gui.FXUtils;
import com.dsl.jfx_live_rendering.models.ProcessedPathModel;
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
	private ObjectProperty<Entry<Kind<Path>, List<Path>>> changedPathEntry = new SimpleObjectProperty<>();

	// session instance
	private Session session = SessionManager.getInstance().getSession();

	public MainWindowViewModel() {
		new WatchServiceImpl().registerDirsAndStartWatch();
		classPathLoaderService.setOnSucceeded(_ -> {
			session.setClassPathList(classPathLoaderService.getValue());
			setClassPathFileList(session.getClassPathList());
		});
		classPathLoaderService.setOnFailed(_ -> setLog("Failed to load classpath files: " + classPathLoaderService.getException().getMessage()));
		setClassPathFileList(session.getClassPathList());

		// thread to update log property
		Thread logConsumerThread = new Thread(() -> {
			do {
				log.set(LoggerImpl.getLog());
			} while(Thread.currentThread().isAlive());
		});

		// thread to update changedPathEntry property
		Thread changedPathConsumerThread = new Thread(() -> {
			do {
				try {
					setChangedPathEntry(Context.getChangedPathQueue());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while(Thread.currentThread().isAlive());
		});

		// setting up threads
		changedPathConsumerThread.setName("ChangedPathConsumer-Thread");
		logConsumerThread.setName("LogConsumer-Thread");
		changedPathConsumerThread.setDaemon(true);
		logConsumerThread.setDaemon(true);
		changedPathConsumerThread.start();
		logConsumerThread.start();
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
	 * changedPathEntry property methods
	 */
	public ObjectProperty<Entry<Kind<Path>, List<Path>>> changedPathEntryProperty() {
		return this.changedPathEntry;
	}

	public Entry<Kind<Path>, List<Path>> getChangedPathEntry() {
		return this.changedPathEntry.get();
	}

	public void setChangedPathEntry(final Entry<Kind<Path>, List<Path>> changedPathEntry) {
		this.changedPathEntry.set(changedPathEntry);
	}

	/*
	 * tabMap methods
	 */
	public ContentTab getContentTabFromMap(String tabName) {
		return contentTabMap.get(tabName);
	}

	public <T extends Tab> void addContentTabToMap(final List<T> tabList) {
		tabList.stream().forEach(tab -> {
			ContentTab ct = FXUtils.tabCast(tab);
			if(contentTabMap.computeIfPresent(ct.getText(), (_, _) -> ct) == null) {
				contentTabMap.put(ct.getText(), ct);
			}
		});
	}

	public <T extends Tab> void removeContentTabFromMap(final List<T> tabList) {
		tabList.stream().forEach(tab -> contentTabMap.remove(tab.getText()));
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

	public Entry<Kind<Path>, List<ContentTab>> transformEntries() {
		var entry = getChangedPathEntry();
		List<ContentTab> tabs = entry.getValue().stream().map(ProcessedPathModel::new).toList().stream()
				.map(ppm -> getContentTabFromMap(ppm.getFileName())).filter(Objects::nonNull)
				.toList();
		return Map.entry(entry.getKey(), tabs);
	}
}
