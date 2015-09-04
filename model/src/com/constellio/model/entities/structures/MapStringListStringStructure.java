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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.ModifiableStructure;

public class MapStringListStringStructure implements ModifiableStructure, Map<String, List<String>> {

	Map<String, List<String>> stringListMap;
	boolean dirty;

	public MapStringListStringStructure() {
		stringListMap = new HashMap<>();
	}

	public MapStringListStringStructure(Map<String, List<String>> parameters) {
		this.stringListMap = parameters;
		dirty = false;
	}

	@Override
	public List<String> put(String key, List<String> values) {
		dirty = true;
		return stringListMap.put(key, values);
	}

	@Override
	public List<String> remove(Object key) {
		dirty = true;
		return stringListMap.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends List<String>> m) {
		dirty = true;
		stringListMap.putAll(m);
	}

	@Override
	public void clear() {
		dirty = true;
		stringListMap.clear();
	}

	@Override
	public Set<String> keySet() {
		return stringListMap.keySet();
	}

	@Override
	public Collection<List<String>> values() {
		return stringListMap.values();
	}

	@Override
	public Set<Entry<String, List<String>>> entrySet() {
		return stringListMap.entrySet();
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public String toString() {
		return "MapStringListString{" +
				"stringListMap=" + stringListMap +
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
		return stringListMap.size();
	}

	@Override
	public boolean isEmpty() {
		return stringListMap.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return stringListMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return stringListMap.containsValue(value);
	}

	@Override
	public List<String> get(Object key) {
		return stringListMap.get(key);
	}
}
