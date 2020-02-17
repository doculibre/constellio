package com.constellio.data.dao.dto.records;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public interface RecordDTO extends RecordsOperationDTO, Serializable {

	int MAIN_SORT_UNDEFINED = 0;
	int MAIN_SORT_UNCHANGED = -1;

	String getId();

	default short getTenantId() {
		return 0;
	}

	long getVersion();

	int getMainSortValue();

	Map<String, Object> getFields();

	Map<String, Object> getCopyFields();

	RecordDTOMode getLoadingMode();

	RecordDTO createCopyWithDelta(RecordDeltaDTO recordDeltaDTO);

	RecordDTO withVersion(long version);

	RecordDTO createCopyOnlyKeeping(Set<String> metadatasDataStoreCodes);

	default String getCollection() {
		return (String) getFields().get("collection_s");
	}

	default String getSchemaCode() {
		return (String) getFields().get("schema_s");
	}

	long heapMemoryConsumption();

	long offHeapMemoryConsumption();

}
