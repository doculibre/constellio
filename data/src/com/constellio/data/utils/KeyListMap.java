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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class KeyListMap<K, V> implements Serializable {
	Map<K, List<V>> map = new HashMap<>();

	public void addAll(K key, List<V> values) {
		for (V value : values) {
			add(key, value);
		}
	}

	public void add(K key, V value) {
		List<V> values = map.get(key);
		if (values == null) {
			values = new ArrayList<>();
			map.put(key, values);
		}
		values.add(value);
	}

	public void remove(V key) {
		map.remove(key);
	}

	public List<V> get(K key) {
		List<V> values = map.get(key);
		if (values == null) {
			values = new ArrayList<>();
		}
		return values;
	}

	public Set<Entry<K, List<V>>> getMapEntries() {
		return map.entrySet();
	}

	public Map<K, List<V>> getNestedMap() {
		return map;
	}

	public void set(K key, List<V> values) {
		map.put(key, values);
	}

	public boolean contains(K key) {
		return map.containsKey(key);
	}

	public void clear() {
		map.clear();
	}
}
