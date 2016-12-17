package com.constellio.data.utils;

public interface Provider<I, O> {

	O get(I input);

}
