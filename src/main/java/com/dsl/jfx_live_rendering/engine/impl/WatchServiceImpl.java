package com.dsl.jfx_live_rendering.engine.impl;

import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;

import com.dsl.jfx_live_rendering.models.ProcessedPathModel;
import com.dsl.jfx_live_rendering.session_manager.SessionManager;

import javafx.scene.Node;

public class WatchServiceImpl {

	private final CustomClassLoader cl = new CustomClassLoader();
	private WatchService watcher;
	private Map<WatchKey, Path> keyMap;

	public WatchServiceImpl() {
		try {
			this.watcher = FileSystems.getDefault().newWatchService();
			this.keyMap = new HashMap<>();
			register();
			new Thread(this::processEvents).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	public void register() {
		SessionManager.getInstance().getSession().getClassPathList().forEach(filePath -> {
			try {
				if(classNodeValidator(filePath)) {
    				Path dir = Files.isDirectory(filePath) ? filePath : filePath.getParent();
    				WatchKey key = dir.register(watcher, ENTRY_DELETE, ENTRY_MODIFY);

    				if (keyMap.computeIfPresent(key, (_, _) -> dir) != null) {
    					LoggerImpl.log(String.format("Directory '%s' has updated successfully.", dir.getFileName().toString()));
    				} else if (keyMap.computeIfAbsent(key, _ -> dir) != null) {
    					LoggerImpl.log(String.format("Directory '%s' has registered successfully.", dir.getFileName().toString()));
    				} else {
    					LoggerImpl.log("** BUG ON DIRECTORY REGISTER (Watch Service) **: No action performed.");
    				}
				}
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		});
	}

	private boolean classNodeValidator(Path filePath) throws ClassNotFoundException {
		boolean result = false;
		String stringFile = filePath.toString();
		if(stringFile.endsWith(".class") && !stringFile.contains("module-info")) {
			Class<?> cls = cl.loadClass(new ProcessedPathModel(filePath).getBinaryFileName());
			result = Node.class.isAssignableFrom(cls);
		}
		return result;
	}

	private void processEvents() {
		while (!keyMap.isEmpty()) {
			WatchKey key;
			try {
				key = watcher.take();

				Path dirPath = keyMap.get(key);

				if (dirPath == null) {
					LoggerImpl.log("WatchKey has an unrecognized path.");
				} else {
					var list = key.pollEvents().stream().filter(evt -> evt.kind() != OVERFLOW).map(evt -> {
						WatchEvent<Path> event = cast(evt);
						return Map.entry(event.kind(), event.context());
					}).filter(entry -> entry.getValue().toString().contains(".class")).map(entry -> {
						LoggerImpl.log(String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
						return entry;
					}).toList();

//					StaticContext.setChangedPaths(list);

					if (!key.reset()) {
						keyMap.remove(key);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
