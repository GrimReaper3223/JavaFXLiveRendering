module javaFXLiveRendering {
	requires transitive javafx.base;
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires transitive org.controlsfx.controls;
    requires transitive com.dsl.classgen;

    exports com.dsl.jfx_live_rendering;
    exports com.dsl.jfx_live_rendering.properties.generated;
}