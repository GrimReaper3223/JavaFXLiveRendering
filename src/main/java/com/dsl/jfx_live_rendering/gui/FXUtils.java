package com.dsl.jfx_live_rendering.gui;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Predicate;

import com.dsl.jfx_live_rendering.properties.generated.P;

import module javafx.controls;

public interface FXUtils {

	final Predicate<Path> isValidDirectory = path -> path != null && !path.toString().isEmpty();

	Scene createScene();
	default void setupActions() {}

	default Border createBorder(Color color, CornerRadii cr) {
		return new Border(new BorderStroke(color == null ? Color.LIGHTGRAY : color, BorderStrokeStyle.SOLID,cr == null ? CornerRadii.EMPTY : cr, BorderWidths.DEFAULT));
	}

	default File onFileChooserRequest(Path classPathInitialDirectory) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(isValidDirectory.test(classPathInitialDirectory) ? classPathInitialDirectory.getParent().toFile() : Path.of(System.getProperty("user.home")).toFile());
		fileChooser.setTitle(P.GuiText.SET_POMPATH);
		return fileChooser.showOpenDialog(new Stage());
	}

	default File onDirectoryChooserRequest(Path pomXMLInitialDirectory) {
		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setInitialDirectory(isValidDirectory.test(pomXMLInitialDirectory) ? pomXMLInitialDirectory.getParent().toFile() : Path.of(System.getProperty("user.home")).toFile());
		dirChooser.setTitle(P.GuiText.SET_CLASSPATH);
		return dirChooser.showDialog(new Stage());
	}

	static ContentTab tabCast(Tab tab) {
		return ContentTab.class.cast(tab);
	}
}
