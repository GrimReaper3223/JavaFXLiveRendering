package com.dsl.jfx_live_rendering.engine.concurrent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import com.dsl.jfx_live_rendering.session_manager.SessionManager;

public class PomDependencyResolver implements Callable<List<Path>> {

	@Override
	public List<Path> call() throws Exception {
		Path tempFilePath = Files.createTempFile("maven-deps", ".txt");
		String cmd = String.format("cd %s; mvn dependency:build-classpath -Dmdep.outputFile=%s", SessionManager.getInstance().getSession().getPomXMLPath().getParent().toString(), tempFilePath);
		ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
		pb.start().waitFor();

		return Files.readAllLines(tempFilePath).stream()
				.dropWhile(str -> str.contains("[INFO] Dependencies claspath:"))
				.takeWhile(str -> !str.contains("--------------------"))
				.flatMap(str -> Arrays.asList(str.split(":")).stream())
				.map(Path::of)
				.toList();
	}
}
