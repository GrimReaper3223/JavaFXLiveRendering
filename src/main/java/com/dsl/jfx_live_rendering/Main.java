package com.dsl.jfx_live_rendering;

import atlantafx.base.theme.PrimerLight;
import com.dsl.jfx_live_rendering.gui.events.FileSystemEvents;
import com.dsl.jfx_live_rendering.gui.mainwindow.MainWindow;
import com.dsl.jfx_live_rendering.gui.startwindow.StartWindow;
import com.dsl.jfx_live_rendering.properties.generated.P;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
	
	@Override
	public void start(Stage primaryStage) {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

		primaryStage.setScene(new StartWindow().createScene());
		primaryStage.setTitle(P.Metadata.APP_NAME);
		primaryStage.addEventHandler(FileSystemEvents.INIT_PATH_PROCESS_EVENT, _ -> primaryStage.close());
		primaryStage.addEventHandler(FileSystemEvents.PATH_PROCESS_COMPLETED_EVENT, _ -> {
			Stage mainStage = new Stage();
			mainStage.setScene(new MainWindow().createScene());
			mainStage.setTitle(P.Metadata.APP_NAME);
            mainStage.setOnCloseRequest(_ -> System.exit(0));
			mainStage.show();
		});
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		// NOTE: Desabilitar ao fazer deploy. Este framework serve somente para mapear relacionalmente arquivos de propriedades para classes java
//        Generator.init("src/main/resources/values/strings", "com.dsl.jfx_live_rendering.properties", true);
//        Generator.generate();
		launch(args);
	}
}
