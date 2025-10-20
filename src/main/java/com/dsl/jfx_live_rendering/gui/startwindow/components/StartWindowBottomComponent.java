package com.dsl.jfx_live_rendering.gui.startwindow.components;

import com.dsl.jfx_live_rendering.properties.generated.P;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import org.controlsfx.control.HyperlinkLabel;

public class StartWindowBottomComponent extends HBox {

    private final Text appVersionText = new Text(String.format("%s %s", P.Metadata.APP_NAME, P.Metadata.APP_VERSION));
    private final HyperlinkLabel authorLink = new HyperlinkLabel(String.format("%s - [%s]", P.Metadata.APP_AUTHOR, P.Metadata.APP_HOMEPAGE));

    public StartWindowBottomComponent() {
        Region spacer = new Region();
        setHgrow(spacer, Priority.ALWAYS);
        setPadding(new Insets(8));

        getChildren().setAll(appVersionText, spacer, authorLink);
    }
}
