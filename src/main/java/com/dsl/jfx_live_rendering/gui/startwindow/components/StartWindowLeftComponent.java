package com.dsl.jfx_live_rendering.gui.startwindow.components;

import com.dsl.jfx_live_rendering.properties.generated.P;
import com.dsl.jfx_live_rendering.session_manager.Session;
import com.dsl.jfx_live_rendering.view_model.StartWindowViewModel;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Collections;
import java.util.Optional;

public class StartWindowLeftComponent extends VBox {

    private final ListView<Session> listView = new ListView<>();
    private final MenuItem deleteSessionMI = new MenuItem();
    private final ContextMenu cm = new ContextMenu(deleteSessionMI);
    private final Button clearSelection = new Button(P.GuiText.CLEAR_SELECTION);

    private final StartWindowViewModel startWindowVM;
    private final ChangeListener<Session> selectionSessionFromListListener;

    private Session activeSession = null;

    public StartWindowLeftComponent(StartWindowViewModel startWindowVM) {
        this.startWindowVM = startWindowVM;

        listView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Session item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setGraphic(null);

                if(!empty && item != null) {
                    setText(item.getApplicationModuleName());
                }
            }
        });

        selectionSessionFromListListener = (_, _, nv) -> Optional.ofNullable(nv).ifPresentOrElse(element -> {
            startWindowVM.setClassPath(element.getClassPath());
            startWindowVM.setPomXMLPath(element.getPomXMLPath());
            activeSession = element;
        }, () -> {
            startWindowVM.setClassPath("");
            startWindowVM.setPomXMLPath("");
            activeSession = null;
        });

        deleteSessionMI.setText(P.ContextMenuOptions.CONTEXT_MENU_DELETE);

        setPadding(new Insets(10, 0, 0, 10));
        setSpacing(10);
        getChildren().setAll(listView, clearSelection);
        startWindowVM.loadSessions();

        setupActions();
    }

    public void freeResources() {
        listView.itemsProperty().unbind();
        listView.visibleProperty().unbind();
        listView.managedProperty().unbind();
        clearSelection.managedProperty().unbind();
        clearSelection.visibleProperty().unbind();

        deleteSessionMI.removeEventHandler(ActionEvent.ANY, deleteSessionMI.getOnAction());
        listView.getSelectionModel().selectedItemProperty().removeListener(selectionSessionFromListListener);
    }

    private void setupActions() {
        listView.itemsProperty().bind(startWindowVM.sessionListProperty());
        listView.visibleProperty().bind(startWindowVM.sessionListProperty().isNotEqualTo(Collections.EMPTY_LIST));
        listView.managedProperty().bind(listView.visibleProperty());
        listView.setContextMenu(cm);

        clearSelection.managedProperty().bind(listView.visibleProperty());
        clearSelection.visibleProperty().bind(listView.visibleProperty().and(listView.getSelectionModel().selectedItemProperty().isNotNull()));

        // listeners
        listView.getSelectionModel().selectedItemProperty().addListener(selectionSessionFromListListener);

        // actions
        clearSelection.setOnAction(_ -> listView.getSelectionModel().clearSelection());
        deleteSessionMI.setOnAction(_ -> {
            Session session = listView.getSelectionModel().getSelectedItem();
            startWindowVM.deleteSessionFile(session);
            listView.getItems().remove(session);
        });
    }

    public Session getActiveSession() {
        return activeSession;
    }
}
