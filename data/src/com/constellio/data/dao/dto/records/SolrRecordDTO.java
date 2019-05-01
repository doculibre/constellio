package com.constellio.data.dao.dto.records;

import com.constellio.data.dao.services.bigVault.RecordDaoRuntimeException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;

public class SolrRecordDTO implements RecordDTO, RecordsOperationDTO, Serializable {

	private final String id;

	private final long version;

	private final List<String> loadedFields;

	private final Map<String, Object> fields;

	private final Map<String, Object> copyfields;

	private boolean summary;

	public SolrRecordDTO(String id, Map<String, Object> fields, boolean summary) {
		this(id, 0, null, fields, new HashMap<String, Object>(), summary);
	}

	public SolrRecordDTO(String id, long version, List<String> loadedFields, Map<String, Object> fields,
						 boolean summary) {
		this(id, version, loadedFields, fields, new HashMap<String, Object>(), summary);
	}

	public SolrRecordDTO(String id, long version, List<String> loadedFields, Map<String, Object> fields,
						 Map<String, Object> copyfields, boolean summary) {
		super();
		if (id == null) {
			throw new RecordDaoRuntimeException("DTO Cannot have a null id");
		}

		this.id = id;
		this.version = version;
		this.loadedFields = loadedFields == null ? null : Collections.unmodifiableList(loadedFields);
		this.fields = fields == null ? null : Collections.unmodifiableMap(fields);
		this.copyfields = copyfields == null ? null : Collections.unmodifiableMap(copyfields);
		this.summary = summary;
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

	@Override
	public boolean isSummary() {
		return summary;
	}

	public SolrRecordDTO createCopyWithDelta(RecordDeltaDTO recordDeltaDTO) {
		Map<String, Object> newFields = new HashMap<>(fields);
		newFields.putAll(recordDeltaDTO.getModifiedFields());

		Map<String, Object> copyFields = new HashMap<>(copyfields);
		copyFields.putAll(recordDeltaDTO.getCopyfields());

		return new SolrRecordDTO(id, version, loadedFields, newFields, copyFields, summary);
	}

	public SolrRecordDTO withVersion(long version) {
		return new SolrRecordDTO(id, version, loadedFields, fields, copyfields, summary);
	}

	private static List<String> alwaysCopiedFields = asList("collection_s", "schema_s");

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

		return new SolrRecordDTO(id, version, new ArrayList<>(metadatasDataStoreCodes), newFields, newCopyFields, summary);
	}
}
