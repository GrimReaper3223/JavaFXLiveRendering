package com.dsl.jfx_live_rendering.view_model;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.dsl.jfx_live_rendering.engine.Context;
import com.dsl.jfx_live_rendering.engine.concurrent.ClassPathLoader;
import com.dsl.jfx_live_rendering.engine.concurrent.PomDependencyResolver;
import com.dsl.jfx_live_rendering.engine.impl.ExceptionHandlerImpl;
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
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;

public final class MainWindowViewModel implements ServiceConverter<List<Path>>, FXUtils {

	private Map<String, ContentTab> contentTabMap = new HashMap<>();

	private ObjectProperty<ObservableList<Path>> javaFXClassList = new SimpleObjectProperty<>(FXCollections.observableArrayList());
	private StringProperty log = new SimpleStringProperty();

	private ObjectProperty<Entry<Kind<Path>, List<Path>>> changedPathEntry = new SimpleObjectProperty<>();

	Service<List<Path>> classPathLoaderService = Context.getService(ClassPathLoader.class);
	Service<List<Path>> pomDependenciesLoaderService = Context.getService(PomDependencyResolver.class);

	// session instance
	private Session session = SessionManager.getInstance().getSession();

	public MainWindowViewModel() {
		new WatchServiceImpl().registerDirsAndStartWatch();
		setJavaFXClassListToProperty(session.getJavaFXClassList());

		// stacking event handlers without overriding previous ones
		classPathLoaderService.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, _ -> setJavaFXClassListToProperty(session.getJavaFXClassList()));
		classPathLoaderService.addEventHandler(WorkerStateEvent.WORKER_STATE_RUNNING, _ -> {
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Classpath Reloading Detected");
			alert.setHeaderText("Classloader reloading detected. \nIt is recommended to select the pom.xml file corresponding to the new classloader. \n\nDo you want to select pom.xml again?");
			alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
			if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
				File file = onFileChooserRequest(getClassPath());
				if(file != null) {
					setPomXMLPath(file.toPath());
				}
			}
		});

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
					setChangedPathEntry(Context.getChangedPathEntry());
				} catch (InterruptedException e) {
					ExceptionHandlerImpl.logException(e);
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
	public ObjectProperty<ObservableList<Path>> javaFXClassListProperty() {
		return this.javaFXClassList;
	}

	public ObservableList<Path> getJavaFXClassListFromProperty() {
		return this.javaFXClassList.get();
	}

	public void setJavaFXClassListToProperty(final List<Path> javaFXClassList) {
		this.javaFXClassList.get().setAll(javaFXClassList);
	}

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
		session.getJavaFXClassList().clear();
		getJavaFXClassListFromProperty().clear();
	}

	// get/set classPath
	public Path getClassPath() {
		return session.getClassPath();
	}

	public void setClassPath(Path classPath) {
		session.setClassPath(classPath);
		classPathLoaderService.restart();
	}

	// get/set pom.xml path
	public Path getPomXMLPath() {
		return session.getPomXMLPath();
	}

	public void setPomXMLPath(Path pomXMLPath) {
		session.setPomXMLPath(pomXMLPath);
		pomDependenciesLoaderService.restart();
	}

	public Entry<Kind<Path>, List<ContentTab>> transformEntries() {
		var entry = getChangedPathEntry();
		List<ContentTab> tabs = entry.getValue().stream().map(ProcessedPathModel::new).toList().stream()
				.map(ppm -> getContentTabFromMap(ppm.getFileName())).filter(Objects::nonNull)
				.toList();
		return Map.entry(entry.getKey(), tabs);
	}

	@Override
	public Scene createScene() {
		return null;
	}
}
