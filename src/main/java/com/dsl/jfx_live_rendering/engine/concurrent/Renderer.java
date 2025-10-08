package com.dsl.jfx_live_rendering.engine.concurrent;

import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.Callable;

import com.dsl.jfx_live_rendering.engine.impl.CustomClassLoader;
import com.dsl.jfx_live_rendering.session_manager.Session;
import com.dsl.jfx_live_rendering.session_manager.SessionManager;

import javafx.scene.Node;

public class Renderer implements Callable<Node> {

	private final String binaryClassName;

	public Renderer(String binaryClassName) {
		this.binaryClassName = binaryClassName;
	}

	@Override
	public Node call() throws Exception {
		Session sessionInstance = SessionManager.getInstance().getSession();
		String applicationModuleName = ModuleFinder.of(sessionInstance.getClassPath()).findAll().stream()
				.map(moduleRef -> moduleRef.descriptor().name())
				.findFirst()
				.orElseThrow();

		CustomClassLoader cl = new CustomClassLoader();

		ModuleFinder dependenciesFinder = ModuleFinder.of(sessionInstance.getPomDependenciesPathList().toArray(Path[]::new));
		ModuleLayer layer = ModuleLayer.boot();
		Configuration config = layer.configuration().resolve(dependenciesFinder, ModuleFinder.of(), Set.of(applicationModuleName));

		ClassLoader loader = layer.defineModulesWithOneLoader(config, cl).findLoader(applicationModuleName);
		Class<?> cls = loader.loadClass(binaryClassName);

		return Node.class.cast(cls.getDeclaredConstructor().newInstance());
	}
}
