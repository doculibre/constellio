/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.records.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.cache.RecordsCacheImplRuntimeException.RecordsCacheImplRuntimeException_CacheAlreadyConfigured;
import com.constellio.model.services.records.cache.RecordsCacheImplRuntimeException.RecordsCacheImplRuntimeException_InvalidSchemaTypeCode;
import com.constellio.model.services.schemas.SchemaUtils;

public class RecordsCacheImpl implements RecordsCache {

	SchemaUtils schemaUtils = new SchemaUtils();

	Map<String, RecordHolder> cacheById = new HashMap<>();

	Map<String, RecordByMetadataCache> recordByMetadataCache = new HashMap<>();
	Map<String, VolatileCache> volatileCaches = new HashMap<>();
	Map<String, PermanentCache> permanentCaches = new HashMap<>();

	Map<String, CacheConfig> cachedTypes = new HashMap<>();

	public boolean isCached(String id) {
		RecordHolder holder = cacheById.get(id);
		return holder != null && holder.getCopy() != null;
	}

	@Override
	public Record get(String id) {
		RecordHolder holder = cacheById.get(id);

		Record copy = null;
		if (holder != null) {
			copy = holder.getCopy();

			if (copy != null) {
				CacheConfig config = getCacheConfigOf(copy.getSchemaCode());
				if (config.isVolatile()) {
					VolatileCache cache = volatileCaches.get(config.getSchemaType());
					synchronized (this) {
						cache.hit(holder);
					}
				}

			}

		}

		return copy;
	}

	public synchronized void insert(List<Record> records) {
		if (records != null) {
			for (Record record : records) {
				insert(record);
			}
		}
	}

	@Override
	public Record insert(Record record) {

		if (record == null || record.isDirty() || !record.isSaved()) {
			return record;
		}

		if (!record.isFullyLoaded()) {
			invalidate(record.getId());
			return record;
		}

		CacheConfig cacheConfig = getCacheConfigOf(record.getSchemaCode());
		if (cacheConfig != null) {
			Record previousRecord = null;

			synchronized (this) {
				RecordHolder holder = cacheById.get(record.getId());
				if (holder != null) {
					previousRecord = holder.record;
					insertRecordIntoAnAlreadyExistingHolder(record, cacheConfig, holder);
				} else {
					holder = insertRecordIntoAnANewHolder(record, cacheConfig);
				}
				this.recordByMetadataCache.get(cacheConfig.getSchemaType()).insert(previousRecord, holder);
			}

		}
		return record;
	}

	private RecordHolder insertRecordIntoAnANewHolder(Record record, CacheConfig cacheConfig) {
		RecordHolder holder = new RecordHolder(record);
		cacheById.put(record.getId(), holder);
		if (cacheConfig.isVolatile()) {
			VolatileCache cache = volatileCaches.get(cacheConfig.getSchemaType());
			cache.releaseFor(1);
			cache.insert(holder);
		} else {
			PermanentCache cache = permanentCaches.get(cacheConfig.getSchemaType());
			cache.insert(holder);
		}

		return holder;
	}

	private void insertRecordIntoAnAlreadyExistingHolder(Record record, CacheConfig cacheConfig, RecordHolder currentHolder) {
		if (currentHolder.record == null && cacheConfig.isVolatile()) {
			VolatileCache cache = volatileCaches.get(cacheConfig.getSchemaType());
			cache.releaseFor(1);
			cache.insert(currentHolder);
		}

		currentHolder.set(record);
	}

	@Override
	public synchronized void invalidateRecordsOfType(String recordType) {
		CacheConfig cacheConfig = cachedTypes.get(recordType);
		if (cacheConfig.isVolatile()) {
			volatileCaches.get(cacheConfig.getSchemaType()).invalidateAll();
		} else {
			permanentCaches.get(cacheConfig.getSchemaType()).invalidateAll();
		}
	}

	public synchronized void invalidate(List<String> recordIds) {
		if (recordIds != null) {
			for (String recordId : recordIds) {
				invalidate(recordId);
			}
		}
	}

	@Override
	public synchronized void invalidate(String recordId) {
		if (recordId != null) {
			RecordHolder holder = cacheById.get(recordId);
			if (holder != null && holder.record != null) {
				CacheConfig cacheConfig = getCacheConfigOf(holder.record.getSchemaCode());
				recordByMetadataCache.get(cacheConfig.getSchemaType()).invalidate(holder.record);
				holder.invalidate();

			}
		}

	}

	public CacheConfig getCacheConfigOf(String schemaOrTypeCode) {
		String schemaTypeCode = schemaUtils.getSchemaTypeCode(schemaOrTypeCode);
		return cachedTypes.get(schemaTypeCode);
	}

	@Override
	public void configureCache(CacheConfig cacheConfig) {

		if (cacheConfig == null) {
			throw new IllegalArgumentException("Required parameter 'cacheConfig'");
		}
		if (cacheConfig.getSchemaType().contains("_")) {
			throw new RecordsCacheImplRuntimeException_InvalidSchemaTypeCode(cacheConfig.getSchemaType());
		}
		if (cachedTypes.containsKey(cacheConfig.getSchemaType())) {
			throw new RecordsCacheImplRuntimeException_CacheAlreadyConfigured(cacheConfig.getSchemaType());
		}

		cachedTypes.put(cacheConfig.getSchemaType(), cacheConfig);
		if (cacheConfig.isPermanent()) {
			permanentCaches.put(cacheConfig.getSchemaType(), new PermanentCache());
		} else {
			volatileCaches.put(cacheConfig.getSchemaType(), new VolatileCache(cacheConfig.getVolatileMaxSize()));
		}

		recordByMetadataCache.put(cacheConfig.getSchemaType(), new RecordByMetadataCache(cacheConfig));
	}

