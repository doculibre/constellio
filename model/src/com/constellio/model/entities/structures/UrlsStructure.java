package com.constellio.model.entities.structures;

import com.constellio.model.entities.schemas.ModifiableStructure;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UrlsStructure implements ModifiableStructure, Map<String, String> {

	public static final String CATEGORY_KEY = "category";
	public static final String DESCRIPTION_KEY = "description";
	public static final String URL_KEY = "url";

	protected Map<String, String> stringStringMap;
	protected boolean dirty;

	public UrlsStructure() {
		stringStringMap = new HashMap<>();
	}

	public UrlsStructure(Map<String, String> parameters) {
		this.stringStringMap = parameters;
		dirty = false;
	}

	public UrlsStructure with(String name, String url) {
		put(name, url);
		return this;
	}

	@Override
	public String put(String name, String url) {
		dirty = true;
		return stringStringMap.put(name, url);
	}

	@Override
	public String remove(Object name) {
		dirty = true;
		return stringStringMap.remove(name);
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
	public boolean containsKey(Object name) {
		return stringStringMap.containsKey(name);
	}

	@Override
	public boolean containsValue(Object url) {
		return stringStringMap.containsValue(url);
	}

	@Override
	public String get(Object name) {
		return stringStringMap.get(name);
	}

	@Override
	public String toString() {
		return "UrlsStructure{" +
			   "stringStringMap=" + stringStringMap +
			   ", dirty=" + dirty +
			   '}';
	}
}
