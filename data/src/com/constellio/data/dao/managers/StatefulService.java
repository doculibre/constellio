package com.constellio.data.dao.managers;

import java.io.Closeable;

public interface StatefulService extends Closeable {

	void initialize();

	void close();

}
