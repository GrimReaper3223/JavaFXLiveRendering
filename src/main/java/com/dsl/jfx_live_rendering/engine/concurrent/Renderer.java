package com.dsl.jfx_live_rendering.engine.concurrent;

import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.Callable;

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
		String applicationModuleName = ModuleFinder.of(sessionInstance.getClassPath()).findAll().stream()
				.map(moduleRef -> moduleRef.descriptor().name())
				.findFirst()
				.orElseThrow();

		CustomURLClassLoader cl = new CustomURLClassLoader();
		ModuleFinder dependenciesFinder = ModuleFinder.of(sessionInstance.getPomDependenciesPathList().toArray(Path[]::new));
		ModuleLayer layer = ModuleLayer.boot();
		Configuration config = layer.configuration().resolve(dependenciesFinder, ModuleFinder.of(), Set.of(applicationModuleName));
		layer.defineModulesWithOneLoader(config, cl);
		FXMLLoader.setDefaultClassLoader(cl);
		Class<?> cls = cl.loadClass(binaryClassName);
		return Node.class.cast(cls.getDeclaredConstructor().newInstance());
	}

	private class CustomURLClassLoader extends URLClassLoader {

		public CustomURLClassLoader() throws MalformedURLException {
			super(new URL[] { sessionInstance.getClassPath().toUri().toURL() });
		}

		@Override
		protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			Class<?> c = findLoadedClass(name);
			if (c == null) {
				try {
					c = findClass(name);
				} catch (ClassNotFoundException _) {
					// not found in this classloader, delegate to parent
					c = super.loadClass(name, resolve);
				}
			}
			if (resolve) {
				resolveClass(c);
			}
			return c;
		}
	}
}
