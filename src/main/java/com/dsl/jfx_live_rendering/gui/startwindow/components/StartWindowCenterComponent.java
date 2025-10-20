package com.dsl.jfx_live_rendering.gui.startwindow.components;

import com.dsl.jfx_live_rendering.gui.events.FileSystemEvents;
import com.dsl.jfx_live_rendering.properties.generated.P;
import com.dsl.jfx_live_rendering.view_model.StartWindowViewModel;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

import java.util.List;
import java.util.function.Consumer;

public class StartWindowCenterComponent extends GridPane {

    private final Consumer<? super ButtonBase> actionEventHandlerRemover = node -> node.removeEventHandler(ActionEvent.ANY, node.getOnAction());

    private final Label classPathLabel = new Label(P.GuiText.CLASSPATH);
    private final Text classPathText = new Text();
    private final Label pomXMLPathLabel = new Label(P.GuiText.POMPATH);
    private final Text pomXMLPathText = new Text();
    private final Button setClassPathButton = new Button(P.GuiText.SET_CLASSPATH);
    private final Button setPomXMLPathButton = new Button(P.GuiText.SET_POMPATH);
    private final Button initButton = new Button(P.GuiText.INIT);

//    private final Text classPathCheckingStatusText = new Text();
//    private final Text pomXMLPathCheckingStatusText = new Text();

    private final StartWindowViewModel startWindowVM;

    public StartWindowCenterComponent(StartWindowViewModel startWindowVM) {
        this.startWindowVM = startWindowVM;

        Region spacer = new Region();
        HBox buttonHBox = new HBox(setClassPathButton, setPomXMLPathButton, spacer, initButton);
        HBox.setHgrow(spacer, Priority.SOMETIMES);

        initButton.setPrefWidth(70);

        buttonHBox.setSpacing(10);

        setHgap(15);
        setVgap(15);
        setPadding(new Insets(10));
        setAlignment(Pos.CENTER);

        add(classPathLabel, 0, 0);
        add(classPathText, 1, 0);
        add(pomXMLPathLabel, 0, 1);
        add(pomXMLPathText, 1, 1);
        add(buttonHBox, 0, 2, 2, 1);
//        add(classPathCheckingStatusText, 0, 3, 2, 1);
//        add(pomXMLPathCheckingStatusText, 0, 4, 2, 1);

        setupActions();
    }

    public void freeResources() {
        classPathText.textProperty().unbind();
        pomXMLPathText.textProperty().unbind();

        List.of(setClassPathButton, setPomXMLPathButton, initButton).forEach(actionEventHandlerRemover);
    }

    private void setupActions() {
        // binds
        classPathText.textProperty().bind(startWindowVM.classPathProperty().asString());
        // classPathCheckingStatusText.textProperty().bind(startWindowVM.classPathCheckingStatusProperty());
        pomXMLPathText.textProperty().bind(startWindowVM.pomXMLPathProperty().asString());
        // pomXMLPathCheckingStatusText.textProperty().bind(startWindowVM.pomXMLCheckingStatusProperty());

        setClassPathButton.setOnAction(_ -> fireEvent(new FileSystemEvents(FileSystemEvents.CLASSPATH_LOAD_REQUEST_EVENT)));
        setPomXMLPathButton.setOnAction(_ -> fireEvent(new FileSystemEvents(FileSystemEvents.POM_XML_LOAD_REQUEST_EVENT)));
        initButton.setOnAction(_ -> fireEvent(new FileSystemEvents(FileSystemEvents.INIT_PATH_PROCESS_EVENT)));
    }
}
