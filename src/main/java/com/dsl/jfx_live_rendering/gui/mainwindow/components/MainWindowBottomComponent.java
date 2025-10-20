package com.dsl.jfx_live_rendering.gui.mainwindow.components;

import com.dsl.jfx_live_rendering.gui.ContentTab;
import com.dsl.jfx_live_rendering.gui.commons.FXUtils;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MainWindowBottomComponent extends HBox {

    private final Label statusLabel = new Label();
    private final Label loadedClassLabel = new Label();
    private final Label lastUpdatedLabel = new Label();

    private final StringProperty statusProperty = statusLabel.textProperty();
    private final StringProperty loadedClassProperty = loadedClassLabel.textProperty();
    private final StringProperty lastUpdatedProperty = lastUpdatedLabel.textProperty();

    public MainWindowBottomComponent() {
        Supplier<Region> spacerSupplier = Region::new;

        setPadding(new Insets(7));
        setSpacing(10);
        setBorder(FXUtils.createBorder(null, null));

        getChildren().addAll(statusLabel, spacerSupplier.get(), new Separator(Orientation.VERTICAL),
                spacerSupplier.get(), loadedClassLabel, spacerSupplier.get(), new Separator(Orientation.VERTICAL),
                spacerSupplier.get(), lastUpdatedLabel);

        getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));
    }

    public void updateTabStatusInfo(ContentTab contentTab) {
        Platform.runLater(() -> Stream
                .of(statusProperty, loadedClassProperty, lastUpdatedProperty)
                .forEach(stringProperty -> {
                    stringProperty.unbind();
                    Optional.ofNullable(contentTab).ifPresentOrElse(ct -> {
                        switch(stringProperty) {
                            case StringProperty status when Objects.equals(stringProperty, statusProperty) -> status.bind(ct.statusProperty());
                            case StringProperty loadedClass when Objects.equals(stringProperty, loadedClassProperty) -> loadedClass.bind(ct.loadedClassProperty());
                            case StringProperty lastUpdated -> lastUpdated.bind(ct.lastUpdatedProperty());
                        }
                    }, () -> stringProperty.set(""));
                }));
    }
}
