package com.constellio.model.services.records.cache.cacheIndexHook;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.cache.MetadataIndexCacheDataStoreStat;
import com.constellio.model.services.records.cache.offHeapCollections.SortedIdsList;
import com.constellio.model.services.records.cache.offHeapCollections.SortedIntIdsList;
import com.constellio.model.services.records.cache.offHeapCollections.SortedStringIdsList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class IndexedCalculatedKeysHookHandler<K, V> {
	protected Map<K, V> map = new HashMap<>();

	protected MetadataIndexCacheDataStoreHook<K> hook;

	private byte[] typeHooked = new byte[MetadataSchemaTypes.LIMIT_OF_TYPES_IN_COLLECTION];

	public IndexedCalculatedKeysHookHandler(MetadataIndexCacheDataStoreHook hook, MetadataSchemaTypes types) {
		this.hook = hook;
		this.onTypesModified(types);
	}

	public MetadataIndexCacheDataStoreHook<K> getHook() {
		return hook;
	}

	public Set<String> onTypesModified(MetadataSchemaTypes types) {
		Set<String> typesToReload = new HashSet<>();
		for (MetadataSchemaType type : types.getSchemaTypes()) {
			boolean hooked = hook.isHooked(type);
			byte hookedByteValue = hooked ? (byte) 1 : (byte) 2;
			if (typeHooked[type.getId()] != 0 && typeHooked[type.getId()] != hookedByteValue) {
				typesToReload.add(type.getCode());
			}
			typeHooked[type.getId()] = hookedByteValue;
		}
		return typesToReload;
	}

	public List<Runnable> handleRecordInsert(Record record) {
		List<Runnable> actions = new ArrayList<>();
		if (typeHooked[record.getTypeId()] == (byte) 1) {

			Set<K> keys = hook.getKeys(record);
			for (K key : keys) {
				Runnable action = addAction(key, record.getRecordId());
				if (action != null) {
					actions.add(action);
				}
			}

		}
		return actions;
	}

	public List<Runnable> handleRecordModification(Record oldVersion, Record newVersion) {
		List<Runnable> actions = new ArrayList<>();
		if (typeHooked[oldVersion.getTypeId()] == (byte) 1) {
			Set<K> previousKeys = hook.getKeys(oldVersion);
			Set<K> newKeys = hook.getKeys(newVersion);
			for (K key : previousKeys) {
				if (!newKeys.contains(key)) {
					Runnable action = remove(key, oldVersion.getRecordId());
					if (action != null) {
						actions.add(action);
					}
				}
			}
			for (K key : newKeys) {
				if (!previousKeys.contains(key)) {
					Runnable action = addAction(key, oldVersion.getRecordId());
					if (action != null) {
						actions.add(action);
					}
				}
			}
		}
		return actions;
	}

	public List<Runnable> handleRecordRemoval(Record record) {
		List<Runnable> actions = new ArrayList<>();
		if (typeHooked[record.getTypeId()] == (byte) 1) {
			Set<K> keys = hook.getKeys(record);
			for (K key : keys) {
				Runnable action = remove(key, record.getRecordId());
				if (action != null) {
					actions.add(action);
				}
			}
		}
		return actions;
	}

	protected abstract V newValue();

	private Runnable addAction(K key, RecordId recordId) {
		V container = map.get(key);
		if (container == null) {
			synchronized (map) {
				container = map.get(key);
				if (container == null) {
					container = newValue();
				}
				map.put(key, container);
			}
		}
		return addAction(container, key, recordId);
	}

	private Runnable remove(K key, RecordId recordId) {
		V container = map.get(key);
		if (container != null) {
			return removeAction(container, key, recordId);
		} else {
			return null;
		}
	}

	protected abstract Runnable addAction(V container, K key, RecordId recordId);

	protected abstract Runnable removeAction(V container, K key, RecordId recordId);

	public Map<K, V> getMap() {
		return map;
	}

	public abstract void clear();

	public abstract MetadataIndexCacheDataStoreStat computerMetadataIndexCacheDataStoreStat();

	public static class RecordCountHookHandler<K> extends IndexedCalculatedKeysHookHandler<K, AtomicInteger> {

		public RecordCountHookHandler(MetadataIndexCacheDataStoreHook hook, MetadataSchemaTypes schemaTypes) {
			super(hook, schemaTypes);
		}

		@Override
		protected AtomicInteger newValue() {
			return new AtomicInteger();
		}

		@Override
		protected Runnable addAction(AtomicInteger atomicInteger, K key, RecordId recordId) {
			return () -> {
				atomicInteger.incrementAndGet();
			};
		}

		@Override
		protected Runnable removeAction(AtomicInteger atomicInteger, K key, RecordId recordId) {
			return () -> {
				atomicInteger.decrementAndGet();
			};
		}


		@Override
		public void clear() {
			map.clear();
		}

		public MetadataIndexCacheDataStoreStat computerMetadataIndexCacheDataStoreStat() {

			long keyHeap = map.size() * (12 + hook.getKeyMemoryLength());
			long valueHeap = map.size() * 16;
			long mapHeapSize = LangUtils.estimatedizeOfMapStructureBasedOnSize(map);
			long offHeap = 0;

			String statName = hook.getCollection() + ".hooks." + hook.getClass().getName();

			return new MetadataIndexCacheDataStoreStat(statName, map.size(), map.size(),
					keyHeap, valueHeap, offHeap, mapHeapSize);
		}

	}


	public static class RecordIdsHookHandler<K> extends IndexedCalculatedKeysHookHandler<K, SortedIdsList> {

		public RecordIdsHookHandler(MetadataIndexCacheDataStoreHook hook, MetadataSchemaTypes schemaTypes) {
			super(hook, schemaTypes);
		}

		@Override
		protected SortedIdsList newValue() {
			return new SortedIntIdsList();
		}

		@Override
		protected Runnable addAction(SortedIdsList list, K key, RecordId recordId) {

			if (!recordId.isInteger() && !list.isSupportingLegacyId()) {
				synchronized (map) {
					list = map.get(key);
					if (!recordId.isInteger() && !list.isSupportingLegacyId()) {
						list = new SortedStringIdsList((SortedIntIdsList) list);
						map.put(key, list);
					}
				}
			}
			final SortedIdsList listForInsertion = list;
			return () -> {
				listForInsertion.add(recordId);
			};

		}

		@Override
		protected Runnable removeAction(SortedIdsList list, K key, RecordId recordId) {
			return () -> {
				list.remove(recordId);
			};
		}

		@Override
		public void clear() {
			map.values().forEach(SortedIdsList::clear);
			map.clear();
		}

		public MetadataIndexCacheDataStoreStat computerMetadataIndexCacheDataStoreStat() {

			long keyHeap = map.size() * (12 + hook.getKeyMemoryLength());
			long valueHeap = 0;
			long mapHeapSize = LangUtils.estimatedizeOfMapStructureBasedOnSize(map);
			long offHeap = 0;

			int valuesCount = 0;
			for (SortedIdsList sortedIdsList : map.values()) {
				valuesCount += sortedIdsList.size();
				valueHeap += sortedIdsList.valuesHeapLength();
				offHeap += sortedIdsList.valuesOffHeapLength();
			}

			String statName = hook.getCollection() + ".hooks." + hook.getClass().getName();

			return new MetadataIndexCacheDataStoreStat(statName, map.size(), valuesCount,
					keyHeap, valueHeap, offHeap, mapHeapSize);
		}
	}
}
