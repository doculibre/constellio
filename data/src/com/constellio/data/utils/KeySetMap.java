/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.data.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class KeySetMap<K, V> implements Serializable {
	Map<K, Set<V>> map = new HashMap<>();

	public KeySetMap() {
	}

	public KeySetMap(KeySetMap<K, V> filters) {
		for (Entry<K, Set<V>> entry : filters.getMapEntries()) {
			Set<V> values = new HashSet<>(entry.getValue());
			map.put(entry.getKey(), values);
		}
	}

	public void addAll(K key, List<V> values) {
		for (V value : values) {
			add(key, value);
		}
	}

	public void add(K key, V value) {
		Set<V> values = map.get(key);
		if (values == null) {
			values = new HashSet<>();
			map.put(key, values);
		}
		values.add(value);
	}

	public void remove(V key) {
		map.remove(key);
	}

	public Set<V> get(K key) {
		Set<V> values = map.get(key);
		if (values == null) {
			values = new HashSet<>();
			map.put(key, values);
		}
		return values;
	}

	public Set<Entry<K, Set<V>>> getMapEntries() {
		return map.entrySet();
	}

	public Map<K, Set<V>> getNestedMap() {
		return map;
	}

	public void set(K key, Set<V> values) {
		map.put(key, values);
	}

	public boolean contains(K key) {
		return map.containsKey(key);
	}

	public void clear() {
		map.clear();
	}

	public void remove(V key, V value) {
		if (map.containsKey(key)) {
			map.get(key).remove(value);
		}
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public void putAll(Map<K, Set<V>> values) {
		getNestedMap().putAll(values);
	}
}
