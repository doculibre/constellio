package com.constellio.model.services.records.cache.hooks;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordId;

import java.util.List;

public interface RecordsCachesHook {

	List<String> getHookedSchemaTypes(MetadataSchemaTypes schemaTypes);

	DeterminedHookCacheInsertion determineCacheInsertion(Record record, MetadataSchemaTypes schemaTypes);

	HookCacheInsertionResponse insert(Record record, MetadataSchemaTypes recordSchemaTypes, InsertionReason reason);

	Record getById(RecordId id);

	void removeRecordFromCache(RecordDTO recordDTO);

}
