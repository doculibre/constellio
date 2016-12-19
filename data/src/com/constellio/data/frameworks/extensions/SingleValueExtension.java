package com.constellio.data.frameworks.extensions;

public class SingleValueExtension<T> {

	public static int DEFAULT_VALUE = 0;

	int currentOrder = -1;

	T value;

	public void register(int order, T value) {
		if (this.currentOrder < order) {
			this.value = value;
		}

	}

	public void register(T value) {
		this.value = value;
		this.currentOrder++;
	}

	public T getValue() {
		return value;
	}
}
