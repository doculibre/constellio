package com.constellio.model.utils;

public interface ClassProvider {

	<T> Class<T> loadClass(String name)
			throws ClassNotFoundException;

}
