package com.constellio.model.services.records.cache;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.data.utils.KeyListMap;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.records.cache.cacheIndexHook.IndexedCalculatedKeysHookHandler;
import com.constellio.model.services.records.cache.cacheIndexHook.IndexedCalculatedKeysHookHandler.RecordCountHookHandler;
import com.constellio.model.services.records.cache.cacheIndexHook.IndexedCalculatedKeysHookHandler.RecordIdsHookHandler;
import com.constellio.model.services.records.cache.cacheIndexHook.MetadataIndexCacheDataStoreHook;
import com.constellio.model.services.records.cache.cacheIndexHook.RecordCountHookDataIndexRetriever;
import com.constellio.model.services.records.cache.cacheIndexHook.RecordIdsHookDataIndexRetriever;
import com.constellio.model.services.records.cache.locks.SimpleReadLockMechanism;
import com.constellio.model.services.records.cache.offHeapCollections.SortedIdsList;
import com.constellio.model.services.records.cache.offHeapCollections.SortedIntIdsList;
import com.constellio.model.services.records.cache.offHeapCollections.SortedStringIdsList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static com.constellio.data.utils.LangUtils.estimatedizeOfMapStructureBasedOnSize;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;

/**
 * Concurrency is handled at multiple levels.
 * <p>
 * - Map creation and iteration is synchronized on this
 * - Map get/put/iteration is synchronized on the given map
 */
public class MetadataIndexCacheDataStore implements Closeable {

	private static MetadataIndex EMPTY_INDEX = new MetadataIndex(Collections.emptyMap());

	private SimpleReadLockMechanism lockMechanism = new SimpleReadLockMechanism();

	private KeyListMap<Byte, IndexedCalculatedKeysHookHandler> hooks = new KeyListMap<>();

	private MetadataSchemasManager schemasManager;

	private CollectionsListManager collectionsListManager;

	private Map<Short, MetadataIndex>[][] cacheIndexMaps = new Map[256][];

	public MetadataIndexCacheDataStore(ModelLayerFactory modelLayerFactory) {
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
	}

	public void onTypesModified(MetadataSchemaTypes types) {
		for (IndexedCalculatedKeysHookHandler hookHandler : hooks.get(types.getCollectionInfo().getCollectionId())) {
			hookHandler.onTypesModified(types);
		}

	}

	public <K> RecordCountHookDataIndexRetriever<K> registerRecordCountHook(byte collectionId,
																			MetadataIndexCacheDataStoreHook hook) {

		MetadataSchemaTypes types = null;
		try {
			types = schemasManager.getSchemaTypes(collectionId);
		} catch(Throwable ignored) {
			//OK, types are not initialized, but they are monitored
		}
		RecordCountHookHandler handler = new RecordCountHookHandler(hook, types);
		hooks.add(collectionId, handler);
		return new RecordCountHookDataIndexRetriever(handler.getMap());
	}


	public <K> RecordIdsHookDataIndexRetriever<K> registerRecordIdsHook(
			byte collectionId, MetadataIndexCacheDataStoreHook hook) {

		MetadataSchemaTypes types = null;
		try {
			types = schemasManager.getSchemaTypes(collectionId);
		} catch(Throwable ignored) {
			//OK, types are not initialized, but they are monitored
		}
		RecordIdsHookHandler handler = new RecordIdsHookHandler<>(hook, types);
		hooks.add(collectionId, handler);
		return new RecordIdsHookDataIndexRetriever<>(handler.getMap());

	}


	private static class MetadataIndex {

		Map<Integer, SortedIdsList> map;

		public MetadataIndex(
				Map<Integer, SortedIdsList> map) {
			this.map = map;
		}

		public MetadataIndex() {
			this.map = new HashMap<>();
		}

		void add(Object value, String id) {
			int hashcode = value.hashCode();
			SortedIdsList list = map.get(hashcode);
			if (list == null) {
				list = new SortedIntIdsList();
				map.put(hashcode, list);
			}

			int intId = RecordUtils.toIntKey(id);
			if (intId == RecordUtils.KEY_IS_NOT_AN_INT) {
				if (list instanceof SortedIntIdsList) {
					list = new SortedStringIdsList((SortedIntIdsList) list);
					map.put(hashcode, list);
				}
				list.add(id);
			} else {
				list.add(intId);
			}
		}

