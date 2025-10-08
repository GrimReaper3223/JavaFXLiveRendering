package com.dsl.jfx_live_rendering.gui.events;

import javafx.event.Event;
import javafx.event.EventType;

public class RenderingEvents extends Event {

	private static final long serialVersionUID = 1L;

	public static final EventType<RenderingEvents> ANY = new EventType<>(Event.ANY, "ANY_RENDERING_EVENT");
	public static final EventType<RenderingEvents> LIVE_RENDERING_EVENT = new EventType<>(Event.ANY, "LIVE_RENDERING_EVENT");
	public static final EventType<RenderingEvents> RENDERING_PAUSED_EVENT = new EventType<>(Event.ANY, "RENDERING_PAUSED_EVENT");
	public static final EventType<RenderingEvents> RENDERING_UNPAUSED_EVENT = new EventType<>(Event.ANY, "RENDERING_UNPAUSED_EVENT");
	public static final EventType<RenderingEvents> RENDERING_ERROR_EVENT = new EventType<>(Event.ANY, "RENDERING_ERROR_EVENT");

	public RenderingEvents(EventType<? extends RenderingEvents> eventType) {
		super(eventType);
	}
}
