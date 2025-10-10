package com.dsl.jfx_live_rendering.gui;

import java.nio.file.Path;

import com.dsl.jfx_live_rendering.engine.concurrent.Renderer;
import com.dsl.jfx_live_rendering.engine.impl.LoggerImpl;
import com.dsl.jfx_live_rendering.models.ProcessedPathModel;
import com.dsl.jfx_live_rendering.properties.generated.P;
import com.dsl.jfx_live_rendering.view_model.ServiceConverter;

import module javafx.base;
import module javafx.controls;

public class ContentTab extends Tab implements ServiceConverter<Node> {

	private ObjectProperty<TabRenderingState> tabRenderingState = new SimpleObjectProperty<>(TabRenderingState.IDLE);

	private StringProperty status = new SimpleStringProperty();
	private StringProperty loadedClass = new SimpleStringProperty();
	private StringProperty lastUpdated = new SimpleStringProperty();

	private Service<Node> renderingService;
	private final String className;

	public ContentTab(Path file) {
		ProcessedPathModel ppm = new ProcessedPathModel(file);
		super(ppm.getFileName());
		this.className = ppm.getFileName();

		renderingService = toService(new Renderer(ppm.getBinaryFileName()));
		renderingService.setOnSucceeded(this::onLiveRendering);
		renderingService.setOnFailed(this::onErrorRendering);
		tabRenderingState.addListener((_, _, nv) -> {
			setStatus(nv.getStateDescription());
			setLoadedClass("%s %s".formatted(P.Status.LOADED_CLASS, this.className));
			setLastUpdated(nv.getLastUpdatedDescription());
		});
		contentProperty().bind(renderingService.valueProperty());
		runService();
	}

	public void runService() {
		Platform.runLater(() -> renderingService.restart());
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
	 * onRenderingState methods
	 */
	private boolean isValidState() {
		return !getTabRenderingState().equals(TabRenderingState.ERROR_RENDERING);
	}

	// apenas a renderizacao forcada deve bypassar o metodo onRenderingProxy
	public void onForceRendering() {
		LoggerImpl.log(String.format("Forced reload for tab '%s' requested.", className));
		tabRenderingState.set(TabRenderingState.FORCED_RENDERING);
		runService();
	}

	public void onLiveRendering(WorkerStateEvent wse) {
		if(isValidState() && !getTabRenderingState().equals(TabRenderingState.PAUSED_RENDERING)) {
			LoggerImpl.log(String.format("'%s' tab is now rendering live.", className));
			tabRenderingState.set(TabRenderingState.LIVE_RENDERING);
		}
	}

	public void onPauseRendering() {
		if(isValidState() && getTabRenderingState().equals(TabRenderingState.LIVE_RENDERING)) {
    		LoggerImpl.log("'%s' tab has had its rendering suspended.".formatted(className));
    		tabRenderingState.set(TabRenderingState.PAUSED_RENDERING);
		}
	}

	public void onUnpauseRendering() {
		if(isValidState() && getTabRenderingState().equals(TabRenderingState.PAUSED_RENDERING)) {
			LoggerImpl.log("Tab '%s' has unpaused rendering. Updating content...".formatted(className));
			tabRenderingState.set(TabRenderingState.UNPAUSED_RENDERING);
			runService();
		}
	}

	public void onErrorRendering(WorkerStateEvent wse) {
		if(isValidState()) {
			LoggerImpl.log("An error has occurred when trying render '%s' tab.".formatted(className));
			tabRenderingState.set(TabRenderingState.ERROR_RENDERING);
			renderingService.reset();
		}
	}
}
