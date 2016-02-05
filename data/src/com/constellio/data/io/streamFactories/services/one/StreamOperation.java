package com.constellio.data.io.streamFactories.services.one;

import java.io.Closeable;

public interface StreamOperation<F extends Closeable> {

	void execute(F stream);

}
