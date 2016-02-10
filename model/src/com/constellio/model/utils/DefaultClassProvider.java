package com.constellio.model.utils;

public class DefaultClassProvider implements ClassProvider {

	@Override
	public Class<?> loadClass(String name) {
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
