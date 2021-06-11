package com.constellio.model.utils;

public abstract class Lazy<T> {

	boolean loaded;
	T value;

	public T get() {
		if (!loaded) {
			value = load();
			loaded = true;
		}
		return value;
	}

	protected abstract T load();

	public boolean isLoaded() {
		return loaded;
	}
}
