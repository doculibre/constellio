package com.constellio.data.io.streamFactories.services.two;

import java.io.Closeable;

public interface TwoStreamsOperationReturningValueOrThrowingException<F extends Closeable, S extends Closeable, R, E extends Exception> {

	R execute(F firstStream, S secondStream)
			throws E;

}
