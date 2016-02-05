package com.constellio.data.frameworks.extensions;

public interface VaultEventListener<T> {

	public void notify(T event);

}