		void add(List<Object> values, String id) {
			if (values != null) {
				for (Object value : values) {
					if (value != null) {
						add(value, id);
					}
				}
			}
		}

		void remove(Object value, String id) {
			SortedIdsList list = map.get(value.hashCode());
			if (list != null) {
				list.remove(id);
			}
		}

		void remove(List<Object> values, String id) {
			if (values != null) {
				for (Object value : values) {
					if (value != null) {
						remove(value, id);
					}
				}
			}
		}


		void add(Object value, RecordId id) {
			int hashcode = value.hashCode();
			SortedIdsList list = map.get(hashcode);
			if (list == null) {
				list = new SortedIntIdsList();
				map.put(hashcode, list);
			}

			if (!id.isInteger()) {
				if (list instanceof SortedIntIdsList) {
					list = new SortedStringIdsList((SortedIntIdsList) list);
					map.put(hashcode, list);
				}
				list.add(id);
			} else {
				list.add(id);
			}
		}

		void add(List<Object> values, RecordId id) {
			if (values != null) {
				for (Object value : values) {
					if (value != null) {
						add(value, id);
					}
				}
			}
		}

		void remove(Object value, RecordId id) {
			SortedIdsList list = map.get(value.hashCode());
			if (list != null) {
				list.remove(id);
			}
		}

		void remove(List<Object> values, RecordId id) {
			if (values != null) {
				for (Object value : values) {
					if (value != null) {
						remove(value, id);
					}
				}
			}
		}

		private boolean isEmpty() {
			return map.isEmpty();
		}

		private List<String> getIds(Object value) {

			int valueHashCode = value.hashCode();
			SortedIdsList list = map.get(valueHashCode);
			return list == null ? Collections.emptyList() : list.getValues();
		}

		private List<String> getValuesWithoutSynchronizing(Object value) {

			int valueHashCode = value.hashCode();
			SortedIdsList list = map.get(valueHashCode);
			return list == null ? Collections.emptyList() : list.getValuesWithoutSynchronizing();
		}

		private List<RecordId> getValuesId(Object value) {

			int valueHashCode = value.hashCode();
			SortedIdsList list = map.get(valueHashCode);
			return list == null ? Collections.emptyList() : list.getValuesId();
		}

		private List<RecordId> getValuesIdWithoutSynchronizing(Object value) {

			int valueHashCode = value.hashCode();
			SortedIdsList list = map.get(valueHashCode);
			return list == null ? Collections.emptyList() : list.getValuesIdWithoutSynchronizing();
		}

		private int getIdsCount(Object value) {
			SortedIdsList list = map.get(value.hashCode());
			return list == null ? 0 : list.size();
		}

		public void clear() {
			this.map.values().stream().forEach(SortedIdsList::clear);
			this.map.clear();

		}
	}

	public Stream<String> stream(MetadataSchemaType schemaType, Metadata metadata, Object value) {
		return search(schemaType, metadata, value).stream();
	}

	public List<String> search(MetadataSchemaType schemaType, Metadata metadata, Object value) {


		if (metadata != null && metadata.getSchemaTypeCode().equals("global")) {
			metadata = schemaType.getDefaultSchema().get(metadata.getLocalCode());
		}

		ensureSearchable(metadata);

		if (value == null || ((value instanceof String) && StringUtils.isBlank((String) value))) {
			return Collections.emptyList();
		}

		MetadataIndex metadataIndex = getMetadataIndexMap(schemaType, metadata, false);

		lockMechanism.obtainSchemaTypeReadingPermit(schemaType);
		try {
			return metadataIndex.getValuesWithoutSynchronizing(value);

		} finally {
			lockMechanism.releaseSchemaTypeReadingPermit(schemaType);
		}
	}

	public Stream<RecordId> streamIds(MetadataSchemaType schemaType, Metadata metadata, Object value) {
		return searchIds(schemaType, metadata, value).stream();
	}

	public Iterator<RecordId> iteratorIds(MetadataSchemaType schemaType, Metadata metadata, Object value) {
		return searchIds(schemaType, metadata, value).iterator();
	}

