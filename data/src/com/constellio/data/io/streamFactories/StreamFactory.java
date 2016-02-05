package com.constellio.data.io.streamFactories;

import java.io.Closeable;
import java.io.IOException;

public interface StreamFactory<T extends Closeable> {

	T create(String name)
			throws IOException;

}
