package com.dsl.jfx_live_rendering.engine;

import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.SynchronousQueue;

public class Context {

	private static final SynchronousQueue<Map.Entry<Kind<Path>, List<Path>>> syncChangedPathQueue = new SynchronousQueue<>();

	private Context() {}

	public static Entry<Kind<Path>, List<Path>> getChangedPathEntry() throws InterruptedException {
		return syncChangedPathQueue.take();
	}

	public static void offerChangedPathEntries(Map<Kind<Path>, List<Path>> map) {
		var iterator = map.entrySet().iterator();
		while(iterator.hasNext()) {
			syncChangedPathQueue.offer(iterator.next());
		}
	}
}
