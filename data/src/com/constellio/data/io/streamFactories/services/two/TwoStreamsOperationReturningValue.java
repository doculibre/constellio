package com.constellio.data.io.streamFactories.services.two;

import java.io.Closeable;

public interface TwoStreamsOperationReturningValue<F extends Closeable, S extends Closeable, R> {

	R execute(F firstStream, S secondStream);

}
