package com.constellio.data.utils;

public class FactoryWithCache<T> implements Factory
													<T> {
	private boolean calculated;
	private T cachedValue;

	private Factory<T> nestedFactory;

	public FactoryWithCache(Factory<T> nestedFactory) {
		this.nestedFactory = nestedFactory;
	}

	@Override
	public T get() {

		synchronized (this) {
			if (calculated) {
				return cachedValue;
			} else {
				cachedValue = nestedFactory.get();
				calculated = true;
				return cachedValue;
			}
		}
	}
}
