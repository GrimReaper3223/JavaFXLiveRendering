package com.dsl.jfx_live_rendering.gui;

import java.io.File;

import org.controlsfx.control.HyperlinkLabel;

import com.dsl.jfx_live_rendering.gui.events.FileSystemHandlerEvents;
import com.dsl.jfx_live_rendering.properties.generated.P;
import com.dsl.jfx_live_rendering.view_model.StartWindowViewModel;

import module javafx.controls;

public class StartWindow extends BorderPane implements BorderPaneConfigHelper {

	// classpath texts
	private Label classPathLabel = new Label(P.GuiText.CLASSPATH);
	private Text classPathText = new Text();

	// pom texts
	private Label pomXMLPathLabel = new Label(P.GuiText.POMPATH);
	private Text pomXMLPathText = new Text();

	// buttons
	private Button setClassPathButton = new Button(P.GuiText.SET_CLASSPATH);
	private Button setPomXMLPathButton = new Button(P.GuiText.SET_POMPATH);
	private Button initButton = new Button(P.GuiText.INIT);

	// status messages
	private Text classPathCheckingStatusText = new Text();
	private Text pomXMLPathCheckingStatusText = new Text();

	// bottom components
	private Text appVersionText = new Text(String.format("%s %s", P.Metadata.APP_NAME, P.Metadata.APP_VERSION));
	private HyperlinkLabel authorLink = new HyperlinkLabel(String.format("%s - [%s]", P.Metadata.APP_AUTHOR, P.Metadata.APP_HOMEPAGE));

	// view model
	private StartWindowViewModel startWindowVM = new StartWindowViewModel();

	public StartWindow() {
		setCenter(configCenter());
		setBottom(configBottom());
		setupActions();
	}

	@Override
	public Scene createScene() {
		var screenBounds = Screen.getPrimary().getVisualBounds();
		return new Scene(this, screenBounds.getWidth() / 2, screenBounds.getHeight() / 2);
	}

	@Override
	public Node configCenter() {
		GridPane grid = new GridPane();
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.SOMETIMES);
		HBox buttonHBox = new HBox(setClassPathButton, setPomXMLPathButton, spacer, initButton);

		initButton.setPrefWidth(60);

		buttonHBox.setSpacing(10);

		grid.setHgap(15);
		grid.setVgap(15);
		grid.setPadding(new Insets(10));
		grid.setAlignment(Pos.CENTER);

		grid.add(classPathLabel, 0, 0);
		grid.add(classPathText, 1, 0);
		grid.add(pomXMLPathLabel, 0, 1);
		grid.add(pomXMLPathText, 1, 1);
		grid.add(buttonHBox, 0, 2, 2, 1);
		grid.add(classPathCheckingStatusText, 0, 3, 2, 1);
		grid.add(pomXMLPathCheckingStatusText, 0, 4, 2, 1);

		return grid;
	}

	@Override
	public Node configBottom() {
		Region spacer = new Region();

		HBox infoBox = new HBox(appVersionText, spacer, authorLink);
		HBox.setHgrow(spacer, Priority.ALWAYS);

		infoBox.setPadding(new Insets(8));
		return infoBox;
	}

	@Override
	public void setupActions() {
		// classPath bind
		classPathText.textProperty().bind(startWindowVM.classPathProperty().asString());
//		classPathCheckingStatusText.textProperty().bind(startWindowVM.classPathCheckingStatusProperty());

		// pomXMLPath bind
		pomXMLPathText.textProperty().bind(startWindowVM.pomXMLPathProperty().asString());
//		pomXMLPathCheckingStatusText.textProperty().bind(startWindowVM.pomXMLCheckingStatusProperty());

		// button actions
		setClassPathButton.setOnAction(_ -> {
			File response = onDirectoryChooserRequest(startWindowVM.getClassPath());

			if(response != null) {
				startWindowVM.setClassPath(response.toPath());
			}
		});
		setPomXMLPathButton.setOnAction(_ -> {
			File response = onFileChooserRequest(startWindowVM.getPomXMLPath());

			if(response != null) {
				startWindowVM.setPomXMLPath(response.toPath());
			}
		});
		initButton.setOnAction(_ -> {
			this.fireEvent(new FileSystemHandlerEvents(FileSystemHandlerEvents.INIT_REQUEST_EVENT));
			Dialog<Void> loadingDialog = configureDialog();
			startWindowVM.getClassPathloaderService().runningProperty().or(startWindowVM.getPomModuleDependencyLoaderService().runningProperty()).addListener((_, _, nv) -> {
				if(nv) {
					loadingDialog.show();
				} else {
					((Stage) loadingDialog.getDialogPane().getScene().getWindow()).close();
					Stage mainStage = new Stage();
					mainStage.setScene(new MainWindow().createScene());
					mainStage.setTitle(P.Metadata.APP_NAME);
					mainStage.show();
				}
			});
			startWindowVM.init();
		});
	}

	private Dialog<Void> configureDialog() {
		Dialog<Void> loadingDialog = new Dialog<>();
		Stage dialogStage = ((Stage) loadingDialog.getDialogPane().getScene().getWindow());
		loadingDialog.setContentText("Resolving and loading files...");
		dialogStage.setOnCloseRequest(_ -> dialogStage.close());
		return loadingDialog;
	}
}
