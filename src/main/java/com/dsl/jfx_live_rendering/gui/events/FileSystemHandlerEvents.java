package com.dsl.jfx_live_rendering.gui.events;

import javafx.event.Event;
import javafx.event.EventType;

public class FileSystemHandlerEvents extends Event {

	private static final long serialVersionUID = 1L;

	public static final EventType<FileSystemHandlerEvents> ANY = new EventType<>(Event.ANY, "ANY_FILE_SYSTEM_HANDLER_EVENT");
	public static final EventType<FileSystemHandlerEvents> CLASSPATH_LOADED_EVENT = new EventType<>(ANY, "CLASSPATH_LOADED_EVENT");
	public static final EventType<FileSystemHandlerEvents> CLASSPATH_UNLOAD_REQUEST_EVENT = new EventType<>(ANY, "CLASSPATH_UNLOADED_EVENT");
	public static final EventType<FileSystemHandlerEvents> POM_XML_LOADED_EVENT = new EventType<>(ANY, "POM_XML_LOADED_EVENT");
	public static final EventType<FileSystemHandlerEvents> POM_XML_UNLOADED_EVENT = new EventType<>(ANY, "POM_XML_UNLOADED_EVENT");
	public static final EventType<FileSystemHandlerEvents> INIT_REQUEST_EVENT = new EventType<>(ANY, "INIT_REQUEST_EVENT");

	public FileSystemHandlerEvents(EventType<FileSystemHandlerEvents> eventType) {
		super(eventType);
	}
}
