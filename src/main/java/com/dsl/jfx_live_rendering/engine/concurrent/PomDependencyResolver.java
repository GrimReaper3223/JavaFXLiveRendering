package com.dsl.jfx_live_rendering.engine.concurrent;

import com.dsl.jfx_live_rendering.engine.impl.ExceptionHandlerImpl;
import com.dsl.jfx_live_rendering.session_manager.SessionManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class PomDependencyResolver implements Callable<List<Path>> {

	@Override
	public List<Path> call() throws Exception {
		Path depsFile = Path.of("deps.txt");
		Files.deleteIfExists(depsFile);
		Files.createFile(depsFile);
		Process proc = new ProcessBuilder("bash", "-c", String.format("mvn dependency:build-classpath -Dmdep.outputFile=%s", depsFile))
				.directory(SessionManager.getInstance().getSession().getPomXMLPath().getParent().toFile())
				.inheritIO()
				.start();

		if (proc.waitFor() != 0) {
			throw new IOException("Failed to resolve dependencies using Maven. Verify that the environment variables required to run MVN command are set correctly.");
		}

        return Files.readAllLines(depsFile).stream()
                .flatMap(str -> Arrays.stream(str.split(":")))
                .map(Path::of)
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
                    try {
                        Files.delete(depsFile);
                    } catch (IOException e) {
                        ExceptionHandlerImpl.logException(e);
                    }
                    return list;
                }));
    }
}
