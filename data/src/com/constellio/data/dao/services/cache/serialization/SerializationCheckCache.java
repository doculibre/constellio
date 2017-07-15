package com.constellio.data.dao.services.cache.serialization;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.SerializationException;
import org.apache.commons.lang3.SerializationUtils;

import com.constellio.data.dao.services.cache.map.ConstellioMapCache;
import com.constellio.data.utils.serialization.ConstellioSerializationUtils;

public class SerializationCheckCache extends ConstellioMapCache {
	
	private Map<String, Object> deserializedCache = new LinkedHashMap<>();

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
		value = (T) deserializedCache.get(deserializedKey);
		if (value == null) {
			byte[] valueBytes = super.get(key);
			if (valueBytes != null) {
				value = (T) SerializationUtils.deserialize(valueBytes);
			} else {
				value = null;
			}
			deserializedCache.put(deserializedKey, value);
		}
		return value;
	}

	@Override
	public <T extends Serializable> void put(String key, T value) {
		String deserializedKey = deserializedKey(key);
		deserializedCache.remove(deserializedKey);
		if (value != null) {
			try {
				byte[] valueBytes = SerializationUtils.serialize(value);
				System.out.println("Serialized size for " + key + ": " + readableFileSize(valueBytes.length));
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
		deserializedCache.remove(deserializedKey);
		super.remove(key);
	}
	
	@Override
	public void clear() {
		deserializedCache.clear();
		super.clear();
	}

	/**
	 * Source: https://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc
	 */
	public static String readableFileSize(long size) {
	    if(size <= 0) return "0";
	    final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}

}
