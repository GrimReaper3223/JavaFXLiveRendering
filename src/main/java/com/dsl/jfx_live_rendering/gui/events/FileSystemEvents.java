package com.dsl.jfx_live_rendering.gui.events;

import javafx.event.Event;
import javafx.event.EventType;

import java.io.Serial;

public class FileSystemEvents extends Event {

	@Serial
    private static final long serialVersionUID = 1L;

	public static final EventType<FileSystemEvents> ANY = new EventType<>(Event.ANY, "ANY_FILE_SYSTEM_EVENT");
	public static final EventType<FileSystemEvents> CLASSPATH_LOAD_REQUEST_EVENT = new EventType<>(ANY, "CLASSPATH_LOAD_REQUEST_EVENT");
	public static final EventType<FileSystemEvents> CLASSPATH_UNLOAD_REQUEST_EVENT = new EventType<>(ANY, "CLASSPATH_UNLOAD_REQUEST_EVENT");
	public static final EventType<FileSystemEvents> POM_XML_LOAD_REQUEST_EVENT = new EventType<>(ANY, "POM_XML_LOAD_REQUEST_EVENT");
	public static final EventType<FileSystemEvents> INIT_PATH_PROCESS_EVENT = new EventType<>(ANY, "INIT_PATH_PROCESS_EVENT");
	public static final EventType<FileSystemEvents> PATH_PROCESS_COMPLETED_EVENT = new EventType<>(ANY, "PATH_PROCESS_COMPLETED_EVENT");

	public FileSystemEvents(EventType<FileSystemEvents> eventType) {
		super(eventType);
	}
}
