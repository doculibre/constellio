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
package com.constellio.data.dao.dto.records;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.data.dao.services.bigVault.RecordDaoRuntimeException;

public class RecordDTO implements RecordsOperationDTO, Serializable {

	private final String id;

	private final long version;

	private final List<String> loadedFields;

	private final Map<String, Object> fields;

	private final Map<String, Object> copyfields;

	public RecordDTO(String id, Map<String, Object> fields) {
		this(id, 0, null, fields, new HashMap<String, Object>());
	}

	public RecordDTO(String id, long version, List<String> loadedFields, Map<String, Object> fields) {
		this(id, version, loadedFields, fields, new HashMap<String, Object>());
	}

	public RecordDTO(String id, long version, List<String> loadedFields, Map<String, Object> fields,
			Map<String, Object> copyfields) {
		super();
		if (id == null) {
			throw new RecordDaoRuntimeException("DTO Cannot have a null id");
		}

		this.id = id;
		this.version = version;
		this.loadedFields = loadedFields == null ? null : Collections.unmodifiableList(loadedFields);
		this.fields = Collections.unmodifiableMap(fields);
		this.copyfields = Collections.unmodifiableMap(copyfields);
	}

	public String getId() {
		return id;
	}

	public long getVersion() {
		return version;
	}

	public List<String> getLoadedFields() {
		return loadedFields;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public Map<String, Object> getCopyFields() {
		return copyfields;
	}

	public RecordDTO createCopyWithDelta(RecordDeltaDTO recordDeltaDTO) {
		Map<String, Object> newFields = new HashMap<>(fields);
		newFields.putAll(recordDeltaDTO.getModifiedFields());

		Map<String, Object> copyFields = new HashMap<>(copyfields);
		copyFields.putAll(recordDeltaDTO.getCopyfields());

		return new RecordDTO(id, version, loadedFields, newFields, copyFields);
	}

	public RecordDTO withVersion(long version) {
		return new RecordDTO(id, version, loadedFields, fields, copyfields);
	}
}
