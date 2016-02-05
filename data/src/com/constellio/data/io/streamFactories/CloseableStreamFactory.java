package com.constellio.data.io.streamFactories;

import java.io.Closeable;
import java.io.IOException;

public interface CloseableStreamFactory<T extends Closeable> extends StreamFactory<T>, Closeable {

	@Override
	void close()
			throws IOException;

	long length();
}
