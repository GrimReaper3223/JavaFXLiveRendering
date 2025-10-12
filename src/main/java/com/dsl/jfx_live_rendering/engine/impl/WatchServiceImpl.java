package com.dsl.jfx_live_rendering.engine.impl;

import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.dsl.jfx_live_rendering.engine.Context;
import com.dsl.jfx_live_rendering.session_manager.SessionManager;

public class WatchServiceImpl {

	private WatchService watcher;
	private Map<WatchKey, Path> keyMap;

	public WatchServiceImpl() {
		try {
			this.watcher = FileSystems.getDefault().newWatchService();
			this.keyMap = new HashMap<>();
		} catch (IOException e) {
			ExceptionHandlerImpl.logException(e);
		}
	}

	public void registerDirsAndStartWatch() {
		register();
		Thread wsiThread = new Thread(this::processEvents);
		wsiThread.setDaemon(true);
		wsiThread.setName("WatchServiceImpl-Thread");
		wsiThread.start();
	}

	@SuppressWarnings("unchecked")
	private static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	private void register() {
		SessionManager.getInstance().getSession().getJavaFXClassList().forEach(filePath -> {
			try {
				Path dir = Files.isDirectory(filePath) ? filePath : filePath.getParent();
				WatchKey key = dir.register(watcher, ENTRY_DELETE, ENTRY_MODIFY);

				if (keyMap.computeIfPresent(key, (_, _) -> dir) != null) {
					LoggerImpl.log(String.format("Directory '%s' has updated successfully.", dir.getFileName().toString()));
				} else if (keyMap.computeIfAbsent(key, _ -> dir) != null) {
					LoggerImpl.log(String.format("Directory '%s' has registered successfully.", dir.getFileName().toString()));
				} else {
					LoggerImpl.log("** BUG ON DIRECTORY REGISTER (Watch Service) **: No action performed.");
				}
			} catch (IOException e) {
				ExceptionHandlerImpl.logException(e);
			}
		});
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
					Context.offerChangedPathEntries(key.pollEvents()
							.stream()
							.filter(evt -> evt.kind() != OVERFLOW)
							.map(evt -> {
								WatchEvent<Path> event = cast(evt);
								Kind<Path> kind = event.kind();
								Path context = dirPath.resolve(event.context());

								if (context.toString().endsWith(".class")) {
									LoggerImpl.log(String.format("%s: %s", kind, context.getFileName().toString()));
									return Map.entry(kind, context);
								}
								return null;
							})
							.filter(Objects::nonNull)
							.collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList()))));

					if (!key.reset()) {
						keyMap.remove(key);
					}
				}
			} catch (InterruptedException e) {
				ExceptionHandlerImpl.logException(e);
			}
		}
	}
}
