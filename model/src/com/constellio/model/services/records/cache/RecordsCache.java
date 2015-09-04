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
import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;

public interface RecordsCache {

	Record get(String id);

	boolean isCached(String id);

	void insert(List<Record> record);

	Record insert(Record record);

	void invalidateRecordsOfType(String recordType);

	void invalidate(List<String> recordIds);

	void invalidate(String recordId);

	void configureCache(CacheConfig cacheConfig);

	Collection<CacheConfig> getConfiguredCaches();

	CacheConfig getCacheConfigOf(String schemaOrTypeCode);

	void invalidateAll();

	Record getByMetadata(Metadata metadata, String value);

	void removeCache(String schemaType);

	boolean isConfigured(MetadataSchemaType type);
}
