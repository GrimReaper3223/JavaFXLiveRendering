package com.dsl.jfx_live_rendering.models;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProcessedPathModel {

	private final String fileName;
	private final String binaryFileName;

	public <T> ProcessedPathModel(T classFile) {
		String[] splitteredPathArray = classFile.toString().split("[\\./]");
		List<String> splitteredPath = Arrays.asList(Arrays.copyOfRange(splitteredPathArray, 0, splitteredPathArray.length - 1));
		this.fileName = splitteredPath.getLast();
		this.binaryFileName = splitteredPath.stream().collect(Collectors.joining("."));
	}

	public String getFileName() {
		return fileName;
	}

	public String getBinaryFileName() {
		return binaryFileName;
	}
}
