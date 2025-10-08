package com.dsl.jfx_live_rendering.engine.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import com.dsl.jfx_live_rendering.session_manager.SessionManager;

public class CustomClassLoader extends ClassLoader {

    @Override
    protected Class<?> findClass(String binaryClassName) throws ClassNotFoundException {
    	byte[] classData = loadClassData();
    	if (classData == null || classData.length == 0) {
			throw new ClassNotFoundException("Class data for '" + binaryClassName + "' not found");
		}
    	return defineClass(binaryClassName, classData, 0, classData.length);
    }

    private byte[] loadClassData() {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	try(InputStream in = Files.newInputStream(SessionManager.getInstance().getSession().getClassPath())) {
    		int i;
    		while((i = in.read()) != 0) {
    			baos.write(i);
    		}
    	} catch (IOException e) {
			e.printStackTrace();
		}
    	return baos.toByteArray();
    }
}
