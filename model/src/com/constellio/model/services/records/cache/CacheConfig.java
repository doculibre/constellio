package com.constellio.model.services.records.cache;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CacheConfig {

	private String schemaType;

	private boolean permanent;

	private List<Metadata> persistedMetadatas;

	private int volatileMaxSize;

	private List<Metadata> indexes;

	private boolean loadedInitially;

	private VolatileCacheInvalidationMethod invalidationMethod;

	private CacheConfig(String schemaType, boolean permanent, List<Metadata> persistedMetadatas, int volatileMaxSize,
						List<Metadata> indexes, boolean loadedInitially,
						VolatileCacheInvalidationMethod invalidationMethod) {
		this.schemaType = schemaType;
		this.permanent = permanent;
		this.volatileMaxSize = volatileMaxSize;
		this.indexes = Collections.unmodifiableList(indexes);
		this.loadedInitially = loadedInitially;
		this.invalidationMethod = invalidationMethod;
		this.persistedMetadatas = Collections.unmodifiableList(persistedMetadatas);
	}

	public String getSchemaType() {
		return schemaType;
	}

	public boolean isPermanent() {
		return permanent;
	}

	public boolean isVolatile() {
		return !permanent;
	}

	public int getVolatileMaxSize() {
		return volatileMaxSize;
	}

	public List<Metadata> getIndexes() {
		return indexes;
	}

	public boolean isLoadedInitially() {
		return loadedInitially;
	}

	public List<Metadata> getPersistedMetadatas() {
		return persistedMetadatas;
	}

	public VolatileCacheInvalidationMethod getInvalidationMethod() {
		return invalidationMethod;
	}

	public static CacheConfig permanentCache(MetadataSchemaType schemaType) {
		return permanentCache(schemaType.getCode(), schemaType.getDefaultSchema().getMetadatas().onlyUniques());
	}

	public static CacheConfig permanentCacheNotLoadedInitially(MetadataSchemaType schemaType) {
		return new CacheConfig(schemaType.getCode(), true, new ArrayList<Metadata>(), 0,
				schemaType.getDefaultSchema().getMetadatas().onlyUniques(), false, null);
	}


	public static CacheConfig permanentCache(String schemaType, List<Metadata> indexes) {
		return new CacheConfig(schemaType, true, new ArrayList<Metadata>(), 0, indexes, true, null);
	}

	public static CacheConfig permanentCacheNotLoadedInitially(String schemaType, List<Metadata> indexes) {
		return new CacheConfig(schemaType, true, new ArrayList<Metadata>(), 0, indexes, false, null);
	}

	public static CacheConfig volatileCache(MetadataSchemaType schemaType, int maxSize) {
		return volatileCache(schemaType.getCode(), maxSize, schemaType.getDefaultSchema().getMetadatas().onlyUniques());
	}

	public static CacheConfig volatileCache(String schemaType, int maxSize, List<Metadata> indexes) {
		return new CacheConfig(schemaType, false, new ArrayList<Metadata>(), maxSize, indexes, false,
				VolatileCacheInvalidationMethod.LRU);
	}

	public static CacheConfig volatileCache(MetadataSchemaType schemaType, int maxSize,
											VolatileCacheInvalidationMethod invalidationMethod) {
		return volatileCache(schemaType.getCode(), maxSize, schemaType.getDefaultSchema().getMetadatas().onlyUniques(),
				invalidationMethod);
	}

	public static CacheConfig volatileCache(String schemaType, int maxSize, List<Metadata> indexes,
											VolatileCacheInvalidationMethod invalidationMethod) {
		return new CacheConfig(schemaType, false, new ArrayList<Metadata>(), maxSize, indexes, false, invalidationMethod);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

}
