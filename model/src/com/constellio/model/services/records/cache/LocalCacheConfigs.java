package com.constellio.model.services.records.cache;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.records.cache.LocalCacheConfigs.CollectionLocalCacheConfigs.CollectionLocalCacheConfigsBuilder;
import com.constellio.model.services.schemas.SchemaUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * TODO Francis : Instead, persist a list of metadata which are available in the local cache and persist it!
 */
public class LocalCacheConfigs {

	int version;

	private Map<String, CollectionLocalCacheConfigs> collectionConfigs;

	public LocalCacheConfigs(int version, Map<String, CollectionLocalCacheConfigs> collectionConfigs) {
		this.version = version;
		this.collectionConfigs = Collections.unmodifiableMap(collectionConfigs);
	}

	public boolean isAvailableInLocalCache(Metadata metadata) {
		CollectionLocalCacheConfigs collectionLocalCacheConfig = collectionConfigs.get(metadata.getCollection());
		if (collectionLocalCacheConfig == null) {
			return metadata.getSchemaType().getCacheType().isSummaryCache() && SchemaUtils.isSummary(metadata);
		} else {
			return SchemaUtils.isSummary(metadata) && collectionLocalCacheConfig.getFullyAvailableMetadatasInCache().contains(metadata.getNoInheritanceCode());
		}
	}

	public LocalCacheConfigsBuilder modify() {
		Map<String, CollectionLocalCacheConfigsBuilder> map = new HashMap<>();
		for (Map.Entry<String, CollectionLocalCacheConfigs> configsEntry : collectionConfigs.entrySet()) {
			map.put(configsEntry.getKey(), configsEntry.getValue().modify());
		}
		return new LocalCacheConfigsBuilder(version, map);
	}

	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	@AllArgsConstructor
	public static class LocalCacheConfigsBuilder {

		int version;

		Map<String, CollectionLocalCacheConfigs.CollectionLocalCacheConfigsBuilder> collectionConfigs = new HashMap<>();

		public LocalCacheConfigsBuilder(LocalCacheConfigs cacheConfigs) {
			version = cacheConfigs.version + 1;
			for (Map.Entry<String, CollectionLocalCacheConfigs> collectionConfig : cacheConfigs.collectionConfigs.entrySet()) {
				collectionConfigs.put(collectionConfig.getKey(), collectionConfig.getValue().modify());
			}
		}

		public LocalCacheConfigs build() {
			Map<String, CollectionLocalCacheConfigs> builtCollectionConfigs = new HashMap<>();
			for (Map.Entry<String, CollectionLocalCacheConfigsBuilder> collectionConfig : collectionConfigs.entrySet()) {
				collectionConfigs.put(collectionConfig.getKey(), collectionConfig.getValue().build());
			}
			return new LocalCacheConfigs(version, builtCollectionConfigs);
		}
	}

	public static class CollectionLocalCacheConfigs {

		@Getter final String collection;

		@Getter final Set<String> fullyAvailableMetadatasInCache;

		@Getter final Set<String> fullyIndexedMetadatasInCache;

		public CollectionLocalCacheConfigs(String collection, Set<String> fullyAvailableMetadatasInCache,
										   Set<String> fullyIndexedMetadatasInCache) {
			this.collection = collection;
			this.fullyAvailableMetadatasInCache = Collections.unmodifiableSet(fullyAvailableMetadatasInCache);
			this.fullyIndexedMetadatasInCache = Collections.unmodifiableSet(fullyIndexedMetadatasInCache);
		}

		@AllArgsConstructor
		@NoArgsConstructor(access = AccessLevel.PRIVATE)
		public static class CollectionLocalCacheConfigsBuilder {

			@Getter
			@Setter
			String collection;

			@Getter
			@Setter
			Set<String> fullyAvailableMetadatasInCache = new HashSet<>();

			@Getter
			@Setter
			Set<String> fullyIndexedMetadatasInCache = new HashSet<>();

			public CollectionLocalCacheConfigsBuilder build() {
				return new CollectionLocalCacheConfigsBuilder(collection, fullyAvailableMetadatasInCache, fullyIndexedMetadatasInCache);
			}
		}


		CollectionLocalCacheConfigs.CollectionLocalCacheConfigsBuilder modify() {
			return new CollectionLocalCacheConfigsBuilder(collection,
					new HashSet<>(fullyAvailableMetadatasInCache),
					new HashSet<>(fullyIndexedMetadatasInCache));
		}

	}


}
