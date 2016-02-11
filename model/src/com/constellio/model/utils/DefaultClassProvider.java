package com.constellio.model.utils;

public class DefaultClassProvider implements ClassProvider {

	@Override
	public <T> Class<T> loadClass(String name)
			throws ClassNotFoundException {

		return (Class<T>) Class.forName(name);
	}

}
