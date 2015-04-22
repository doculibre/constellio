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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RecordDeltaDTO implements RecordsOperationDTO {

	private final String id;

	private final long fromVersion;

	private final Map<String, Object> modifiedFields;

	private final Map<String, Object> initialFields;

	private final Map<String, Object> copyfields;

	//TODO Remove initialFields param, replacing it with recordDTO
	public RecordDeltaDTO(RecordDTO recordDTO, Map<String, Object> modifiedFields, Map<String, Object> initialFields) {
		this(recordDTO, modifiedFields, initialFields, new HashMap<String, Object>());
	}

	//TODO Remove initialFields param, replacing it with recordDTO
	public RecordDeltaDTO(RecordDTO recordDTO, Map<String, Object> modifiedFields, Map<String, Object> initialFields,
			Map<String, Object> copyfields) {
		super();
		this.initialFields = initialFields;
		this.copyfields = copyfields;
		this.id = recordDTO.getId();
		this.fromVersion = recordDTO.getVersion();
		this.modifiedFields = Collections.unmodifiableMap(modifiedFields);
	}

	public RecordDeltaDTO(String id, long fromVersion, Map<String, Object> modifiedFields, Map<String, Object> initialFields) {
		this(id, fromVersion, modifiedFields, initialFields, new HashMap<String, Object>());
	}

	public RecordDeltaDTO(String id, long fromVersion, Map<String, Object> modifiedFields, Map<String, Object> initialFields,
			Map<String, Object> copyfields) {
		super();
		this.id = id;
		this.copyfields = copyfields;
		this.fromVersion = fromVersion;
		this.initialFields = initialFields;
		this.modifiedFields = Collections.unmodifiableMap(modifiedFields);
	}

	public String getId() {
		return id;
	}

	public long getFromVersion() {
		return fromVersion;
	}

	public Map<String, Object> getModifiedFields() {
		return modifiedFields;
	}

	public Map<String, Object> getInitialFields() {
		return initialFields;
	}

	public Map<String, Object> getCopyfields() {
		return copyfields;
	}

	public <T> T get(String field) {

		if (modifiedFields.containsKey(field)) {
			return (T) modifiedFields.get(field);

		} else if (initialFields.containsKey(field)) {
			return (T) initialFields.get(field);

		} else if (copyfields.containsKey(field)) {
			return (T) copyfields.get(field);

		} else {
			return null;
		}

	}
}
