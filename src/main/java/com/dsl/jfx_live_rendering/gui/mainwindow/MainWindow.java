package com.dsl.jfx_live_rendering.gui.mainwindow;

import module javafx.controls;
import com.dsl.jfx_live_rendering.engine.impl.ExceptionHandlerImpl;
import com.dsl.jfx_live_rendering.gui.ContentTab;
import com.dsl.jfx_live_rendering.gui.TabRenderingState;
import com.dsl.jfx_live_rendering.gui.commons.FXUtils;
import com.dsl.jfx_live_rendering.gui.events.FileSystemEvents;
import com.dsl.jfx_live_rendering.gui.events.RenderingEvents;
import com.dsl.jfx_live_rendering.gui.mainwindow.components.MainWindowBottomComponent;
import com.dsl.jfx_live_rendering.gui.mainwindow.components.MainWindowCenterComponent;
import com.dsl.jfx_live_rendering.gui.mainwindow.components.MainWindowTopComponent;
import com.dsl.jfx_live_rendering.view_model.MainWindowViewModel;

public class MainWindow extends BorderPane implements FXUtils {

    private final MainWindowViewModel mainWindowVM = new MainWindowViewModel();

    // views
    private final MainWindowTopComponent topComponent = new MainWindowTopComponent(mainWindowVM);
    private final MainWindowCenterComponent centerComponent = new MainWindowCenterComponent(mainWindowVM);
    private final MainWindowBottomComponent bottomComponent = new MainWindowBottomComponent();

    public MainWindow() {
        setTop(topComponent);
        setCenter(centerComponent);
        setBottom(bottomComponent);
        setupActions();

        addEventHandler(RenderingEvents.ANY, event -> {
            if(event.getEventType().equals(RenderingEvents.START_RENDERING_REQUEST_EVENT)) {
                if(mainWindowVM.getContentTabFromMap(event.getContentTab().getPpm().getPath()) != null) {
                    event.consume();
                    return;
                }
            }
            centerComponent.onRendering(event);
            bottomComponent.updateTabStatusInfo(event.getContentTab());
        });
        addEventHandler(FileSystemEvents.CLASSPATH_UNLOAD_REQUEST_EVENT, _ -> {
            mainWindowVM.unloadClassPath();
            centerComponent.clearTabs();
            bottomComponent.updateTabStatusInfo(null);
        });
    }

    public Scene createScene() {
        var screenBounds = Screen.getPrimary().getVisualBounds();
        return new Scene(this, screenBounds.getWidth(), screenBounds.getHeight());
    }

    @Override
    public void setupActions() {
        mainWindowVM.changedFileEntryProperty().addListener(_ -> {
            try {
                var entry = mainWindowVM.transformEntries();
                switch (entry.getKey().name()) {
                    case "ENTRY_DELETE" -> entry.getValue().forEach(ct -> ct.setTabRenderingState(TabRenderingState.ERROR_RENDERING));
                    case "ENTRY_MODIFY" -> entry.getValue().forEach(ContentTab::runService);
                    default -> throw new IllegalArgumentException("Unknown WatchEvent.Kind: " + entry.getKey().name());
                }
            } catch (Exception e) {
                ExceptionHandlerImpl.logException(e);
            }
        });
    }
}
