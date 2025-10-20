package com.dsl.jfx_live_rendering.engine;

import javafx.concurrent.Service;

import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

public class Context {

	private static final SynchronousQueue<Map.Entry<Kind<Path>, List<Path>>> SYNC_CHANGED_PATH_QUEUE = new SynchronousQueue<>();
	private static final Map<Class<?>, Service<List<Path>>> SERVICE_MAP = new ConcurrentHashMap<>();

	private Context() {}

	public static Entry<Kind<Path>, List<Path>> getChangedPathEntry() throws InterruptedException {
		return SYNC_CHANGED_PATH_QUEUE.take();
	}

	public static void offerChangedPathEntries(Map<Kind<Path>, List<Path>> map) {
        for (Entry<Kind<Path>, List<Path>> kindListEntry : map.entrySet()) {
            SYNC_CHANGED_PATH_QUEUE.offer(kindListEntry);
        }
	}

	public static void registerService(Class<?> clazz, Service<List<Path>> service) {
		SERVICE_MAP.put(clazz, service);
	}

	public static Service<List<Path>> getService(Class<?> clazz) {
		return SERVICE_MAP.get(clazz);
	}
}