	public List<RecordId> searchIds(MetadataSchemaType schemaType, Metadata metadata, Object value) {

		if (value instanceof List<?>) {
			throw new IllegalArgumentException("value cannot be a List. Seperately call for each item");
		}

		if (metadata != null && metadata.getSchemaTypeCode().equals("global")) {
			metadata = schemaType.getDefaultSchema().get(metadata.getLocalCode());
		}

		ensureSearchable(metadata);

		if (value == null || ((value instanceof String) && StringUtils.isBlank((String) value))) {
			return Collections.emptyList();
		}

		MetadataIndex metadataIndex = getMetadataIndexMap(schemaType, metadata, false);

		lockMechanism.obtainSchemaTypeReadingPermit(schemaType);
		try {
			return metadataIndex.getValuesIdWithoutSynchronizing(value);

		} finally {
			lockMechanism.releaseSchemaTypeReadingPermit(schemaType);
		}
	}


	private void ensureSearchable(Metadata metadata) {
		if (metadata == null) {
			throw new IllegalArgumentException("metadata parameter cannot be null");
		}

		if (!metadata.isCacheIndex() && !metadata.isUniqueValue()
			&& (metadata.getType() != REFERENCE || metadata.getType() != STRING)
			|| metadata.getLocalCode().equals(IDENTIFIER.getLocalCode())) {
			throw new IllegalArgumentException("Metadata in parameter must be a cacheIndex or unique and not ID to search on this cache : " + metadata.getCode());
		}
	}


	public int estimateMaxResultSizeUsingIndexedMetadata(MetadataSchemaType schemaType, Metadata metadata,
														 Object value) {

		lockMechanism.obtainSchemaTypeReadingPermit(schemaType);
		try {
			ensureSearchable(metadata);

			if (value == null || ((value instanceof String) && StringUtils.isBlank((String) value))) {
				return -1;
			}

			MetadataIndex metadataIndex = getMetadataIndexMap(schemaType, metadata, false);
			return metadataIndex.getIdsCount(value);

		} finally {
			lockMechanism.releaseSchemaTypeReadingPermit(schemaType);
		}
	}

	public void addUpdate(Record oldVersion, Record newVersion, MetadataSchemaType schemaType, MetadataSchema schema) {
		List<Runnable> actions = prepareActionsToAjustKeys(oldVersion, newVersion, schema);

		lockMechanism.obtainSchemaTypeWritingPermit(schemaType);
		try {

			validateParameters(oldVersion, newVersion, schema);

			if (schemaType.getCollectionInfo().getCollectionId() != schema.getCollectionInfo().getCollectionId()) {
				throw new IllegalArgumentException("Schema type and schema have different collection id");
			}

			for (Metadata currentMetadata : schema.getCacheIndexMetadatas()) {

				if (oldVersion == null) {
					addRecordMetadata(newVersion, schemaType, currentMetadata);
				} else if (newVersion == null) {
					removeRecordMetadata(oldVersion, schemaType, currentMetadata);
				} else {
					updateRecordMetadata(oldVersion, newVersion, schemaType, currentMetadata);
				}

			}

			actions.forEach((a) -> a.run());

		} finally {
			lockMechanism.releaseSchemaTypeWritingPermit(schemaType);
		}


	}

	@NotNull
	private List<Runnable> prepareActionsToAjustKeys(Record oldVersion, Record newVersion, MetadataSchema schema) {
		List<Runnable> actions = new ArrayList<>();
		List<IndexedCalculatedKeysHookHandler> hookHandlers = hooks.getNestedMap().get(schema.getCollectionInfo().getCollectionId());
		if (hookHandlers != null) {
			if (oldVersion == null) {
				for (IndexedCalculatedKeysHookHandler hookHandler : hookHandlers) {
					actions.addAll(hookHandler.handleRecordInsert(newVersion));
				}
			} else if (newVersion == null) {
				for (IndexedCalculatedKeysHookHandler hookHandler : hookHandlers) {
					actions.addAll(hookHandler.handleRecordRemoval(oldVersion));
				}
			} else {
				for (IndexedCalculatedKeysHookHandler hookHandler : hookHandlers) {
					actions.addAll(hookHandler.handleRecordModification(oldVersion, newVersion));
				}
			}
		}
		return actions;
	}

