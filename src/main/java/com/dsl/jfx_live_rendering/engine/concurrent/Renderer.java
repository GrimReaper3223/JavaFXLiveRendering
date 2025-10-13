package com.dsl.jfx_live_rendering.engine.concurrent;

import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.Callable;

import com.dsl.jfx_live_rendering.engine.impl.CustomURLClassLoader;
import com.dsl.jfx_live_rendering.session_manager.Session;
import com.dsl.jfx_live_rendering.session_manager.SessionManager;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

public class Renderer implements Callable<Node> {

	private final Session sessionInstance;
	private final String binaryClassName;

	public Renderer(String binaryClassName) {
		this.binaryClassName = binaryClassName;
		this.sessionInstance = SessionManager.getInstance().getSession();
	}

	@Override
	public Node call() throws Exception {
		Path classpath = sessionInstance.getClassPath();
		ModuleFinder applicationModuleFinder = ModuleFinder.of(classpath);
		ModuleFinder dependenciesFinder = ModuleFinder.of(sessionInstance.getPomDependenciesPathList().toArray(Path[]::new));
		ModuleFinder composedFinder = ModuleFinder.compose(applicationModuleFinder, dependenciesFinder);

		String applicationModuleName = applicationModuleFinder.findAll().stream()
				.map(moduleRef -> moduleRef.descriptor().name())
				.findFirst()
				.orElseThrow();

		ModuleLayer layer = ModuleLayer.boot();
		Configuration config = layer.configuration().resolve(composedFinder, ModuleFinder.of(), Set.of(applicationModuleName));
		ClassLoader cl = layer.defineModulesWithOneLoader(config, new CustomURLClassLoader(classpath.toUri().toURL())).findLoader(applicationModuleName);
		FXMLLoader.setDefaultClassLoader(cl);
		Class<?> cls = cl.loadClass(binaryClassName);
		return Node.class.cast(cls.getDeclaredConstructor().newInstance());
	}
}
