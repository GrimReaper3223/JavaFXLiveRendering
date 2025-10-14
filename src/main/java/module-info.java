module javaFXLiveRendering {
	requires transitive javafx.base;
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.web;
    requires transitive org.controlsfx.controls;
    requires transitive com.dsl.classgen;

    exports com.dsl.jfx_live_rendering;
    exports com.dsl.jfx_live_rendering.properties.generated;
    opens com.dsl.jfx_live_rendering.session_manager to javafx.base;
}