	private void updateRecordMetadata(Record oldVersion, Record newVersion, MetadataSchemaType schemaType,
									  Metadata currentMetadata) {
		if (oldVersion.getCollectionInfo().getCollectionId() != schemaType.getCollectionInfo().getCollectionId()) {
			throw new IllegalArgumentException("New version and schema type have different collection id");
		}
		if (newVersion.getCollectionInfo().getCollectionId() != schemaType.getCollectionInfo().getCollectionId()) {
			throw new IllegalArgumentException("New version and schema type have different collection id");
		}
		if (!oldVersion.getRecordId().equals(newVersion.getRecordId())) {
			throw new IllegalArgumentException("Records have different ids");
		}

		Object newValue = newVersion.get(currentMetadata);
		boolean isNewValueNull = isObjectNullOrEmpty(newValue);

		MetadataIndex metadataIndexMap = getMetadataIndexMap(schemaType, currentMetadata, !isNewValueNull);

		Object oldValue = oldVersion.get(currentMetadata);

		if (!LangUtils.isEqual(newValue, oldValue)) {
			if (!metadataIndexMap.isEmpty()) {

				if (currentMetadata.isUniqueValue() && currentMetadata.isMultivalue()) {
					((List<?>) oldValue).forEach(valueToRemove -> removeRecordIdToMapByValue(valueToRemove, oldVersion.getRecordId(), metadataIndexMap, currentMetadata));
				} else {
					removeRecordIdToMapByValue(oldValue, oldVersion.getRecordId(), metadataIndexMap, currentMetadata);
				}
			}

			if (!isNewValueNull) {
				if (currentMetadata.isUniqueValue() && currentMetadata.isMultivalue()) {
					((List<?>) newValue).forEach(valueToAdd -> addRecordIdToMapByValue(valueToAdd, oldVersion.getRecordId(), metadataIndexMap, currentMetadata));
				} else {
					addRecordIdToMapByValue(newValue, newVersion.getRecordId(), metadataIndexMap, currentMetadata);
				}
			}
		}
	}

	private void removeRecordMetadata(Record oldVersion, MetadataSchemaType schemaType, Metadata currentMetadata) {
		if (oldVersion.getCollectionInfo().getCollectionId() != schemaType.getCollectionInfo().getCollectionId()) {
			throw new IllegalArgumentException("New version and schema type have different collection id");
		}

		MetadataIndex metadataIndexMap = getMetadataIndexMap(schemaType, currentMetadata, false);

		if (!metadataIndexMap.isEmpty()) {
			removeRecordIdToMapByValue(oldVersion.get(currentMetadata), oldVersion.getRecordId(), metadataIndexMap, currentMetadata);
		}
	}

	private void addRecordMetadata(Record newVersion, MetadataSchemaType schemaType, Metadata currentMetadata) {
		if (newVersion.getCollectionInfo().getCollectionId() != schemaType.getCollectionInfo().getCollectionId()) {
			throw new IllegalArgumentException("New version and schema type have different collection id");
		}

		Object newValue = newVersion.get(currentMetadata);
		if (!isObjectNullOrEmpty(newValue)) {
			MetadataIndex metadataIndexMap = getMetadataIndexMap(schemaType, currentMetadata, true);

			if (currentMetadata.isUniqueValue() && currentMetadata.isMultivalue()) {
				RecordId recordId = newVersion.getRecordId();
				((List<Object>) newValue).forEach(value -> addRecordIdToMapByValue(value, recordId, metadataIndexMap, currentMetadata));
			} else {
				addRecordIdToMapByValue(newValue, newVersion.getRecordId(), metadataIndexMap, currentMetadata);
			}
		}
	}

