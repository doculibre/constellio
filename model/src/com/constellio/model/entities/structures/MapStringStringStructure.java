package com.constellio.model.entities.structures;

import com.constellio.model.entities.schemas.ModifiableStructure;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapStringStringStructure implements ModifiableStructure, Map<String, String> {

	protected Map<String, String> stringStringMap;
	protected boolean dirty;

	public MapStringStringStructure() {
		stringStringMap = new HashMap<>();
	}

	public MapStringStringStructure(Map<String, String> parameters) {
		this.stringStringMap = parameters;
		dirty = false;
	}

	public MapStringStringStructure with(String key, String value) {
		put(key, value);
		return this;
	}

	@Override
	public String put(String key, String value) {
		dirty = true;
		return stringStringMap.put(key, value);
	}

	@Override
	public String remove(Object key) {
		dirty = true;
		return stringStringMap.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> m) {
		dirty = true;
		stringStringMap.putAll(m);
	}

	@Override
	public void clear() {
		dirty = true;
		stringStringMap.clear();
	}

	@Override
	public Set<String> keySet() {
		return stringStringMap.keySet();
	}

	@Override
	public Collection<String> values() {
		return stringStringMap.values();
	}

	@Override
	public Set<Entry<String, String>> entrySet() {
		return stringStringMap.entrySet();
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, "dirty");
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "dirty");
	}

	@Override
	public int size() {
		return stringStringMap.size();
	}

	@Override
	public boolean isEmpty() {
		return stringStringMap.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return stringStringMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return stringStringMap.containsValue(value);
	}

	@Override
	public String get(Object key) {
		return stringStringMap.get(key);
	}

	@Override
	public String toString() {
		return "MapStringString{" +
			   "stringStringMap=" + stringStringMap +
			   ", dirty=" + dirty +
			   '}';
	}
}
