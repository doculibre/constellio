package com.constellio.data.dao.dto.records;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RecordDTO extends RecordsOperationDTO, Serializable {

	String getId();

	long getVersion();

	List<String> getLoadedFields();

	Map<String, Object> getFields();

	Map<String, Object> getCopyFields();

	boolean isSummary();

	RecordDTO createCopyWithDelta(RecordDeltaDTO recordDeltaDTO);

	RecordDTO withVersion(long version);

	RecordDTO createCopyOnlyKeeping(Set<String> metadatasDataStoreCodes);
}
