package com.dsl.jfx_live_rendering.engine.impl;

import java.net.URL;
import java.net.URLClassLoader;

public class CustomURLClassLoader extends URLClassLoader {

	public CustomURLClassLoader(URL classPathURL) {
		super(new URL[] { classPathURL });
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
