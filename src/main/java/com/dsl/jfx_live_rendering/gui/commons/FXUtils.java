package com.dsl.jfx_live_rendering.gui.commons;

import module javafx.controls;
import com.dsl.jfx_live_rendering.properties.generated.P;
import javafx.stage.FileChooser.ExtensionFilter;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Predicate;

public interface FXUtils {

	final Predicate<Path> isValidDirectory = path -> path != null && !path.toString().isEmpty();

	default Scene createScene() { return null; }
	default void setupActions() {}

	static Border createBorder(Color color, CornerRadii cr) {
		return new Border(new BorderStroke(color == null ? Color.LIGHTGRAY : color, BorderStrokeStyle.SOLID,cr == null ? CornerRadii.EMPTY : cr, BorderWidths.DEFAULT));
	}

	default File onFileChooserRequest(Path classPathInitialDirectory) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(isValidDirectory.test(classPathInitialDirectory) ? classPathInitialDirectory.getParent().toFile() : Path.of(System.getProperty("user.home")).toFile());
		fileChooser.setSelectedExtensionFilter(new ExtensionFilter("pom.xml file", "pom.xml"));
		fileChooser.setTitle(P.GuiText.SET_POMPATH);
		return fileChooser.showOpenDialog(new Stage());
	}

	default File onDirectoryChooserRequest(Path pomXMLInitialDirectory) {
		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setInitialDirectory(isValidDirectory.test(pomXMLInitialDirectory) ? pomXMLInitialDirectory.getParent().toFile() : Path.of(System.getProperty("user.home")).toFile());
		dirChooser.setTitle(P.GuiText.SET_CLASSPATH);
		return dirChooser.showDialog(new Stage());
	}

	default Dialog<Void> createLoadingDialog() {
		Dialog<Void> loadingDialog = new Dialog<>();
		Stage dialogStage = ((Stage) loadingDialog.getDialogPane().getScene().getWindow());
		loadingDialog.setContentText("Resolving dependencies and loading files...");
		dialogStage.setOnCloseRequest(_ -> dialogStage.close());
		return loadingDialog;
	}
}
