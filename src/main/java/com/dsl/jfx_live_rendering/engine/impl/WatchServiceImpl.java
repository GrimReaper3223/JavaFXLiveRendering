package com.dsl.jfx_live_rendering.engine.impl;

import com.dsl.jfx_live_rendering.engine.Context;
import com.dsl.jfx_live_rendering.session_manager.SessionManager;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.nio.file.StandardWatchEventKinds.*;

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

	// TODO: verificar se podemos inserir o evento ENTRY_CREATE
	private void register() {
		SessionManager.getInstance().getSession().getJavaFXClassList().forEach(filePath -> {
			try {
				Path dir = Files.isDirectory(filePath) ? filePath : filePath.getParent();
				WatchKey key = dir.register(watcher, ENTRY_DELETE, ENTRY_MODIFY);

				if (keyMap.computeIfPresent(key, (_, _) -> dir) != null) {
					LoggerImpl.log(String.format("Directory '%s' has updated successfully.", dir.getFileName().toString()));
				} else {
                    keyMap.computeIfAbsent(key, _ -> dir);
                    LoggerImpl.log(String.format("Directory '%s' has registered successfully.", dir.getFileName().toString()));
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