	private static boolean isObjectNullOrEmpty(Object object) {
		if (object == null) {
			return true;
		} else if (object instanceof List && ((List) object).isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	private void validateParameters(Record oldVersion, Record newVersion, MetadataSchema metadataSchema) {
		if (metadataSchema == null) {
			throw new IllegalArgumentException("metadataSchema parameter cannot be null.");
		}

		if (oldVersion == null && newVersion == null) {
			throw new IllegalArgumentException("For these parameters : oldVersion and newVersion, one need to be not null.");
		}

	}

	private MetadataIndex getMetadataIndexMap(MetadataSchemaType schemaType, Metadata metadata,
											  boolean createIfNotExisitant) {

		Map<Short, MetadataIndex>[] typeMaps = this.cacheIndexMaps[schemaType.getCollectionInfo().getCollectionIndex()];
		if (typeMaps == null) {
			if (createIfNotExisitant) {
				synchronized (this) {
					typeMaps = this.cacheIndexMaps[schemaType.getCollectionInfo().getCollectionIndex()];
					if (typeMaps == null) {
						typeMaps = this.cacheIndexMaps[schemaType.getCollectionInfo().getCollectionIndex()]
								= new Map[MetadataSchemaTypes.LIMIT_OF_TYPES_IN_COLLECTION];
					}
				}
			} else {
				return EMPTY_INDEX;
			}
		}

		Map<Short, MetadataIndex> metadataMaps = typeMaps[schemaType.getId()];
		if (metadataMaps == null) {
			if (createIfNotExisitant) {
				synchronized (this) {
					metadataMaps = typeMaps[schemaType.getId()];
					if (metadataMaps == null) {
						metadataMaps = typeMaps[schemaType.getId()] = new HashMap<>();
					}
				}
			} else {
				return EMPTY_INDEX;
			}
		}


		MetadataIndex metadataIndexMap = metadataMaps.get(metadata.getId());
		if (metadataIndexMap == null) {
			if (createIfNotExisitant) {
				synchronized (this) {
					metadataIndexMap = metadataMaps.computeIfAbsent(metadata.getId(), k -> new MetadataIndex());
				}
			} else {
				return EMPTY_INDEX;
			}
		}

		return metadataIndexMap;
	}

	public int countByIterating() {

		lockMechanism.obtainSystemWideReadingPermit();
		try {
			int counter = 0;

			for (int i = 0; i < cacheIndexMaps.length; i++) {
				Map<Short, MetadataIndex>[] typesMaps = cacheIndexMaps[i];
				if (typesMaps != null) {
					for (int j = 0; j < typesMaps.length; j++) {
						Map<Short, MetadataIndex> metadatasMaps = typesMaps[j];
						if (metadatasMaps != null) {
							synchronized (metadatasMaps) {
								for (MetadataIndex metadatasMap : metadatasMaps.values()) {

									synchronized (metadatasMap) {
										for (SortedIdsList value : metadatasMap.map.values()) {
											counter += value.size();
										}
									}
								}
							}
						}
					}
				}
			}
			return counter;
		} finally {
			lockMechanism.releaseSystemWideReadingPermit();
		}
	}

	private void removeRecordIdToMapByValue(Object value, RecordId recordId, MetadataIndex metadataIndex,
											Metadata metadata) {
		if (isObjectNullOrEmpty(value)) {
			return;
		}

		if (metadata.isUniqueValue()) {
			metadataIndex.remove(value, recordId);

		} else if (!metadata.isMultivalue()) {
			metadataIndex.remove(value, recordId);

		} else if (metadata.isMultivalue()) {
			metadataIndex.remove((List<Object>) value, recordId);
		}
	}


	private void addRecordIdToMapByValue(Object value, RecordId recordId, MetadataIndex metadataIndex,
										 Metadata metadata) {
		if (isObjectNullOrEmpty(value)) {
			return;
		}

		if (metadata.isUniqueValue()) {
			metadataIndex.add(value, recordId);
		} else if (!metadata.isMultivalue()) {
			metadataIndex.add(value, recordId);
		} else if (metadata.isMultivalue()) {
			List<Object> valueList = (List<Object>) value;
			metadataIndex.add(valueList, recordId);
		}
	}

	public void close() {
		lockMechanism.obtainSystemWideWritingPermit();
		try {
			for (Map.Entry<Byte, List<IndexedCalculatedKeysHookHandler>> entry : this.hooks.getMapEntries()) {
				for (IndexedCalculatedKeysHookHandler hookHandler : entry.getValue()) {
					hookHandler.clear();
				}

			}
			this.hooks.clear();

			for (int i = 0; i < this.cacheIndexMaps.length; i++) {
				Map<Short, MetadataIndex>[] collectionIndexes = this.cacheIndexMaps[i];

				if (collectionIndexes != null) {
					for (int j = 0; j < collectionIndexes.length; j++) {
						Map<Short, MetadataIndex> indexes = collectionIndexes[j];
						if (indexes != null) {
							indexes.values().stream().forEach((index) -> {
								index.clear();
							});
							indexes.clear();
						}
					}
				}
			}
		} finally {
			lockMechanism.releaseSystemWideWritingPermit();
		}
	}


	public void clear(CollectionInfo collectionInfo) {
		lockMechanism.obtainCollectionWritingPermit(collectionInfo.getCollectionId());
		try {
			cacheIndexMaps[collectionInfo.getCollectionIndex()] = null;

			for (IndexedCalculatedKeysHookHandler handler : this.hooks.get(collectionInfo.getCollectionId())) {
				handler.clear();
			}

		} finally {
			lockMechanism.releaseCollectionWritingPermit(collectionInfo.getCollectionId());
		}
	}

	public void clear(MetadataSchemaType metadataSchemaType) {
		lockMechanism.obtainSchemaTypeWritingPermit(metadataSchemaType);
		try {
			Map[] typesMaps = cacheIndexMaps[metadataSchemaType.getCollectionInfo().getCollectionIndex()];
			if (typesMaps != null) {
				typesMaps[metadataSchemaType.getId()] = null;
			}

		} finally {
			lockMechanism.releaseSchemaTypeWritingPermit(metadataSchemaType);
		}
	}

	public SimpleReadLockMechanism getLockMechanism() {
		return lockMechanism;
	}


	public List<MetadataIndexCacheDataStoreStat> compileMemoryConsumptionStats() {
		lockMechanism.obtainSystemWideReadingPermit();
		try {
			List<MetadataIndexCacheDataStoreStat> stats = new ArrayList<>();
			for (String collectionCode : collectionsListManager.getCollections()) {

				buildCollectionCachedIndexedStats(stats, collectionCode);
				buildCollectionHookStats(stats, collectionCode);
			}
			return stats;
		} finally {
			lockMechanism.releaseSystemWideReadingPermit();
		}

	}

	private void buildCollectionCachedIndexedStats(List<MetadataIndexCacheDataStoreStat> stats, String collectionCode) {


		int collectionIndex = collectionsListManager.getCollectionInfo(collectionCode).getCollectionIndex();
		Map<Short, MetadataIndex>[] cacheIndexMap = this.cacheIndexMaps[collectionIndex];
		if (cacheIndexMap != null) {
			for (MetadataSchemaType schemaType : schemasManager.getSchemaTypes(collectionCode).getSchemaTypes()) {
				Map<Short, MetadataIndex> metadataIndexes = cacheIndexMap[schemaType.getId()];
				if (metadataIndexes != null) {

					for (Entry<Short, MetadataIndex> entry : metadataIndexes.entrySet()) {
						Metadata metadata = schemaType.getMetadata(entry.getKey());

						String statName = collectionCode + ".metadataIndexes." + metadata.getCode();
						stats.add(buildCachedIndexStats(entry, statName));
					}

				}
			}

		}
	}

	private void buildCollectionHookStats(List<MetadataIndexCacheDataStoreStat> stats, String collectionCode) {


		byte collectionId = collectionsListManager.getCollectionInfo(collectionCode).getCollectionId();
		List<IndexedCalculatedKeysHookHandler> hookHandlers = this.hooks.get(collectionId);
		if (hookHandlers != null) {

			for (IndexedCalculatedKeysHookHandler hookHandler : hookHandlers) {
				stats.add(hookHandler.computerMetadataIndexCacheDataStoreStat());
			}

		}
	}

	private MetadataIndexCacheDataStoreStat buildCachedIndexStats(Entry<Short, MetadataIndex> entry, String statName) {
		//16 bytes for stocking an Integer
		int keysCount = entry.getValue().map.size();
		long keysHeapLength = keysCount * 16;
		long valuesHeapLength = 0;
		long valuesOffHeapLength = 0;
		int valuesCount = 0;
		for (SortedIdsList list : entry.getValue().map.values()) {
			valuesHeapLength += list.valuesHeapLength();
			valuesOffHeapLength += list.valuesOffHeapLength();
			valuesCount += list.size();
		}

		long estimatedMapHeapLength = estimatedizeOfMapStructureBasedOnSize(entry.getValue().map);

		return new MetadataIndexCacheDataStoreStat(
				statName,
				keysCount,
				valuesCount,
				keysHeapLength,
				valuesHeapLength,
				valuesOffHeapLength,
				estimatedMapHeapLength
		);
	}

}
