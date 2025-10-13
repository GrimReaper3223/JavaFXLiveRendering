package com.dsl.jfx_live_rendering.gui;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.controlsfx.control.HiddenSidesPane;

import com.dsl.jfx_live_rendering.engine.Context;
import com.dsl.jfx_live_rendering.engine.concurrent.ClassPathLoader;
import com.dsl.jfx_live_rendering.engine.concurrent.PomDependencyResolver;
import com.dsl.jfx_live_rendering.properties.generated.P;
import com.dsl.jfx_live_rendering.view_model.MainWindowViewModel;

import module javafx.base;
import module javafx.controls;
import javafx.scene.control.TabPane.TabDragPolicy;

public class MainWindow extends BorderPane implements BorderPaneConfigHelper, FXUtils {

	// top pane components (TOOLBAR)
	private Text selectClassText = new Text(P.GuiText.SELECT_JAVAFX_CLASS);
	private ComboBox<Path> selectClassFileComboBox = new ComboBox<>();
	private Hyperlink renderLink = new Hyperlink(P.GuiText.RENDER);
	private Button pauseRenderButton = new Button(P.GuiText.PAUSE_RENDER);
	private Button unpauseRenderButton = new Button(P.GuiText.UNPAUSE_RENDER);
	private Button forceReloadButton = new Button(P.GuiText.FORCE_RELOAD);
	private Hyperlink chooseAnotherClasspathLink = new Hyperlink(P.GuiText.CHOOSE_ANOTHER_CLASSPATH);
	private Hyperlink unloadClassPathLink = new Hyperlink(P.GuiText.UNLOAD_CLASSPATH);
	private CheckBox alwaysShowLogCheckbox = new CheckBox(P.GuiText.ALWAYS_SHOW_LOG);

	// center pane components
	private HiddenSidesPane hsp = new HiddenSidesPane();
	private TabPane tabPane = new TabPane();
	private Text logHeaderText = new Text(P.GuiText.LOG_AREA);
	private TextArea logTextArea = new TextArea();
	private Button copyToClipboardButton = new Button(P.GuiText.COPY_TO_CLIPBOARD);
	private Button clearLogButton = new Button(P.GuiText.CLEAR_LOG);

	// bottom pane components (STATUS BAR)
	private Label statusLabel = new Label();
	private Label loadedClassLabel = new Label();
	private Label lastUpdatedLabel = new Label();

	private MainWindowViewModel mainWindowVM = new MainWindowViewModel();
	private Service<List<Path>> classPathLoaderService = Context.getService(ClassPathLoader.class);
	private Service<List<Path>> pomDependenciesLoaderService = Context.getService(PomDependencyResolver.class);

	// loading files dialog
	private Dialog<Void> loadingDialog = createLoadingDialog();

	public MainWindow() {
		setTop(configTop());
		setCenter(configCenter());
		setBottom(configBottom());
		setupActions();
	}

	public Scene createScene() {
		var screenBounds = Screen.getPrimary().getVisualBounds();
		return new Scene(this, screenBounds.getWidth(), screenBounds.getHeight());
	}

	@Override
	public Node configTop() {
		ToolBar toolBar = new ToolBar(selectClassText,
				selectClassFileComboBox,
				renderLink,
				new Separator(Orientation.VERTICAL),
				pauseRenderButton,
				unpauseRenderButton,
				forceReloadButton,
				new Separator(Orientation.VERTICAL),
				chooseAnotherClasspathLink,
				unloadClassPathLink,
				alwaysShowLogCheckbox);

		Callback<ListView<Path>, ListCell<Path>> cellFactory = _ -> new ListCell<Path>() {
			@Override
			protected void updateItem(Path item, boolean empty) {
				super.updateItem(item, empty);
				setGraphic(item == null || empty ? null : new Text(item.getFileName().toString()));
			}
		};

		selectClassFileComboBox.setMaxSize(Double.MAX_VALUE, 20);
		selectClassFileComboBox.setButtonCell(cellFactory.call(null));
		selectClassFileComboBox.setCellFactory(cellFactory);
		toolBar.setPadding(new Insets(10));
		toolBar.setBorder(createBorder(null, null));
		return toolBar;
	}

	@Override
	public Node configCenter() {
		HBox buttonBox = new HBox(copyToClipboardButton, clearLogButton);
		VBox logBox = new VBox(logHeaderText, logTextArea, buttonBox);
		VBox.setVgrow(logTextArea, Priority.SOMETIMES);

		tabPane.setTabDragPolicy(TabDragPolicy.REORDER);

		hsp.setContent(tabPane);
		hsp.setRight(logBox);

		logBox.setSpacing(10);
		logBox.setPadding(new Insets(10));
		logBox.setBackground(Background.fill(Color.LIGHTGRAY));
		logBox.setAlignment(Pos.CENTER_LEFT);
		buttonBox.setSpacing(10);
		buttonBox.setPadding(new Insets(5, 0, 5, 5));
		buttonBox.setAlignment(Pos.CENTER_RIGHT);

		logTextArea.setBorder(createBorder(null, new CornerRadii(3)));
		logTextArea.setEditable(false);

		return hsp;
	}

