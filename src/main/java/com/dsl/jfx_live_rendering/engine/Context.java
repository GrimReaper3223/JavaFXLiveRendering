package com.dsl.jfx_live_rendering.engine;

import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Context {

	private static final BlockingQueue<Map.Entry<Kind<Path>, List<Path>>> changedPathQueue = new ArrayBlockingQueue<>(128);

	private Context() {}

	public static Entry<Kind<Path>, List<Path>> getChangedPathQueue() throws InterruptedException {
		return changedPathQueue.take();
	}

	public static void putChangedPathQueue(Map<Kind<Path>, List<Path>> map) {
		var iterator = map.entrySet().iterator();
		while(iterator.hasNext()) {
			changedPathQueue.offer(iterator.next());
		}
	}
}
