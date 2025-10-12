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
		String applicationModuleName = ModuleFinder.of(classpath).findAll().stream()
				.map(moduleRef -> moduleRef.descriptor().name())
				.findFirst()
				.orElseThrow();

		CustomURLClassLoader cl = new CustomURLClassLoader(classpath.toUri().toURL());
		ModuleFinder dependenciesFinder = ModuleFinder.of(sessionInstance.getPomDependenciesPathList().toArray(Path[]::new));
		ModuleLayer layer = ModuleLayer.boot();
		Configuration config = layer.configuration().resolve(dependenciesFinder, ModuleFinder.of(), Set.of(applicationModuleName));
		layer.defineModulesWithOneLoader(config, cl);
		FXMLLoader.setDefaultClassLoader(cl);
		Class<?> cls = cl.loadClass(binaryClassName);
		return Node.class.cast(cls.getDeclaredConstructor().newInstance());
	}
}
