package com.dsl.jfx_live_rendering.gui.mainwindow.components;

import com.dsl.jfx_live_rendering.gui.ContentTab;
import com.dsl.jfx_live_rendering.gui.commons.FXUtils;
import com.dsl.jfx_live_rendering.gui.events.RenderingEvents;
import com.dsl.jfx_live_rendering.properties.generated.P;
import com.dsl.jfx_live_rendering.view_model.MainWindowViewModel;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.controlsfx.control.HiddenSidesPane;

import java.util.Map;

public class MainWindowCenterComponent extends HiddenSidesPane {

    private final TabPane tabPane = new TabPane();
    private final Text logHeaderText = new Text(P.GuiText.LOG_AREA);
    private final TextArea logTextArea = new TextArea();
    private final Button copyToClipboardButton = new Button(P.GuiText.COPY_TO_CLIPBOARD);
    private final Button clearLogButton = new Button(P.GuiText.CLEAR_LOG);

    private final MainWindowViewModel mainWindowVM;

    public MainWindowCenterComponent(MainWindowViewModel mainWindowVM) {
        this.mainWindowVM = mainWindowVM;

        HBox buttonBox = new HBox(copyToClipboardButton, clearLogButton);
        VBox logBox = new VBox(logHeaderText, logTextArea, buttonBox);
        VBox.setVgrow(logTextArea, Priority.SOMETIMES);

        tabPane.setTabDragPolicy(TabPane.TabDragPolicy.REORDER);

        logBox.setSpacing(10);
        logBox.setPadding(new Insets(10));
        logBox.setBackground(Background.fill(Color.LIGHTGRAY));
        logBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setSpacing(10);
        buttonBox.setPadding(new Insets(5, 0, 5, 5));
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        logTextArea.setBorder(FXUtils.createBorder(null, new CornerRadii(3)));
        logTextArea.setEditable(false);

        setContent(tabPane);
        setRight(logBox);
        setupActions();
    }

    private void setupActions() {
        // binds
        mainWindowVM.tabSelectedProperty().bind(tabPane.getSelectionModel().selectedItemProperty().isNull());

        // listeners
        mainWindowVM.logProperty().addListener((_, _, nv) -> logTextArea.appendText(nv));
        mainWindowVM.alwaysShowLogPaneProperty().addListener((_, _, nv) -> setPinnedSide(nv ? Side.RIGHT : null));
        tabPane.getTabs().addListener((ListChangeListener<? super Tab>) listener -> {
            listener.next();
            if(listener.wasAdded()) {
                mainWindowVM.addContentTabToMap(listener.getAddedSubList().stream().map(tab -> {
                    ContentTab ct = (ContentTab) tab;
                    return Map.entry(ct.getPpm().getPath(), ct);
                }).toList());

            } else if(listener.wasRemoved()) {
                mainWindowVM.removeContentTabFromMap(listener.getRemoved().stream().map(tab -> ((ContentTab)tab).getPpm().getPath()).toList());
                tabPane.getSelectionModel().clearSelection();
            }
        });

        // actions
        clearLogButton.setOnAction(_ -> logTextArea.clear());
        copyToClipboardButton.setOnAction(_ -> {
            logTextArea.selectAll();
            logTextArea.copy();
            logTextArea.deselect();
        });
    }

    private void onStartRenderingRequest(ContentTab ct) {
        tabPane.getTabs().add(ct);
        tabPane.getSelectionModel().select(ct);
    }

    public void onRendering(RenderingEvents renderingEvent) {
        if(renderingEvent.getEventType().equals(RenderingEvents.START_RENDERING_REQUEST_EVENT)) {
            onStartRenderingRequest(renderingEvent.getContentTab());
        } else {
            ((ContentTab) tabPane.getSelectionModel().getSelectedItem()).onRendering(renderingEvent);
        }
    }

    public void clearTabs() {
        tabPane.getTabs().clear();
    }
}
