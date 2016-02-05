package com.constellio.data.utils;

public class Counter {

	private static Counter global;

	private long current = 0;

	public static synchronized Counter global() {
		if (global == null) {
			global = new Counter();
		}
		return global;
	}

	public long current() {
		return current;
	}

	public long increment() {
		return ++current;
	}

	public void reset() {
		current = 0;
	}

}
