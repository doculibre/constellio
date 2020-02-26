package com.constellio.app.modules.tasks.navigation;

import com.constellio.data.utils.TimedCache;
import org.joda.time.Duration;

//todo: déplacer au bon endroit
public class TemporaryParameters {
	TimedCache timedCache;

	public TemporaryParameters(String key, Duration duration) {
		timedCache = new TimedCache(duration);
	}

	public void addParam(String key, Object value) {
		timedCache.insert(key, value);
	}
}
