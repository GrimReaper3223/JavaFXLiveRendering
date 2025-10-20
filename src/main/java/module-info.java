module jfxLiveRendering {
	requires transitive javafx.base;
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.web;
    requires transitive org.controlsfx.controls;
    requires com.dsl.classgen;
    requires atlantafx.base;
    requires org.jetbrains.annotations;

    exports com.dsl.jfx_live_rendering;
    exports com.dsl.jfx_live_rendering.properties.generated;
}