package com.constellio.data.io.streamFactories.services.two;

import java.io.Closeable;

public interface TwoStreamsOperationThrowingException<F extends Closeable, S extends Closeable, E extends Exception> {

	void execute(F firstStream, S secondStream)
			throws E;

}