	@Override
	public Node configBottom() {
		HBox statusHBox = new HBox();
		Supplier<Region> spacerSupplier = Region::new;

		statusHBox.setPadding(new Insets(7));
		statusHBox.setSpacing(10);
		statusHBox.setBorder(createBorder(null, null));

		statusHBox.getChildren().addAll(statusLabel, spacerSupplier.get(), new Separator(Orientation.VERTICAL),
				spacerSupplier.get(), loadedClassLabel, spacerSupplier.get(), new Separator(Orientation.VERTICAL),
				spacerSupplier.get(), lastUpdatedLabel);

		statusHBox.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));
		return statusHBox;
	}

	@Override
	public void setupActions() {
		topPaneSetupActions();
		centerPaneSetupActions();
		bottomPaneSetupActions();
	}

	private void topPaneSetupActions() {
		renderLink.disableProperty().bind(selectClassFileComboBox.selectionModelProperty().getValue().selectedItemProperty().isNull());
		selectClassFileComboBox.itemsProperty().bind(mainWindowVM.javaFXClassListProperty());

		BooleanBinding isTabSelected = tabPane.getSelectionModel().selectedItemProperty().isNull();
		pauseRenderButton.disableProperty().bind(isTabSelected);
		unpauseRenderButton.disableProperty().bind(isTabSelected);
		forceReloadButton.disableProperty().bind(isTabSelected);

		renderLink.setOnAction(_ -> {
			ContentTab ct = new ContentTab(selectClassFileComboBox.getSelectionModel().getSelectedItem());
			tabPane.getTabs().add(ct);
			tabPane.getSelectionModel().select(ct);
		});
		pauseRenderButton.setOnAction(_ -> FXUtils.tabCast(tabPane.getSelectionModel().getSelectedItem()).onPauseRendering());
		unpauseRenderButton.setOnAction(_ -> FXUtils.tabCast(tabPane.getSelectionModel().getSelectedItem()).onUnpauseRendering());
		forceReloadButton.setOnAction(_ -> FXUtils.tabCast(tabPane.getSelectionModel().getSelectedItem()).onForceRendering());
		unloadClassPathLink.setOnAction(_ -> {
			mainWindowVM.unloadClassPath();
			tabPane.getTabs().clear();
			tabPane.getSelectionModel().clearSelection();
		});

		chooseAnotherClasspathLink.setOnAction(_ -> {
			File response = onDirectoryChooserRequest(mainWindowVM.getPomXMLPath());

			if(response != null) {
				Subscription subscription = null;
				Path responsePath = response.toPath();

				Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
				alert.setTitle("Classpath Reloading Detected");
				alert.setHeaderText("Classloader reloading detected. \nIt is recommended to select the pom.xml file corresponding to the new classloader. \n\nDo you want to select pom.xml again?");
				alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
				if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
					File file = onFileChooserRequest(responsePath);
					if(file != null) {
						subscription = createSubscriber(classPathLoaderService.runningProperty(), pomDependenciesLoaderService.runningProperty());
						mainWindowVM.setPomXMLPath(file.toPath());
					}
				} else {
					subscription = createSubscriber(classPathLoaderService.runningProperty(), null);
				}
				mainWindowVM.setClassPath(responsePath);
				subscription.unsubscribe();
			}
		});

		mainWindowVM.changedPathEntryProperty().addListener(_ -> {
			try {
				var entry = mainWindowVM.transformEntries();
				switch (entry.getKey().name()) {
					case "ENTRY_DELETE" -> entry.getValue().forEach(ct -> ct.setTabRenderingState(TabRenderingState.ERROR_RENDERING));
					case "ENTRY_MODIFY" -> entry.getValue().forEach(ContentTab::runService);
					default -> throw new IllegalArgumentException("Unknown WatchEvent.Kind: " + entry.getKey().name());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private void centerPaneSetupActions() {
		mainWindowVM.logProperty().addListener((_, _, nv) -> logTextArea.appendText(nv));
		alwaysShowLogCheckbox.selectedProperty().addListener(_ -> hsp.setPinnedSide(alwaysShowLogCheckbox.isSelected() ? Side.RIGHT : null));
		clearLogButton.setOnAction(_ -> logTextArea.clear());
		copyToClipboardButton.setOnAction(_ -> {
			logTextArea.selectAll();
			logTextArea.copy();
			logTextArea.deselect();
		});

		tabPane.getTabs().addListener((ListChangeListener<Tab>) listener -> {
			listener.next();
			if (listener.wasAdded()) {
				mainWindowVM.addContentTabToMap(listener.getAddedSubList());
			} else if (listener.wasRemoved()) {
				mainWindowVM.removeContentTabFromMap(listener.getRemoved());
			}
		});
	}

	private void bottomPaneSetupActions() {
		tabPane.getSelectionModel().selectedItemProperty().addListener((_, _, tab) -> updateTabStatusInfo(tab != null ? FXUtils.tabCast(tab) : null));
	}

	// NOTE: verificar se podemos aprimorar este metodo
	private void updateTabStatusInfo(ContentTab contentTab) {
		Platform.runLater(() -> Stream
				.of(statusLabel.textProperty(), loadedClassLabel.textProperty(), lastUpdatedLabel.textProperty())
				.map(sp -> {
					if (sp.isBound()) {
						sp.unbind();
					}
					return sp;
				})
				.forEach(sp -> {
					if (contentTab != null) {
						if (sp.equals(statusLabel.textProperty())) {
							sp.bind(contentTab.statusProperty());

						} else if (sp.equals(loadedClassLabel.textProperty())) {
							sp.bind(contentTab.loadedClassProperty());

						} else if (sp.equals(lastUpdatedLabel.textProperty())) {
							sp.bind(contentTab.lastUpdatedProperty());
						}
					} else {
						sp.set("");
					}
				}));
	}

	private Subscription createSubscriber(ReadOnlyBooleanProperty p1, ReadOnlyBooleanProperty p2) {
		Predicate<Boolean> instruction = val -> {
			if (val) {
				loadingDialog.show();
			} else {
				((Stage) loadingDialog.getDialogPane().getScene().getWindow()).close();
			}
			return val;
		};
		return p2 != null ? p1.or(p2).subscribe((_, nv) -> instruction.test(nv)) : p1.subscribe((_, nv) -> instruction.test(nv));
	}
}
