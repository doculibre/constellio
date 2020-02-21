package com.constellio.data.dao.dto.records;

import com.constellio.data.dao.services.bigVault.RecordDaoRuntimeException;
import com.constellio.data.utils.LangUtils;

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

	private int mainSortValue;

	private RecordDTOMode mode;

	public SolrRecordDTO(String id, Map<String, Object> fields, RecordDTOMode mode) {
		this(id, 0, fields, new HashMap<String, Object>(), mode);
	}

	public SolrRecordDTO(RecordDTO recordDTO) {
		this(recordDTO.getId(), recordDTO.getVersion(), new HashMap<>(recordDTO.getFields()), new HashMap<>(recordDTO.getCopyFields()), recordDTO.getLoadingMode());
	}

	public SolrRecordDTO(String id, long version, Map<String, Object> fields,
						 RecordDTOMode mode) {
		this(id, version, fields, new HashMap<String, Object>(), mode);
	}

	public SolrRecordDTO(String id, long version, Map<String, Object> fields,
						 Map<String, Object> copyfields, RecordDTOMode mode) {
		this(id, version, fields, copyfields, mode, MAIN_SORT_UNDEFINED, false);
	}

	private SolrRecordDTO(String id, long version, Map<String, Object> fields,
						  Map<String, Object> copyfields, RecordDTOMode mode, int mainSortValue, boolean creatingCopy) {
		super();
		if (!creatingCopy && id == null) {
			throw new RecordDaoRuntimeException("DTO Cannot have a null id");
		}

		this.id = id;
		this.mainSortValue = mainSortValue;
		this.version = version;

		if (creatingCopy) {
			this.fields = fields;
			this.copyfields = copyfields;

		} else {
			if (fields == null && copyfields != null) {
				this.fields = copyfields;
				this.copyfields = null;
			} else {
				this.fields = fields == null ? null : Collections.unmodifiableMap(fields);
				this.copyfields = copyfields == null ? null : Collections.unmodifiableMap(copyfields);
			}
		}
		this.mode = mode;
	}


	public String getId() {
		return id;
	}

	public long getVersion() {
		return version;
	}

	@Override
	public int getMainSortValue() {
		return mainSortValue;
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
		return new SolrRecordDTO(id, version, fields, copyfields, mode, mainSortValue, true);
	}

	public SolrRecordDTO withMainSortValue(int mainSortValue) {
		return new SolrRecordDTO(id, version, fields, copyfields, mode, mainSortValue, true);
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
	public long heapMemoryConsumption() {
		return 12 + LangUtils.sizeOf(id) + 12 + LangUtils.sizeOf(fields) + 12 + LangUtils.sizeOf(copyfields) + Integer.BYTES;
	}

	@Override
	public long offHeapMemoryConsumption() {
		return 0;
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

	public RecordDTO createSummaryKeeping(List<String> summaryMetadatas) {
		Map<String, Object> newFields = new HashMap<>();

		for (Map.Entry<String, Object> entry : this.fields.entrySet()) {
			if (summaryMetadatas.contains(entry.getKey())) {
				newFields.put(entry.getKey(), entry.getValue());
			}
		}
		newFields.put("schema_s", fields.get("schema_s"));
		newFields.put("collection_s", fields.get("collection_s"));

		Map<String, Object> copyfields = new HashMap<>();

		return new SolrRecordDTO(id, version, newFields, copyfields, RecordDTOMode.SUMMARY);
	}
}
