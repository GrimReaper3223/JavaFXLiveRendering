package com.dsl.jfx_live_rendering.view_model;

import module javafx.controls;
import com.dsl.jfx_live_rendering.engine.Context;
import com.dsl.jfx_live_rendering.engine.concurrent.ClassPathLoader;
import com.dsl.jfx_live_rendering.engine.concurrent.PomDependencyResolver;
import com.dsl.jfx_live_rendering.engine.impl.ExceptionHandlerImpl;
import com.dsl.jfx_live_rendering.engine.impl.LoggerImpl;
import com.dsl.jfx_live_rendering.engine.impl.WatchServiceImpl;
import com.dsl.jfx_live_rendering.gui.ContentTab;
import com.dsl.jfx_live_rendering.models.ProcessedPathModel;
import com.dsl.jfx_live_rendering.session_manager.Session;
import com.dsl.jfx_live_rendering.session_manager.SessionManager;

import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public final class MainWindowViewModel implements ServiceConverter<List<Path>> {

	private final Map<Path, ContentTab> contentTabMap = new HashMap<>();

	private final ObjectProperty<ObservableList<Path>> javaFXClassList = new SimpleObjectProperty<>(FXCollections.observableArrayList());
	private final StringProperty log = new SimpleStringProperty();

	private final ObjectProperty<Entry<Kind<Path>, List<Path>>> changedFileEntry = new SimpleObjectProperty<>();

    private final BooleanProperty tabSelected = new SimpleBooleanProperty();
    private final BooleanProperty alwaysShowLogPane = new SimpleBooleanProperty();

	private final Service<List<Path>> classPathLoaderService = Context.getService(ClassPathLoader.class);
	private final Service<List<Path>> pomDependenciesLoaderService = Context.getService(PomDependencyResolver.class);

	// session instance
	private final Session session = SessionManager.getInstance().getSession();

	public MainWindowViewModel() {
		new WatchServiceImpl().registerDirsAndStartWatch();
		setJavaFXClassListToProperty(session.getJavaFXClassList());

		// stacking event handlers without overriding previous ones
		classPathLoaderService.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, _ -> {
            session.setJavaFXClassList(classPathLoaderService.getValue());
            setJavaFXClassListToProperty(session.getJavaFXClassList());
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
					setChangedFileEntry(Context.getChangedPathEntry());
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
	public ObjectProperty<Entry<Kind<Path>, List<Path>>> changedFileEntryProperty() {
		return this.changedFileEntry;
	}

	public Entry<Kind<Path>, List<Path>> getChangedFileEntry() {
		return this.changedFileEntry.get();
	}

	public void setChangedFileEntry(final Entry<Kind<Path>, List<Path>> changedFileEntry) {
		this.changedFileEntry.set(changedFileEntry);
	}

	/*
	 * tabMap methods
	 */
	public ContentTab getContentTabFromMap(Path path) {
		return contentTabMap.get(path);
	}

	public <T extends Entry<? extends Path, ? extends Tab>> void addContentTabToMap(final List<T> tabEntryList) {
        tabEntryList.forEach(entry -> {
            Path path = entry.getKey();
			ContentTab ct = (ContentTab) entry.getValue();
			if(contentTabMap.computeIfPresent(path, (_, _) -> ct) == null) {
				contentTabMap.computeIfAbsent(path, _ -> ct);
			}
		});
	}

	public <T extends Path> void removeContentTabFromMap(final List<T> pathList) {
        pathList.forEach(contentTabMap::remove);
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
		var entry = getChangedFileEntry();
		List<ContentTab> tabs = entry.getValue().stream().map(ProcessedPathModel::new).toList().stream()
				.map(ppm -> getContentTabFromMap(ppm.getPath())).filter(Objects::nonNull)
				.toList();
		return Map.entry(entry.getKey(), tabs);
	}

    // tabSelected property
    public boolean isTabSelected() {
        return tabSelected.get();
    }

    public BooleanProperty tabSelectedProperty() {
        return tabSelected;
    }

    // alwaysShowLogPane property
    public boolean isAlwaysShowLogPane() {
        return alwaysShowLogPane.get();
    }

    public void setIfAlwaysShowLogPane(boolean value) {
        alwaysShowLogPane.set(value);
    }

    public BooleanProperty alwaysShowLogPaneProperty() {
        return alwaysShowLogPane;
    }
}
