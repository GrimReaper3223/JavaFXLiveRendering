package com.dsl.jfx_live_rendering.gui;

import java.io.File;
import java.util.function.Supplier;

import org.controlsfx.control.HyperlinkLabel;

import com.dsl.jfx_live_rendering.gui.events.FileSystemEvents;
import com.dsl.jfx_live_rendering.properties.generated.P;
import com.dsl.jfx_live_rendering.session_manager.Session;
import com.dsl.jfx_live_rendering.view_model.StartWindowViewModel;

import module javafx.base;
import module javafx.controls;

public class StartWindow extends BorderPane implements BorderPaneConfigHelper, FXUtils {

	private Supplier<Region> regionSupplier = Region::new;

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

	// left components
	private ListView<Session> listView = new ListView<>();
	private Button clearSelection = new Button("Clear Selection");
	private Button deleteSelection = new Button("Delete Selection");

	// view model
	private StartWindowViewModel startWindowVM = new StartWindowViewModel();

	// loading files dialog
	private Dialog<Void> loadingDialog = createLoadingDialog();

	// subscription
	private final Subscription subscription = startWindowVM.getClassPathloaderService().runningProperty().or(startWindowVM.getPomModuleDependencyLoaderService().runningProperty()).subscribe((_, nv) -> {
		if(nv) {
			loadingDialog.show();
		} else {
			((Stage) loadingDialog.getDialogPane().getScene().getWindow()).close();
			fireEvent(new FileSystemEvents(FileSystemEvents.INIT_COMPLETED_EVENT));
		}
	});

	public StartWindow() {
		setBottom(configBottom());
		setLeft(configLeft());
		setCenter(configCenter());
		setupActions();
	}

	@Override
	public Scene createScene() {
		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		return new Scene(this, screenBounds.getWidth() / 2, screenBounds.getHeight() / 2);
	}

	@Override
	public Node configLeft() {
		listView.setCellFactory(_ -> new ListCell<Session>() {
			@Override
			protected void updateItem(Session item, boolean empty) {
				super.updateItem(item, empty);
				setText(null);
				setGraphic(null);

				if(!empty && item != null) {
					setText(item.getApplicationModuleName());
				}
			};
		});

		Region spacer = regionSupplier.get();
		HBox buttonBox = new HBox(clearSelection, spacer, deleteSelection);
		VBox box = new VBox(listView, buttonBox);
		box.setPadding(new Insets(10, 0, 0, 10));
		box.setSpacing(10);

		HBox.setHgrow(spacer, Priority.SOMETIMES);
		startWindowVM.loadSessions();
		return box;
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
		Region spacer = regionSupplier.get();

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

		// listView bind
		listView.itemsProperty().bind(startWindowVM.sessionListProperty());
		listView.visibleProperty().bind(startWindowVM.sessionListProperty().isNotEqualTo(FXCollections.observableArrayList()));
		listView.managedProperty().bind(listView.visibleProperty());
		listView.getSelectionModel().selectedItemProperty().addListener((_, _, nv) -> {
			if(nv != null) {
				startWindowVM.setClassPath(nv.getClassPath());
				startWindowVM.setPomXMLPath(nv.getPomXMLPath());
			}
		});

		clearSelection.managedProperty().bind(listView.visibleProperty());
		clearSelection.visibleProperty().bind(listView.visibleProperty());
		deleteSelection.managedProperty().bind(listView.visibleProperty());
		deleteSelection.visibleProperty().bind(listView.visibleProperty());

		// button actions
		clearSelection.setOnAction(_ -> listView.getSelectionModel().clearSelection());
		deleteSelection.setOnAction(_ -> {
			Session session = listView.getSelectionModel().getSelectedItem();
			startWindowVM.deleteSessionFile(session);
			listView.getItems().remove(session);
		});
		setClassPathButton.setOnAction(_ -> {
			File response = onDirectoryChooserRequest(startWindowVM.getPomXMLPath());

			if(response != null) {
				startWindowVM.setClassPath(response.toPath());
			}
		});
		setPomXMLPathButton.setOnAction(_ -> {
			File response = onFileChooserRequest(startWindowVM.getClassPath());

			if(response != null) {
				startWindowVM.setPomXMLPath(response.toPath());
			}
		});
		initButton.setOnAction(_ -> {
			fireEvent(new FileSystemEvents(FileSystemEvents.INIT_REQUEST_EVENT));
			startWindowVM.init(listView.getSelectionModel().getSelectedItem());
		});

		addEventHandler(FileSystemEvents.INIT_COMPLETED_EVENT, _ -> {
			subscription.unsubscribe();
			startWindowVM.defineSessionModuleFinders();
		});
	}
}
