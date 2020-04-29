package com.constellio.model.services.records.cache;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LocalCacheConfigs {

	private Map<Integer, CollectionTypeLocalCacheConfigs> typesConfigs;

	public LocalCacheConfigs(Map<Integer, CollectionTypeLocalCacheConfigs> typesConfigs) {
		this.typesConfigs = Collections.unmodifiableMap(typesConfigs);
	}

	public boolean isConfigured(MetadataSchemaType metadataSchemaType) {
		CollectionTypeLocalCacheConfigs typeConfigs = typesConfigs.get(metadataSchemaType.getUniqueTenantId());
		return typeConfigs != null;
	}

	public boolean excludedDuringLastCacheRebuild(Metadata metadata) {
		if (metadata.getSchema() == null) {
			throw new IllegalArgumentException("Unsupported global metadata : " + metadata.getCode());
		}
		CollectionTypeLocalCacheConfigs typeConfigs = typesConfigs.get(metadata.getSchemaType().getUniqueTenantId());
		if (typeConfigs == null) {
			return true;

		} else {
			return typeConfigs.getUnavailableMetadatasInCache().contains(metadata.getNoInheritanceCode());
		}
	}

	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	public static class LocalCacheConfigsBuilder {

		Map<Integer, CollectionTypeLocalCacheConfigs> typesConfigs = new HashMap<>();

		public LocalCacheConfigsBuilder(LocalCacheConfigs cacheConfigs) {
			typesConfigs = new HashMap<>(cacheConfigs.typesConfigs);
		}

		public void clearTypeConfigs(MetadataSchemaType schemaType) {
			typesConfigs.remove(schemaType.getUniqueTenantId());
		}

		public void setCollectionTypeConfigs(MetadataSchemaType schemaType, CollectionTypeLocalCacheConfigs configs) {
			typesConfigs.put(schemaType.getUniqueTenantId(), configs);
		}
	}

	public static class CollectionTypeLocalCacheConfigs {

		@Getter final Set<String> unavailableMetadatasInCache;

		public CollectionTypeLocalCacheConfigs(Set<String> unavailableMetadatasInCache) {
			this.unavailableMetadatasInCache = Collections.unmodifiableSet(unavailableMetadatasInCache);
		}

	}


}
