package com.dsl.jfx_live_rendering.engine.concurrent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import com.dsl.jfx_live_rendering.session_manager.SessionManager;

public class PomDependencyResolver implements Callable<List<Path>> {

	/*
	 * TODO: adicionar posteriormente uma verificacao que checa se a variavel que detem a
	 * home do maven esta configurada. Caso nao esteja, o servico nao lanca uma excecao.
	 * Ele apenas nao achara o comando 'mvn' e retornara uma lista vazia.
	 *
	 * TODO: O depsFile deve fazer parte do diretorio da sessao atual carregada.
	 */
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
			throw new RuntimeException("Failed to resolve dependencies using Maven.");
		}

		return Files.readAllLines(depsFile).stream()
				.flatMap(str -> Arrays.asList(str.split(":")).stream())
				.map(Path::of)
				.toList();
	}
}
