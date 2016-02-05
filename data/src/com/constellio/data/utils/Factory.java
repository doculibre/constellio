package com.constellio.data.utils;

import java.io.Serializable;

public interface Factory<T> extends Serializable {

	T get();

}
