package com.dsl.jfx_live_rendering.gui;

import java.io.File;
import java.nio.file.Path;

import com.dsl.jfx_live_rendering.properties.generated.P;

import module javafx.controls;

public interface FXUtils {

	Scene createScene();
	default void setupActions() {}

	default Border createBorder(Color color, CornerRadii cr) {
		return new Border(new BorderStroke(color == null ? Color.LIGHTGRAY : color, BorderStrokeStyle.SOLID,cr == null ? CornerRadii.EMPTY : cr, BorderWidths.DEFAULT));
	}

	default File onFileChooserRequest(Path pomXMLPath) {
		FileChooser fileChooser = new FileChooser();
		if(pomXMLPath != null) {
			fileChooser.setInitialDirectory(pomXMLPath.toString().isEmpty() ? pomXMLPath.toFile() : Path.of(System.getProperty("user.home")).toFile());
		}
		fileChooser.setTitle(P.GuiText.SET_POMPATH);
		return fileChooser.showOpenDialog(new Stage());
	}

	default File onDirectoryChooserRequest(Path classPath) {
		DirectoryChooser dirChooser = new DirectoryChooser();
		if(classPath != null) {
			dirChooser.setInitialDirectory(!classPath.toString().isEmpty() ? classPath.getParent().toFile() : Path.of(System.getProperty("user.home")).toFile());
		}
		dirChooser.setTitle(P.GuiText.SET_CLASSPATH);
		return dirChooser.showDialog(new Stage());
	}

	static ContentTab tabCast(Tab tab) {
		return ContentTab.class.cast(tab);
	}
}
