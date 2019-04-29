package com.constellio.data.utils;

public class Holder<T> {

	T holded;


	public Holder() {
	}

	public Holder(T holded) {
		this.holded = holded;
	}

	public T get() {
		return holded;
	}

	public Holder<T> set(T holded) {
		this.holded = holded;
		return this;
	}
}
