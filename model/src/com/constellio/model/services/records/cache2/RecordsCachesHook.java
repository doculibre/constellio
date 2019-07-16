package com.constellio.model.services.records.cache2;

import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.data.utils.Provider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.List;
import java.util.Optional;

public interface RecordsCachesHook {

	List<String> getHookSchemaTypes(MetadataSchemaTypes schemaTypes);

	DeterminedHookCacheInsertion determineCacheInsertion(Record record, MetadataSchemaType schemaType,
														 MetadataSchemaTypes schemaTypes);

	HookCacheInsertionResponse insert(Record record, MetadataSchemaTypes recordSchemaTypes, InsertionReason reason);

	Record getById(String id, Provider<String, MetadataSchemaType> schemaTypeProviderByCollection);

	HookCachePresence isRestrictedToHookCache(String id, boolean integerId,
											  Optional<MetadataSchemaType> metadataSchemaType);
}
