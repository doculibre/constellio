package com.constellio.app.services.records;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.cache.CacheInsertionStatus;
import com.constellio.model.services.records.cache2.DeterminedHookCacheInsertion;
import com.constellio.model.services.records.cache2.HookCacheInsertionResponse;
import com.constellio.model.services.records.cache2.RecordsCachesHook;
import com.constellio.model.services.records.cache2.RemoteCacheAction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class SavedSearchRecordsCachesHook implements RecordsCachesHook {

	private int current = 0;
	private Record[] lastSavedSearches;

	private Map<String, Integer> recordsIndex = new HashMap<>();

	public SavedSearchRecordsCachesHook(int maxSize) {
		this.lastSavedSearches = new Record[maxSize];
	}

	@Override
	public List<String> getHookedSchemaTypes(MetadataSchemaTypes schemaTypes) {
		return schemaTypes.hasType(SavedSearch.SCHEMA_TYPE) ? singletonList(SavedSearch.SCHEMA_TYPE) : emptyList();
	}

	@Override
	public DeterminedHookCacheInsertion determineCacheInsertion(Record record, MetadataSchemaTypes schemaTypes) {
		if (new SavedSearch(record, schemaTypes).isTemporary()) {
			return DeterminedHookCacheInsertion.INSERT_WITH_HOOK_REPLACING_DEFAULT_INSERT;
		}

		return DeterminedHookCacheInsertion.DEFAULT_INSERT;
	}

	@Override
	public synchronized HookCacheInsertionResponse insert(Record record, MetadataSchemaTypes recordSchemaTypes,
														  InsertionReason reason) {

		Integer index = recordsIndex.get(record.getId());

		if (current >= lastSavedSearches.length) {
			current = 0;
		}

		lastSavedSearches[current] = record.getUnmodifiableCopyOfOriginalRecord();
		current++;

		return new HookCacheInsertionResponse(CacheInsertionStatus.ACCEPTED, RemoteCacheAction.DO_NOTHING);
	}

	@Override
	public Record getById(String id) {
		for (Record aSavedSearch : lastSavedSearches) {
			if (aSavedSearch != null && aSavedSearch.getId().equals(id)) {
				return aSavedSearch.getCopyOfOriginalRecord();
			}
		}

		return null;
	}

	@Override
	public void removeRecordFromCache(RecordDTO recordDTO) {
		//Unsupported
	}

}
