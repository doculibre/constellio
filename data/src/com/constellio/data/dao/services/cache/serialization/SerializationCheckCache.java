package com.constellio.data.dao.services.cache.serialization;

import java.io.Serializable;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;

import com.constellio.data.dao.services.cache.map.ConstellioMapCache;
import com.constellio.data.utils.serialization.ConstellioSerializationUtils;

public class SerializationCheckCache extends ConstellioMapCache {

	public SerializationCheckCache(String name) {
		super(name);
	}
	
	private String deserializedKey(String key) {
		return key + ".deserialized";
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> T get(String key) {
		T value;
		String deserializedKey = deserializedKey(key);
		value = super.get(deserializedKey);
		if (value == null) {
			byte[] valueBytes = super.get(key);
			if (valueBytes != null) {
				value = (T) SerializationUtils.deserialize(valueBytes);
			} else {
				value = null;
			}
			super.put(deserializedKey, value);
		}
		return value;
	}

	@Override
	public <T extends Serializable> void put(String key, T value) {
		String deserializedKey = deserializedKey(key);
		super.remove(deserializedKey);
		if (value != null) {
			try {
				byte[] valueBytes = SerializationUtils.serialize(value);
				super.put(key, valueBytes);
			} catch (SerializationException e) {
				ConstellioSerializationUtils.validateSerializable(value);
				throw e;
			} 
		} else {
			super.put(key, null);
		}
	}

	@Override
	public void remove(String key) {
		String deserializedKey = deserializedKey(key);
		super.remove(deserializedKey);
		super.remove(key);
	}

}
