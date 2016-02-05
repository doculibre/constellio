package com.constellio.sdk.tests.concurrent;

import java.util.HashMap;
import java.util.Map;

public abstract class ConcurrentJob {

	public Map<String, Object> setupWorkerContext(int worker) {
		return new HashMap<String, Object>();
	}

	public abstract void run(Map<String, Object> context, int worker)
			throws Exception;

}
