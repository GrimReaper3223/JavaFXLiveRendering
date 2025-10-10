package com.dsl.jfx_live_rendering.session_manager;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Session implements Serializable {

	private static final long serialVersionUID = 1L;

	private Path classPath;
	private Path pomXMLPath;
	private List<Path> javaFXClassList = new ArrayList<>();
	private List<Path> pomDependenciesPathList = new ArrayList<>();

	public Session(Path classPath, Path pomXMLPath) {
		this.classPath = classPath;
		this.pomXMLPath = pomXMLPath;
	}

	public Path getClassPath() {
		return classPath;
	}

	public void setClassPath(Path classPath) {
		this.classPath = classPath;
	}

	public Path getPomXMLPath() {
		return pomXMLPath;
	}

	public void setPomXMLPath(Path pomXMLPath) {
		this.pomXMLPath = pomXMLPath;
	}

	public List<Path> getJavaFXClassList() {
		return javaFXClassList;
	}

	public void setJavaFXClassList(List<Path> javaFXClassList) {
		this.javaFXClassList = javaFXClassList;
	}

	public List<Path> getPomDependenciesPathList() {
		return pomDependenciesPathList;
	}

	public void setPomDependenciesPathList(List<Path> pomDependenciesPathList) {
		this.pomDependenciesPathList = pomDependenciesPathList;
	}
}
