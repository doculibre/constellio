package com.constellio.app.modules.tasks.model;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.cache2.DeterminedHookCacheInsertion;
import com.constellio.model.services.records.cache2.HookCacheInsertionResponse;
import com.constellio.model.services.records.cache2.RecordsCachesHook;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.data.dao.services.cache.InsertionReason.WAS_MODIFIED;
import static com.constellio.model.services.records.cache.CacheInsertionStatus.ACCEPTED;
import static com.constellio.model.services.records.cache2.RemoteCacheAction.DO_NOTHING;
import static com.constellio.model.services.records.cache2.RemoteCacheAction.INSERT;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class TaskRecordsCachesHook implements RecordsCachesHook {

	private Map<String, Record> modelTasks = new HashMap<>();

	@Override
	public List<String> getHookedSchemaTypes(MetadataSchemaTypes schemaTypes) {
		return schemaTypes.hasType(Task.SCHEMA_TYPE) ? singletonList(Task.SCHEMA_TYPE) : emptyList();
	}

	@Override
	public DeterminedHookCacheInsertion determineCacheInsertion(Record record, MetadataSchemaTypes schemaTypes) {
		Task task = new Task(record, schemaTypes);
		if (task.isModel()) {
			return DeterminedHookCacheInsertion.INSERT_WITH_HOOK_ALONG_DEFAULT_INSERT_WITHOUT_VOLATILE;
		}

		return DeterminedHookCacheInsertion.DEFAULT_INSERT;
	}

	@Override
	public HookCacheInsertionResponse insert(Record record, MetadataSchemaTypes schemaTypes, InsertionReason reason) {
		modelTasks.put(record.getId(), record);
		return new HookCacheInsertionResponse(ACCEPTED, reason == WAS_MODIFIED ? INSERT : DO_NOTHING);
	}

	@Override
	public Record getById(String id) {
		Record record = modelTasks.get(id);
		return record == null ? null : record.getCopyOfOriginalRecord();
	}

	@Override
	public void removeRecordFromCache(RecordDTO recordDTO) {
		modelTasks.remove(recordDTO.getId());
	}

}
