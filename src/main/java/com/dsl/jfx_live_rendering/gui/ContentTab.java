package com.dsl.jfx_live_rendering.gui;

import module javafx.controls;
import com.dsl.jfx_live_rendering.engine.concurrent.Renderer;
import com.dsl.jfx_live_rendering.engine.impl.ExceptionHandlerImpl;
import com.dsl.jfx_live_rendering.engine.impl.LoggerImpl;
import com.dsl.jfx_live_rendering.gui.events.RenderingEvents;
import com.dsl.jfx_live_rendering.models.ProcessedPathModel;
import com.dsl.jfx_live_rendering.properties.generated.P;
import com.dsl.jfx_live_rendering.view_model.ServiceConverter;

import java.nio.file.Path;
import java.util.Optional;

public class ContentTab extends Tab implements ServiceConverter<Node> {

	private final ObjectProperty<TabRenderingState> tabRenderingState = new SimpleObjectProperty<>(TabRenderingState.IDLE);

    private final StackPane pane = new StackPane();

	private final StringProperty status = new SimpleStringProperty();
	private final StringProperty loadedClass = new SimpleStringProperty();
	private final StringProperty lastUpdated = new SimpleStringProperty();

	private final Service<Node> renderingService;
	private final String className;
    private final ProcessedPathModel ppm;

	public ContentTab(Path file) {
        // instance setup
		ppm = new ProcessedPathModel(file);
		className = ppm.getFileName();

        // pane setup
		pane.setPadding(new Insets(10));

        // service setup
		renderingService = toService(new Renderer(ppm.getBinaryFileName()));
        configureService();

        // tabRenderingState setup
		tabRenderingState.addListener((_, _, nv) -> {
			setStatus(nv.getStateDescription());
			setLoadedClass("%s %s".formatted(P.Status.LOADED_CLASS, this.className));
			setLastUpdated(nv.getLastUpdatedDescription());
		});

        setText(className);
		setContent(pane);
		runService();
	}

	public void runService() {
		if (!tabRenderingState.get().equals(TabRenderingState.PAUSED_RENDERING)) {
			Platform.runLater(renderingService::restart);
		}
	}

	public TabRenderingState getTabRenderingState() {
		return this.tabRenderingState.get();
	}

	public void setTabRenderingState(TabRenderingState state) {
		this.tabRenderingState.set(state);
	}

	/*
	 * status methods
	 */
	public String getStatus() {
		return status.get();
	}

	public void setStatus(String status) {
		this.status.set(status);
	}

	public StringProperty statusProperty() {
		return status;
	}

	/*
	 * loadedClass methods
	 */
	public String getLoadedClass() {
		return loadedClass.get();
	}

	public void setLoadedClass(String loadedClass) {
		this.loadedClass.set(loadedClass);
	}

	public StringProperty loadedClassProperty() {
		return loadedClass;
	}

	/*
	 * lastUpdated methods
	 */
	public String getLastUpdated() {
		return lastUpdated.get();
	}

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated.set(lastUpdated);
	}

	public StringProperty lastUpdatedProperty() {
		return lastUpdated;
	}

    /*
     * ProcessedPathModel getter
     */
    public ProcessedPathModel getPpm() {
        return ppm;
    }

    private void configureService() {
        renderingService.setOnSucceeded(_ -> onRendering(new RenderingEvents(RenderingEvents.LIVE_RENDERING_EVENT, this)));
        renderingService.setOnFailed(_ -> onRendering(new RenderingEvents(RenderingEvents.RENDERING_ERROR_EVENT, this)));
        renderingService.valueProperty().addListener((_, _, nv) -> {
            var children = pane.getChildren();
            Optional.ofNullable(nv).ifPresentOrElse(children::setAll, children::clear);
        });
    }

    public void onRendering(RenderingEvents event) {
        switch(event.getEventType().getName()) {
            case "LIVE_RENDERING_EVENT" -> {
                if(!getTabRenderingState().equals(TabRenderingState.PAUSED_RENDERING)) {
                    LoggerImpl.log(String.format("'%s' tab is now rendering live.", className));
                    tabRenderingState.set(TabRenderingState.LIVE_RENDERING);
                }
            }
            case "RENDERING_PAUSED_EVENT" -> {
                if(getTabRenderingState().equals(TabRenderingState.LIVE_RENDERING)) {
                    LoggerImpl.log("'%s' tab has had its rendering suspended.".formatted(className));
                    tabRenderingState.set(TabRenderingState.PAUSED_RENDERING);
                }
            }
            case "RENDERING_UNPAUSED_EVENT" -> {
                if(getTabRenderingState().equals(TabRenderingState.PAUSED_RENDERING)) {
                    LoggerImpl.log("Tab '%s' has unpaused rendering. Updating content...".formatted(className));
                    tabRenderingState.set(TabRenderingState.UNPAUSED_RENDERING);
                    runService();
                }
            }
            case "RENDERING_FORCED_EVENT" -> {
                LoggerImpl.log(String.format("Forced reload for tab '%s' requested.", className));
                tabRenderingState.set(TabRenderingState.IDLE);
                runService();
            }
            case "RENDERING_ERROR_EVENT" -> {
                LoggerImpl.log("An error has occurred when trying render '%s' tab.".formatted(className));
                ExceptionHandlerImpl.logException(renderingService.getException());
                tabRenderingState.set(TabRenderingState.ERROR_RENDERING);
                renderingService.reset();
            }
        }

        if(event.getContentTab() == null) {
            event.setContentTab(this);
        }
    }
}
