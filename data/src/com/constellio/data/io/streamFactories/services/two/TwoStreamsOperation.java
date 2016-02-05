package com.constellio.data.io.streamFactories.services.two;

import java.io.Closeable;

public interface TwoStreamsOperation<F extends Closeable, S extends Closeable> {

	void execute(F firstStream, S secondStream);

}
