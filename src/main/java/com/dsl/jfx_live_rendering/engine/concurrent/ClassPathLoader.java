package com.dsl.jfx_live_rendering.engine.concurrent;

import com.dsl.jfx_live_rendering.engine.impl.CustomURLClassLoader;
import com.dsl.jfx_live_rendering.engine.impl.ExceptionHandlerImpl;
import com.dsl.jfx_live_rendering.models.ProcessedPathModel;
import com.dsl.jfx_live_rendering.session_manager.SessionManager;
import javafx.scene.Node;
import org.jetbrains.annotations.NotNull;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ClassPathLoader implements Callable<List<Path>> {

	private CustomURLClassLoader cl;

	@Override
	public List<Path> call() throws Exception {
    	List<Path> files = new ArrayList<>();
    	Path classPath = SessionManager.getInstance().getSession().getClassPath();
    	cl = new CustomURLClassLoader(classPath.toUri().toURL());

		Files.walkFileTree(classPath, new SimpleFileVisitor<>() {
            @Override
			public @NotNull FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) {
				try {
					if(classNodeValidator(file)) {
						files.add(file);
					}
				} catch (ClassNotFoundException e) {
					ExceptionHandlerImpl.logException(e);
				}
				return FileVisitResult.CONTINUE;
			}
		});
    	return files;
	}

	private boolean classNodeValidator(Path filePath) throws ClassNotFoundException {
		boolean result = false;
		String stringFile = filePath.toString();
		if(stringFile.endsWith(".class") && !stringFile.contains("module-info")) {
			Class<?> cls = cl.loadClass(new ProcessedPathModel(filePath).getBinaryFileName());
			result = Node.class.isAssignableFrom(cls);
		}
		return result;
	}
}
