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

	private CacheConfig(String schemaType, boolean permanent, int volatileMaxSize, List<Metadata> indexes) {
		this.schemaType = schemaType;
		this.permanent = permanent;
		this.volatileMaxSize = volatileMaxSize;
		this.indexes = Collections.unmodifiableList(indexes);
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

	public static CacheConfig permanentCache(MetadataSchemaType schemaType) {
		return permanentCache(schemaType.getCode(), schemaType.getDefaultSchema().getMetadatas().onlyUniques());
	}

	public static CacheConfig permanentCache(String schemaType, List<Metadata> indexes) {
		return new CacheConfig(schemaType, true, 0, indexes);
	}

	public static CacheConfig volatileCache(MetadataSchemaType schemaType, int maxSize) {
		return volatileCache(schemaType.getCode(), maxSize, schemaType.getDefaultSchema().getMetadatas().onlyUniques());
	}

	public static CacheConfig volatileCache(String schemaType, int maxSize, List<Metadata> indexes) {
		return new CacheConfig(schemaType, false, maxSize, indexes);
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
