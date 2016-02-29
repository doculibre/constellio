package com.constellio.data.io.streamFactories;

import java.io.Closeable;

public interface StreamFactoryWithFilename<T extends Closeable> extends StreamFactory<T>, Closeable {

	String getFilename();

}
