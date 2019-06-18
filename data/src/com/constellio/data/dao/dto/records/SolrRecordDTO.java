package com.constellio.data.dao.dto.records;

import com.constellio.data.dao.services.bigVault.RecordDaoRuntimeException;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.constellio.data.dao.dto.records.RecordDTOMode.CUSTOM;
import static java.util.Arrays.asList;

public class SolrRecordDTO implements RecordDTO, RecordsOperationDTO, Serializable {

	private final String id;

	private final long version;

	private final Map<String, Object> fields;

	private final Map<String, Object> copyfields;

	private RecordDTOMode mode;

	public SolrRecordDTO(String id, Map<String, Object> fields, RecordDTOMode mode) {
		this(id, 0, fields, new HashMap<String, Object>(), mode);
	}

	public SolrRecordDTO(String id, long version, Map<String, Object> fields,
						 RecordDTOMode mode) {
		this(id, version, fields, new HashMap<String, Object>(), mode);
	}

	public SolrRecordDTO(String id, long version, Map<String, Object> fields,
						 Map<String, Object> copyfields, RecordDTOMode mode) {
		super();
		if (id == null) {
			throw new RecordDaoRuntimeException("DTO Cannot have a null id");
		}

		this.id = id;
		this.version = version;
		if (fields == null && copyfields != null) {
			this.fields = copyfields;
			this.copyfields = null;
		} else {
			this.fields = fields == null ? null : Collections.unmodifiableMap(fields);
			this.copyfields = copyfields == null ? null : Collections.unmodifiableMap(copyfields);
		}
		this.mode = mode;
	}

	public String getId() {
		return id;
	}

	public long getVersion() {
		return version;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public Map<String, Object> getCopyFields() {
		return copyfields;
	}

	@Override
	public RecordDTOMode getLoadingMode() {
		return mode;
	}

	public RecordDTOMode getMode() {
		return mode;
	}

	public SolrRecordDTO createCopyWithDelta(RecordDeltaDTO recordDeltaDTO) {
		Map<String, Object> newFields = new HashMap<>(fields);
		newFields.putAll(recordDeltaDTO.getModifiedFields());

		Map<String, Object> copyFields = new HashMap<>(copyfields);
		copyFields.putAll(recordDeltaDTO.getCopyfields());

		return new SolrRecordDTO(id, version, newFields, copyFields, mode);
	}

	public SolrRecordDTO withVersion(long version) {
		return new SolrRecordDTO(id, version, fields, copyfields, mode);
	}

	private static List<String> alwaysCopiedFields = asList("collection_s", "schema_s");

	@Deprecated
	public SolrRecordDTO createCopyOnlyKeeping(Set<String> metadatasDataStoreCodes) {

		Map<String, Object> newFields = new HashMap<>();
		for (Map.Entry<String, Object> entry : fields.entrySet()) {
			if (metadatasDataStoreCodes.contains(entry.getKey()) || alwaysCopiedFields.contains(entry.getKey())) {
				newFields.put(entry.getKey(), entry.getValue());
			}
		}

		Map<String, Object> newCopyFields = new HashMap<>();
		for (Map.Entry<String, Object> entry : copyfields.entrySet()) {
			if (metadatasDataStoreCodes.contains(entry.getKey())) {
				newCopyFields.put(entry.getKey(), entry.getValue());
			}
		}

		return new SolrRecordDTO(id, version, newFields, newCopyFields, CUSTOM);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof SolrRecordDTO)) {
			return false;
		}
		SolrRecordDTO dto = (SolrRecordDTO) o;
		return version == dto.version &&
			   mode == dto.mode &&
			   Objects.equals(id, dto.id) &&
			   Objects.equals(fields, dto.fields) &&
			   Objects.equals(copyfields, dto.copyfields);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, version, fields, copyfields, mode);
	}

	@Override
	public String toString() {
		return "SolrRecordDTO{" +
			   "id='" + id + '\'' +
			   ", version=" + version +
			   '}';
	}
}
