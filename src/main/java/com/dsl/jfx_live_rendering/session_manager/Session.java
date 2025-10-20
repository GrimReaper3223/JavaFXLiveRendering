package com.dsl.jfx_live_rendering.session_manager;

import java.io.*;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class Session implements Externalizable {

	@Serial
    private static final long serialVersionUID = 1L;

	private String applicationModuleName;

	private transient Path classPath;
	private transient Path pomXMLPath;
	private transient List<Path> javaFXClassList;
	private transient List<Path> pomDependenciesPathList;

	private transient ModuleFinder applicationModuleFinder;
	private transient ModuleFinder dependenciesFinder;
	private transient ModuleFinder composedFinder;

	public Session() {}

	public Session(Path classPath, Path pomXMLPath) {
		this.classPath = classPath;
		this.pomXMLPath = pomXMLPath;
	}

	public void defineSessionModuleFinders() {
		this.applicationModuleFinder = ModuleFinder.of(classPath);
		this.dependenciesFinder = ModuleFinder.of(pomDependenciesPathList.toArray(Path[]::new));
		this.composedFinder = ModuleFinder.compose(applicationModuleFinder, dependenciesFinder);
		this.applicationModuleName = applicationModuleFinder.findAll().stream()
				.map(moduleRef -> moduleRef.descriptor().name())
				.findFirst()
				.orElseThrow();
	}


	public String getApplicationModuleName() {
		return applicationModuleName;
	}

	public void setApplicationModuleName(String applicationModuleName) {
		this.applicationModuleName = applicationModuleName;
	}

	public ModuleFinder getApplicationModuleFinder() {
		return applicationModuleFinder;
	}

	public void setApplicationModuleFinder(ModuleFinder applicationModuleFinder) {
		this.applicationModuleFinder = applicationModuleFinder;
	}

	public ModuleFinder getDependenciesFinder() {
		return dependenciesFinder;
	}

	public void setDependenciesFinder(ModuleFinder dependenciesFinder) {
		this.dependenciesFinder = dependenciesFinder;
	}

	public ModuleFinder getComposedFinder() {
		return composedFinder;
	}

	public void setComposedFinder(ModuleFinder composedFinder) {
		this.composedFinder = composedFinder;
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


	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(applicationModuleName);
		out.writeUTF(classPath.toString());
		out.writeUTF(pomXMLPath.toString());
		out.writeObject(javaFXClassList.stream().map(Path::toString).toArray(String[]::new));
		out.writeObject(pomDependenciesPathList.stream().map(Path::toString).toArray(String[]::new));
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.applicationModuleName = in.readUTF();
		this.classPath = Path.of(in.readUTF());
		this.pomXMLPath = Path.of(in.readUTF());
		this.javaFXClassList = Stream.of(in.readObject()).map(Object::toString).map(Path::of).toList();
		this.pomDependenciesPathList = Stream.of(in.readObject()).map(Object::toString).map(Path::of).toList();
	}
}
