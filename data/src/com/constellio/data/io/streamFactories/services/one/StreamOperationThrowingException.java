package com.constellio.data.io.streamFactories.services.one;

import java.io.Closeable;

public interface StreamOperationThrowingException<F extends Closeable, E extends Exception> {

	void execute(F stream)
			throws E;

}
