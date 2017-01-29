package com.constellio.model.services.records.cache;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;

public class CacheConfig {

	private String schemaType;

	private boolean permanent;

	private int volatileMaxSize;

	private List<Metadata> indexes;

	private boolean loadedInitially;

	private CacheConfig(String schemaType, boolean permanent, int volatileMaxSize, List<Metadata> indexes,
			boolean loadedInitially) {
		this.schemaType = schemaType;
		this.permanent = permanent;
		this.volatileMaxSize = volatileMaxSize;
		this.indexes = Collections.unmodifiableList(indexes);
		this.loadedInitially = loadedInitially;
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

	public static CacheConfig permanentCache(MetadataSchemaType schemaType) {
		return permanentCache(schemaType.getCode(), schemaType.getDefaultSchema().getMetadatas().onlyUniques());
	}

	public static CacheConfig permanentCache(String schemaType, List<Metadata> indexes) {
		return new CacheConfig(schemaType, true, 0, indexes, true);
	}

	public static CacheConfig volatileCache(MetadataSchemaType schemaType, int maxSize) {
		return volatileCache(schemaType.getCode(), maxSize, schemaType.getDefaultSchema().getMetadatas().onlyUniques());
	}

	public static CacheConfig volatileCache(String schemaType, int maxSize, List<Metadata> indexes) {
		return new CacheConfig(schemaType, false, maxSize, indexes, false);
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
