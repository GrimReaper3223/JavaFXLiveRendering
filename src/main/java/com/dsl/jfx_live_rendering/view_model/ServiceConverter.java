package com.dsl.jfx_live_rendering.view_model;

import java.util.concurrent.Callable;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public interface ServiceConverter<T> {

	default Service<T> toService(Callable<T> taskInstance) {
		return new Service<>() {
			@Override
			protected Task<T> createTask() {
				return new Task<>() {
					@Override
					protected T call() throws Exception {
						return taskInstance.call();
					}
				};
			}
		};
	}
}
