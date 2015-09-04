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
package com.constellio.model.entities.structures;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.ModifiableStructure;

public class MapStringStringStructure implements ModifiableStructure, Map<String, String> {

	Map<String, String> stringStringMap;
	boolean dirty;

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
	public String toString() {
		return "MapStringString{" +
				"stringStringMap=" + stringStringMap +
				", dirty=" + dirty +
				'}';
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
}
