package com.dsl.jfx_live_rendering.gui;

import javafx.scene.Node;

public interface BorderPaneConfigHelper extends FXUtils {

	default Node configTop() { return null; }
	default Node configRight() { return null; }
	default Node configBottom() { return null; }
	default Node configLeft() { return null; }
	default Node configCenter() { return null; }
}
