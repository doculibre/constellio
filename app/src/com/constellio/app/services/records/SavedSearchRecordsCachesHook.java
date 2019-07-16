package com.constellio.app.services.records;

import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.data.utils.Provider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.cache.CacheInsertionStatus;
import com.constellio.model.services.records.cache2.DeterminedHookCacheInsertion;
import com.constellio.model.services.records.cache2.HookCacheInsertionResponse;
import com.constellio.model.services.records.cache2.HookCachePresence;
import com.constellio.model.services.records.cache2.RecordsCachesHook;
import com.constellio.model.services.records.cache2.RemoteCacheAction;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class SavedSearchRecordsCachesHook implements RecordsCachesHook {

	private int current = 0;
	private Record[] lastSavedSearches;

	public SavedSearchRecordsCachesHook(int maxSize) {
		this.lastSavedSearches = new Record[maxSize];
	}

	@Override
	public List<String> getHookSchemaTypes(MetadataSchemaTypes schemaTypes) {
		return schemaTypes.hasType(SavedSearch.SCHEMA_TYPE) ? singletonList(SavedSearch.SCHEMA_TYPE) : emptyList();
	}

	@Override
	public DeterminedHookCacheInsertion determineCacheInsertion(Record record, MetadataSchemaType schemaType,
																MetadataSchemaTypes schemaTypes) {
		if (schemaType.getCode().equals(SavedSearch.SCHEMA_TYPE)) {
			if (new SavedSearch(record, schemaTypes).isTemporary()) {
				return DeterminedHookCacheInsertion.INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT;
			}
		}

		return DeterminedHookCacheInsertion.DEFAULT_INSERT;
	}

	@Override
	public HookCacheInsertionResponse insert(Record record, MetadataSchemaTypes recordSchemaTypes,
											 InsertionReason reason) {

		if (current >= lastSavedSearches.length) {
			current = 0;
		}

		lastSavedSearches[current] = record.getUnmodifiableCopyOfOriginalRecord();
		current++;

		return new HookCacheInsertionResponse(CacheInsertionStatus.ACCEPTED, RemoteCacheAction.DO_NOTHING);
	}

	@Override
	public Record getById(String id, Provider<String, MetadataSchemaType> schemaTypeProviderByCollection) {
		for (Record aSavedSearch : lastSavedSearches) {
			if (aSavedSearch != null && aSavedSearch.getId().equals(id)) {
				return aSavedSearch;
			}
		}

		return null;
	}

	@Override
	public HookCachePresence isRestrictedToHookCache(String id, boolean integerId,
													 Optional<MetadataSchemaType> metadataSchemaType) {
		if (integerId) {
			return HookCachePresence.CANNOT_BE_FOUND_IN_THIS_HOOK_CACHE;

		} else {
			return HookCachePresence.CAN_BE_FOUND_IN_THIS_HOOK_CACHE;
		}
	}

}
