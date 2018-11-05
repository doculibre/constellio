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

	private final Map<String, Double> incrementedFields;

	//TODO Remove initialFields param, replacing it with recordDTO
	public RecordDeltaDTO(RecordDTO recordDTO, Map<String, Object> modifiedFields) {
		this(recordDTO, modifiedFields, recordDTO.getFields(), new HashMap<String, Object>());
	}

	//TODO Remove initialFields param, replacing it with recordDTO
	public RecordDeltaDTO(RecordDTO recordDTO, Map<String, Object> modifiedFields, Map<String, Object> initialFields) {
		this(recordDTO, modifiedFields, initialFields, new HashMap<String, Object>());
	}

	//TODO Remove initialFields param, replacing it with recordDTO
	public RecordDeltaDTO(RecordDTO recordDTO, Map<String, Object> modifiedFields, Map<String, Object> initialFields,
						  Map<String, Object> copyfields) {
		this(recordDTO, modifiedFields, initialFields, copyfields, new HashMap<String, Double>());
	}

	public RecordDeltaDTO(RecordDTO recordDTO, Map<String, Object> modifiedFields,
						  Map<String, Object> initialFields, Map<String, Object> copyfields,
						  Map<String, Double> incrementedFields) {
		this(recordDTO.getId(), recordDTO.getVersion(), modifiedFields, initialFields, copyfields, incrementedFields);
	}

	public RecordDeltaDTO(String id, long fromVersion) {
		this(id, fromVersion, new HashMap<String, Object>());
	}

	public RecordDeltaDTO(String id, long fromVersion, Map<String, Object> modifiedFields) {
		this(id, fromVersion, modifiedFields, null);
	}

	public RecordDeltaDTO(String id, long fromVersion, Map<String, Object> modifiedFields,
						  Map<String, Object> initialFields) {
		this(id, fromVersion, modifiedFields, initialFields, new HashMap<String, Object>());
	}

	public RecordDeltaDTO(String id, long fromVersion, Map<String, Object> modifiedFields,
						  Map<String, Object> initialFields, Map<String, Object> copyfields) {
		this(id, fromVersion, modifiedFields, initialFields, copyfields, new HashMap<String, Double>());
	}

	public RecordDeltaDTO(String id, long fromVersion, Map<String, Object> modifiedFields,
						  Map<String, Object> initialFields, Map<String, Object> copyfields,
						  Map<String, Double> incrementedFields) {
		super();
		this.id = id;
		this.copyfields = copyfields;
		this.fromVersion = fromVersion;
		this.initialFields = initialFields;
		this.modifiedFields = Collections.unmodifiableMap(modifiedFields);
		this.incrementedFields = incrementedFields;
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

	public Map<String, Double> getIncrementedFields() {
		return incrementedFields;
	}

	public <T> T get(String field) {

		if (modifiedFields.containsKey(field)) {
			return (T) modifiedFields.get(field);

		} else if (initialFields.containsKey(field)) {
			return (T) initialFields.get(field);

		} else if (copyfields.containsKey(field)) {
			return (T) copyfields.get(field);

		} else if (incrementedFields.containsKey(field)) {
			return (T) incrementedFields.get(field);

		} else {
			return null;
		}

	}
}
