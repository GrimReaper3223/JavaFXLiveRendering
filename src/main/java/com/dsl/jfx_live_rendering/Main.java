package com.dsl.jfx_live_rendering;

import com.dsl.jfx_live_rendering.gui.StartWindow;
import com.dsl.jfx_live_rendering.gui.events.FileSystemHandlerEvents;
import com.dsl.jfx_live_rendering.properties.generated.P;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setScene(new StartWindow().createScene());
		primaryStage.setTitle(P.Metadata.APP_NAME);
		primaryStage.addEventHandler(FileSystemHandlerEvents.INIT_REQUEST_EVENT, _ -> primaryStage.close());
		primaryStage.show();
	}

	public static void main(String[] args) {
//		Generator.init("src/main/resources/values/strings", "com.dsl.jfx_live_rendering.properties", true);
//		Generator.generate();
		launch(args);
	}
}
