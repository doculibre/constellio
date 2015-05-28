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
package com.constellio.model.services.records.bulkImport.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportData {

	private int index;

	private String legacyId;

	private String schema;

	private Map<String, Object> fields;

	public ImportData(int index, String schema, String legacyId, Map<String, Object> fields) {
		this.legacyId = legacyId;
		this.index = index;
		this.fields = fields;
		this.schema = schema;
	}

	public String getLegacyId() {
		return legacyId;
	}

	public int getIndex() {
		return index;
	}

	public String getSchema() {
		return schema;
	}

	public Map<String, Object> getFields() {
		return Collections.unmodifiableMap(fields);
	}

	public <T> T getValue(String key) {
		return (T) fields.get(key);
	}

	public <K, V> Map<K, V> getMap(String key) {
		Map<K, V> values = getValue(key);
		return values == null ? new HashMap<K, V>() : values;
	}

	public <V> List<V> getList(String key) {
		List<V> values = getValue(key);
		return values == null ? new ArrayList<V>() : values;
	}

}
