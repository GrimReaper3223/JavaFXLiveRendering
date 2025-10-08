package com.dsl.jfx_live_rendering.engine.impl;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class LoggerImpl {

	private static final BlockingQueue<String> LOG_BUFFER = new ArrayBlockingQueue<>(1024);

	private LoggerImpl() {}

	public static void log(String log) {
		LOG_BUFFER.offer(log);
	}

	public static String getLog() {
		String log = "";
		try {
			log = LOG_BUFFER.take() + '\n';
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return log;
	}
}
