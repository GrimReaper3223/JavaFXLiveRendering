package com.dsl.jfx_live_rendering.engine.concurrent;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.dsl.jfx_live_rendering.session_manager.SessionManager;

public class ClassPathLoader implements Callable<List<Path>> {

	@Override
	public List<Path> call() throws Exception {
		Path classPath = SessionManager.getInstance().getSession().getClassPath();
    	List<Path> files = new ArrayList<>();
		Files.walkFileTree(classPath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				files.add(classPath.relativize(file));
				return FileVisitResult.CONTINUE;
			}
		});
    	return files;
	}
}
