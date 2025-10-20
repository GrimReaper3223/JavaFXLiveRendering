package com.dsl.jfx_live_rendering.gui.mainwindow.components;

import com.dsl.jfx_live_rendering.engine.Context;
import com.dsl.jfx_live_rendering.engine.concurrent.ClassPathLoader;
import com.dsl.jfx_live_rendering.engine.concurrent.PomDependencyResolver;
import com.dsl.jfx_live_rendering.gui.ContentTab;
import com.dsl.jfx_live_rendering.gui.commons.FXUtils;
import com.dsl.jfx_live_rendering.gui.events.FileSystemEvents;
import com.dsl.jfx_live_rendering.gui.events.RenderingEvents;
import com.dsl.jfx_live_rendering.properties.generated.P;
import com.dsl.jfx_live_rendering.view_model.MainWindowViewModel;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.nio.file.Path;
import java.util.Optional;

public class MainWindowTopComponent extends ToolBar implements FXUtils {

    // service running properties
    private final ReadOnlyBooleanProperty classPathLoaderServiceRunningProperty = Context.getService(ClassPathLoader.class).runningProperty();
    private final ReadOnlyBooleanProperty pomDependenciesLoaderServiceRunningProperty = Context.getService(PomDependencyResolver.class).runningProperty();

    private final Text selectClassText = new Text(P.GuiText.SELECT_JAVAFX_CLASS);
    private final ComboBox<Path> selectClassFileComboBox = new ComboBox<>();
    private final Hyperlink renderLink = new Hyperlink(P.GuiText.RENDER);
    private final Button pauseRenderButton = new Button(P.GuiText.PAUSE_RENDER);
    private final Button unpauseRenderButton = new Button(P.GuiText.UNPAUSE_RENDER);
    private final Button forceReloadButton = new Button(P.GuiText.FORCE_RELOAD);
    private final Hyperlink chooseAnotherClassPathLink = new Hyperlink(P.GuiText.CHOOSE_ANOTHER_CLASSPATH);
    private final Hyperlink unloadClassPathLink = new Hyperlink(P.GuiText.UNLOAD_CLASSPATH);
    private final CheckBox alwaysShowLogCheckbox = new CheckBox(P.GuiText.ALWAYS_SHOW_LOG);

    private final Dialog<Void> loadingDialog = createLoadingDialog();

    private final MainWindowViewModel mainWindowVM;

    public MainWindowTopComponent(MainWindowViewModel mainWindowVM) {
        this.mainWindowVM = mainWindowVM;

        Callback<ListView<Path>, ListCell<Path>> cellFactory = _ -> new ListCell<>() {
            @Override
            protected void updateItem(Path item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(item == null || empty ? null : new Text(item.getFileName().toString()));
            }
        };

        selectClassFileComboBox.setMaxSize(Double.MAX_VALUE, 20);
        selectClassFileComboBox.setButtonCell(cellFactory.call(null));
        selectClassFileComboBox.setCellFactory(cellFactory);

        setPadding(new Insets(10));
        setBorder(FXUtils.createBorder(null, null));
        getItems().setAll(selectClassText,
                selectClassFileComboBox,
                renderLink,
                new Separator(Orientation.VERTICAL),
                pauseRenderButton,
                unpauseRenderButton,
                forceReloadButton,
                new Separator(Orientation.VERTICAL),
                chooseAnotherClassPathLink,
                unloadClassPathLink,
                alwaysShowLogCheckbox);
        setupActions();
    }

    @Override
    public void setupActions() {
        renderLink.disableProperty().bind(selectClassFileComboBox.selectionModelProperty().getValue().selectedItemProperty().isNull());
        selectClassFileComboBox.itemsProperty().bind(mainWindowVM.javaFXClassListProperty());
        mainWindowVM.alwaysShowLogPaneProperty().bind(alwaysShowLogCheckbox.selectedProperty());

        pauseRenderButton.disableProperty().bind(mainWindowVM.tabSelectedProperty());
        unpauseRenderButton.disableProperty().bind(mainWindowVM.tabSelectedProperty());
        forceReloadButton.disableProperty().bind(mainWindowVM.tabSelectedProperty());

        renderLink.setOnAction(_ -> fireEvent(new RenderingEvents(RenderingEvents.START_RENDERING_REQUEST_EVENT, new ContentTab(selectClassFileComboBox.getSelectionModel().getSelectedItem()))));
        pauseRenderButton.setOnAction(_ -> fireEvent(new RenderingEvents(RenderingEvents.RENDERING_PAUSED_EVENT, null)));
        unpauseRenderButton.setOnAction(_ -> fireEvent(new RenderingEvents(RenderingEvents.RENDERING_UNPAUSED_EVENT, null)));
        forceReloadButton.setOnAction(_ -> fireEvent(new RenderingEvents(RenderingEvents.RENDERING_FORCED_EVENT, null)));
        chooseAnotherClassPathLink.setOnAction(_ -> onChooseAnotherClassPath());
        unloadClassPathLink.setOnAction(_ -> fireEvent(new FileSystemEvents(FileSystemEvents.CLASSPATH_UNLOAD_REQUEST_EVENT)));
    }

    private void onChooseAnotherClassPath() {
        Optional.ofNullable(onDirectoryChooserRequest(mainWindowVM.getPomXMLPath()))
                .ifPresent(response -> {
                    WeakChangeListener<Boolean> runningWeakListener = new WeakChangeListener<>((_, _, nv) -> {
                        if (nv) {
                            loadingDialog.show();
                        } else {
                            ((Stage) loadingDialog.getDialogPane().getScene().getWindow()).close();
                        }
                    });
                    Runnable action = () -> classPathLoaderServiceRunningProperty.addListener(runningWeakListener);
                    Path pathResponse = response.toPath();

                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Classpath Reloading Detected");
                    alert.setHeaderText("Classloader reloading detected. \nIt is recommended to select the pom.xml file corresponding to the new classloader. \n\nDo you want to select pom.xml again?");
                    alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
                    alert.showAndWait().filter(btn -> btn.equals(ButtonType.YES)).ifPresentOrElse(_ -> Optional.ofNullable(onFileChooserRequest(pathResponse))
                            .ifPresentOrElse(file -> {
                                classPathLoaderServiceRunningProperty.or(pomDependenciesLoaderServiceRunningProperty).addListener(runningWeakListener);
                                mainWindowVM.setPomXMLPath(file.toPath());
                            }, action), action);
                    mainWindowVM.setClassPath(pathResponse);
                });
    }
}
