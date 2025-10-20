package com.dsl.jfx_live_rendering.gui.events;

import com.dsl.jfx_live_rendering.gui.ContentTab;
import javafx.event.Event;
import javafx.event.EventType;

import java.io.Serial;

public class RenderingEvents extends Event {

	@Serial
    private static final long serialVersionUID = 1L;

	public static final EventType<RenderingEvents> ANY = new EventType<>(Event.ANY, "ANY_RENDERING_EVENT");
	public static final EventType<RenderingEvents> START_RENDERING_REQUEST_EVENT = new EventType<>(ANY, "START_RENDERING_REQUEST_EVENT");
	public static final EventType<RenderingEvents> LIVE_RENDERING_EVENT = new EventType<>(ANY, "LIVE_RENDERING_EVENT");
	public static final EventType<RenderingEvents> RENDERING_PAUSED_EVENT = new EventType<>(ANY, "RENDERING_PAUSED_EVENT");
	public static final EventType<RenderingEvents> RENDERING_UNPAUSED_EVENT = new EventType<>(ANY, "RENDERING_UNPAUSED_EVENT");
	public static final EventType<RenderingEvents> RENDERING_FORCED_EVENT = new EventType<>(ANY, "RENDERING_FORCED_EVENT");
	public static final EventType<RenderingEvents> RENDERING_ERROR_EVENT = new EventType<>(ANY, "RENDERING_ERROR_EVENT");

    private ContentTab ct;

	public RenderingEvents(EventType<? extends RenderingEvents> eventType, ContentTab ct) {
        super(eventType);
        this.ct = ct;
	}

    public ContentTab getContentTab() {
        return ct;
    }

    public void setContentTab(ContentTab ct) {
        this.ct = ct;
    }
}
