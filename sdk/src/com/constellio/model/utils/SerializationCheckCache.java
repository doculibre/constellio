package com.constellio.model.utils;

import java.io.Serializable;

import com.constellio.app.utils.ConstellioSerializationUtils;
import com.constellio.data.dao.services.cache.map.ConstellioMapCache;

public class SerializationCheckCache extends ConstellioMapCache {

	public SerializationCheckCache(String name) {
		super(name);
	}

	@Override
	public <T extends Serializable> void put(String key, T value) {
		ConstellioSerializationUtils.validateSerializable(value);
		super.put(key, value);
	}

}
