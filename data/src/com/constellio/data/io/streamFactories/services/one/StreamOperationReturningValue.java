package com.constellio.data.io.streamFactories.services.one;

import java.io.Closeable;

public interface StreamOperationReturningValue<F extends Closeable, R> {

	R execute(F inputStream);

}
