package com.dsl.jfx_live_rendering;

import com.dsl.jfx_live_rendering.gui.StartWindow;
import com.dsl.jfx_live_rendering.gui.events.FileSystemEvents;
import com.dsl.jfx_live_rendering.properties.generated.P;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setScene(new StartWindow().createScene());
		primaryStage.setTitle(P.Metadata.APP_NAME);
		primaryStage.addEventHandler(FileSystemEvents.INIT_REQUEST_EVENT, _ -> primaryStage.close());
		primaryStage.show();
	}

	public static void main(String[] args) {
		// NOTE: Desabilitar ao fazer deploy. Este framework serve somente para mapear relacionalmente arquivos de propriedades para classes java
//		Generator.init("src/main/resources/values/strings", "com.dsl.jfx_live_rendering.properties", true);
//		Generator.generate();
		launch(args);
	}
}
