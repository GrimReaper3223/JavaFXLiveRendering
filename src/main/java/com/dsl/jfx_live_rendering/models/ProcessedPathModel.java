package com.dsl.jfx_live_rendering.models;

import com.dsl.jfx_live_rendering.session_manager.SessionManager;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class ProcessedPathModel {

    private final Path path;
	private final String fileName;
	private final String binaryFileName;

	public <T> ProcessedPathModel(T classFile) {
		path = SessionManager.getInstance().getSession().getClassPath().relativize(Path.of(classFile.toString()));
		String[] splitteredPathArray = path.toString().split("[./]");
		List<String> splitteredPath = Arrays.asList(Arrays.copyOfRange(splitteredPathArray, 0, splitteredPathArray.length - 1));
		this.fileName = splitteredPath.getLast();
		this.binaryFileName = String.join(".", splitteredPath);
	}

	public String getFileName() {
		return fileName;
	}

	public String getBinaryFileName() {
		return binaryFileName;
	}

    public Path getPath() { return path; }
}
