package com.dsl.jfx_live_rendering.gui;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.dsl.jfx_live_rendering.properties.generated.P;

public enum TabRenderingState {

	LIVE_RENDERING(P.Status.RENDERING_LIVE),
	PAUSED_RENDERING(P.Status.RENDERING_PAUSED),
	UNPAUSED_RENDERING(P.Status.RENDERING_UNPAUSED),
	ERROR_RENDERING(P.Status.RENDERING_ERROR);

	private final String stateDescription;

	TabRenderingState(String stateDescription) {
		this.stateDescription = stateDescription;
	}

	public String getStateDescription() {
		return stateDescription;
	}

	public String getLastUpdatedDescription() {
		return "%s %s".formatted(P.Status.LAST_UPDATED, LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
	}
}
