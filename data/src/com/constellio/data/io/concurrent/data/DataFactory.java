package com.constellio.data.io.concurrent.data;

public interface DataFactory<T extends DataWrapper<?>> {

	public T makeInstance();
}
