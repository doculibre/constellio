package com.constellio.model.utils;

import java.io.Serializable;

public interface ClassProvider extends Serializable {

	<T> Class<T> loadClass(String name)
			throws ClassNotFoundException;

}
