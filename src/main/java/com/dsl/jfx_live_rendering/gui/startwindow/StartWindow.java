package com.dsl.jfx_live_rendering.gui.startwindow;

import module javafx.controls;
import com.dsl.jfx_live_rendering.engine.Context;
import com.dsl.jfx_live_rendering.engine.concurrent.ClassPathLoader;
import com.dsl.jfx_live_rendering.engine.concurrent.PomDependencyResolver;
import com.dsl.jfx_live_rendering.gui.commons.FXUtils;
import com.dsl.jfx_live_rendering.gui.events.FileSystemEvents;
import com.dsl.jfx_live_rendering.gui.startwindow.components.StartWindowBottomComponent;
import com.dsl.jfx_live_rendering.gui.startwindow.components.StartWindowCenterComponent;
import com.dsl.jfx_live_rendering.gui.startwindow.components.StartWindowLeftComponent;
import com.dsl.jfx_live_rendering.view_model.StartWindowViewModel;

public class StartWindow extends BorderPane implements FXUtils {

    // view model
    private final StartWindowViewModel startWindowVM = new StartWindowViewModel();

    // views
    private final StartWindowBottomComponent bottomComponent = new StartWindowBottomComponent();
    private final StartWindowLeftComponent leftComponent = new StartWindowLeftComponent(startWindowVM);
    private final StartWindowCenterComponent centerComponent = new StartWindowCenterComponent(startWindowVM);

	// loading files dialog
	private final Dialog<Void> loadingDialog = createLoadingDialog();
	
	// services
	private final Service<?> classPathLoaderService = Context.getService(ClassPathLoader.class);
	private final Service<?> pomDependencyResolverService = Context.getService(PomDependencyResolver.class);

	// subscriptions
	private final Subscription serviceRunningSubscription = classPathLoaderService.runningProperty().or(pomDependencyResolverService.runningProperty()).subscribe((_, nv) -> {
		if (nv) {
			loadingDialog.show();
		} else {
			((Stage) loadingDialog.getDialogPane().getScene().getWindow()).close();
            fireEvent(new FileSystemEvents(FileSystemEvents.PATH_PROCESS_COMPLETED_EVENT));
		}
	});

	public StartWindow() {
		setBottom(bottomComponent);
		setLeft(leftComponent);
		setCenter(centerComponent);

        addEventHandler(FileSystemEvents.CLASSPATH_LOAD_REQUEST_EVENT, _ -> startWindowVM.loadClassPath(onDirectoryChooserRequest(startWindowVM.getPomXMLPath())));
        addEventHandler(FileSystemEvents.POM_XML_LOAD_REQUEST_EVENT, _ -> startWindowVM.loadPomXMLPath(onFileChooserRequest(startWindowVM.getClassPath())));
        addEventHandler(FileSystemEvents.INIT_PATH_PROCESS_EVENT, _ ->  startWindowVM.init(leftComponent.getActiveSession()));
        addEventHandler(FileSystemEvents.PATH_PROCESS_COMPLETED_EVENT, _ -> {
            centerComponent.freeResources();
            leftComponent.freeResources();
            startWindowVM.defineSessionModuleFinders();
            serviceRunningSubscription.unsubscribe();
        });
	}

	@Override
	public Scene createScene() {
		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		return new Scene(this, screenBounds.getWidth() / 2, screenBounds.getHeight() / 2);
	}
}
