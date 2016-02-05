package com.constellio.model.utils;

public class InstanciationUtils {

	public <T> T instanciateWithoutExpectableExceptions(Class<T> clazz) {
		if (clazz == null) {
			return null;
		}
		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new InstanciationUtilsRuntimeException(e);
		}

	}

	public <T> Class<T> loadClassWithoutExpectableExceptions(String classname) {
		try {
			return (Class<T>) Class.forName(classname);
		} catch (ClassNotFoundException e) {
			throw new InstanciationUtilsRuntimeException(e);
		}
	}

	public <T> T instanciate(String classname) {
		Class<T> clazz = loadClassWithoutExpectableExceptions(classname);
		return instanciateWithoutExpectableExceptions(clazz);
	}
}
