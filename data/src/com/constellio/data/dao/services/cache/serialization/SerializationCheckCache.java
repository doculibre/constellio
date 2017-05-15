package com.constellio.data.dao.services.cache.serialization;

import java.io.Serializable;

import com.constellio.data.dao.services.cache.map.ConstellioMapCache;
import com.constellio.data.utils.serialization.ConstellioSerializationUtils;

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
