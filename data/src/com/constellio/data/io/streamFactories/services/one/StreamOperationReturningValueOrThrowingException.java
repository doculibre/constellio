package com.constellio.data.io.streamFactories.services.one;

import java.io.Closeable;

public interface StreamOperationReturningValueOrThrowingException<F extends Closeable, R, E extends Exception> {

	R execute(F inputStream)
			throws E;

}