	@Override
	public Collection<CacheConfig> getConfiguredCaches() {
		return cachedTypes.values();
	}

	@Override
	public void invalidateAll() {
		cacheById.clear();
		for (VolatileCache cache : volatileCaches.values()) {
			cache.invalidateAll();
		}

		for (PermanentCache cache : permanentCaches.values()) {
			cache.invalidateAll();
		}
	}

	@Override
	public Record getByMetadata(Metadata metadata, String value) {
		String schemaTypeCode = schemaUtils.getSchemaTypeCode(metadata);
		RecordByMetadataCache recordByMetadataCache = this.recordByMetadataCache.get(schemaTypeCode);

		Record foundRecord = null;
		if (recordByMetadataCache != null) {
			foundRecord = recordByMetadataCache.getByMetadata(metadata.getLocalCode(), value);
		}
		return foundRecord;
	}

	@Override
	public synchronized void removeCache(String schemaType) {
		recordByMetadataCache.remove(schemaType);
		if (volatileCaches.containsKey(schemaType)) {
			volatileCaches.get(schemaType).invalidateAll();
			volatileCaches.remove(schemaType);
		}
		if (permanentCaches.containsKey(schemaType)) {
			permanentCaches.get(schemaType).invalidateAll();
			permanentCaches.remove(schemaType);
		}

		cachedTypes.remove(schemaType);
	}

	@Override
	public boolean isConfigured(MetadataSchemaType type) {
		return cachedTypes.containsKey(type.getCode());
	}

	static class VolatileCache {

		int maxSize;

		LinkedList<RecordHolder> holders = new LinkedList<>();

		int recordsInCache;

		private VolatileCache(int maxSize) {
			this.maxSize = maxSize;
		}

		public void insert(RecordHolder holder) {
			holder.volatileCacheOccurences = 1;
			holders.add(holder);
			recordsInCache++;
		}

		public void hit(RecordHolder holder) {
			holder.volatileCacheOccurences++;
			holders.add(holder);
		}

		public void releaseFor(int qty) {
			while (recordsInCache + qty > maxSize) {
				releaseNext();
			}
		}

		private void releaseNext() {
			RecordHolder recordHolder = holders.removeFirst();
			if (recordHolder.volatileCacheOccurences > 1) {
				recordHolder.volatileCacheOccurences--;
				releaseNext();
			} else {
				recordHolder.invalidate();
				recordsInCache--;
			}
		}

		public void invalidateAll() {
			this.recordsInCache = 0;
			for (RecordHolder holder : holders) {
				holder.invalidate();
			}
			holders.clear();
		}

	}

	static class PermanentCache {

		LinkedList<RecordHolder> holders = new LinkedList<>();

		public void insert(RecordHolder holder) {
			holders.add(holder);
		}

		public void invalidateAll() {
			for (RecordHolder holder : holders) {
				holder.invalidate();
			}
		}

	}

	static class RecordByMetadataCache {

		Map<String, Map<String, RecordHolder>> map = new HashMap<>();
		Map<String, Metadata> supportedMetadatas = new HashMap<>();

		public RecordByMetadataCache(CacheConfig cacheConfig) {
			for (Metadata indexedMetadata : cacheConfig.getIndexes()) {
				supportedMetadatas.put(indexedMetadata.getLocalCode(), indexedMetadata);
				map.put(indexedMetadata.getLocalCode(), new HashMap<String, RecordHolder>());
			}
		}

		public Record getByMetadata(String localCode, String value) {
			Map<String, RecordHolder> metadataMap = map.get(localCode);
			RecordHolder recordHolder = null;
			if (metadataMap != null) {
				recordHolder = metadataMap.get(value);
			}
			return recordHolder == null ? null : recordHolder.record;
		}

		public void insert(Record previousRecord, RecordHolder recordHolder) {

			for (Metadata supportedMetadata : supportedMetadatas.values()) {
				String value = null;
				String previousValue = null;

				if (previousRecord != null) {
					previousValue = previousRecord.get(supportedMetadata);
				}
				if (recordHolder.record != null) {
					value = recordHolder.record.get(supportedMetadata);
				}
				if (previousValue != null && !previousValue.equals(value)) {
					map.get(supportedMetadata.getLocalCode()).remove(previousValue);
				}
				if (value != null && !value.equals(previousValue)) {
					map.get(supportedMetadata.getLocalCode()).put(value, recordHolder);
				}
			}
		}

		public void invalidate(Record record) {
			for (Metadata supportedMetadata : supportedMetadatas.values()) {
				String value = record.get(supportedMetadata);

				if (value != null) {
					map.get(supportedMetadata.getLocalCode()).remove(value);
				}
			}
		}
	}

	static class RecordHolder {

		private Record record;

		private int volatileCacheOccurences;

		private RecordHolder(Record record) {
			set(record);
		}

		public Record getCopy() {
			Record copy = record;
			if (copy != null) {
				copy.getCopyOfOriginalRecord();
			}
			return copy;
		}

		public void set(Record record) {
			Boolean logicallyDeletedStatus = record.get(Schemas.LOGICALLY_DELETED_STATUS);
			if (logicallyDeletedStatus == null || !logicallyDeletedStatus) {
				this.record = record.getCopyOfOriginalRecord();
			} else {
				this.record = null;
			}
		}

		public void invalidate() {
			this.record = null;
		}

	}
}

