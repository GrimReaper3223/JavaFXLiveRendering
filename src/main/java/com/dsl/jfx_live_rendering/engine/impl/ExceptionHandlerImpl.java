package com.dsl.jfx_live_rendering.engine.impl;

import java.util.Arrays;

public class ExceptionHandlerImpl {

	private ExceptionHandlerImpl() {}

	private static String getStackTrace(Throwable throwable) {
		StringBuilder sb = new StringBuilder();
		sb.append(throwable.toString()).append('\n');
		Arrays.stream(throwable.getStackTrace()).forEach(element -> sb.append("\tat ").append(element).append('\n'));
		Throwable cause = throwable.getCause();
		if (cause != null) {
			sb.append("Caused by: ").append(getStackTrace(cause));
		}
		return sb.toString();
	}

	public static void logException(Throwable throwable) {
		if(throwable instanceof InterruptedException) {
			Thread.currentThread().interrupt();
		}
		String trace = getStackTrace(throwable);
		LoggerImpl.log(trace);
		IO.println(trace);	// NOTE: remover ao fim da depuracao
	}
}
