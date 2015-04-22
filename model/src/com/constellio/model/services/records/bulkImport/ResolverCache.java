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
package com.constellio.model.services.records.bulkImport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;

public class ResolverCache {

	Map<String, Map<String, SchemaTypeUniqueMetadataMappingCache>> cache = new HashMap<>();

	MetadataSchemaTypes types;

	SearchServices searchServices;

	public ResolverCache(SearchServices searchServices, MetadataSchemaTypes types) {
		this.searchServices = searchServices;
		this.types = types;
	}

	public SchemaTypeUniqueMetadataMappingCache getSchemaTypeCache(String schemaType, String metadata) {
		if (!cache.containsKey(schemaType)) {
			cache.put(schemaType, new HashMap<String, SchemaTypeUniqueMetadataMappingCache>());
		}
		Map<String, SchemaTypeUniqueMetadataMappingCache> mapping = cache.get(schemaType);
		if (!mapping.containsKey(metadata)) {
			mapping.put(metadata, new SchemaTypeUniqueMetadataMappingCache(schemaType, metadata));
		}

		return mapping.get(metadata);
	}

	public void mapIds(String schemaType, String metadata, String legacyId, String id) {
		getSchemaTypeCache(schemaType, metadata).mapIds(legacyId, id);
	}

	public void markAsRecordInFile(String schemaType, String metadata, String legacyId) {
		getSchemaTypeCache(schemaType, metadata).markAsRecordInFile(legacyId);
	}

	public boolean isAvailable(String schemaType, String metadata, String legacyId) {
		return !getSchemaTypeCache(schemaType, metadata).recordsInFile.contains(legacyId);
	}

	public String resolve(String schemaType, String resolver) {
		if (resolver == null) {
			return null;
		} else if (resolver.contains(":")) {
			String resolverMetadata = resolver.split(":")[0];
			String resolverValue = resolver.split(":")[1];
			String id = getSchemaTypeCache(schemaType, resolverMetadata).searchMapping.get(resolverValue);
			if (id == null) {
				MetadataSchemaType type = types.getSchemaType(schemaType);
				Metadata metadata = type.getAllMetadatas().getMetadataWithLocalCode(resolverMetadata);
				Record result = searchServices.searchSingleResult(
						LogicalSearchQueryOperators.from(type).where(metadata).isEqualTo(resolverValue));
				id = result == null ? null : result.getId();
				getSchemaTypeCache(schemaType, resolverMetadata).mapSearch(resolver, id);
			}
			return id;
		} else {
			return getSchemaTypeCache(schemaType, Schemas.LEGACY_ID.getLocalCode()).idsMapping.get(resolver);
		}

	}

	public void markUniqueValueAsRequired(String schemaType, String metadata, String uniqueValue) {
		getSchemaTypeCache(schemaType, metadata).markLegacyIdAsRequired(uniqueValue);
	}

	public List<String> getUnresolvableUniqueValues(String schemaType, String metadata) {
		return getSchemaTypeCache(schemaType, metadata).getUnresolvableLegacyIds();
	}

	public List<String> getNotYetImportedLegacyIds(String schemaType) {
		return new ArrayList<>(getSchemaTypeCache(schemaType, Schemas.LEGACY_ID.getLocalCode()).recordsInFile);
	}

	public boolean isNewUniqueValue(String schemaType, String metadata, String legacyId) {
		//		boolean newLegacyId = true;
		//		for (Map<String, SchemaTypeUniqueMetadataMappingCache> typeUniqueValues : cache.values()) {
		//			for (SchemaTypeUniqueMetadataMappingCache typeMappingCache : typeUniqueValues.values()) {
		//				newLegacyId &= typeMappingCache.isNewLegacyId(legacyId);
		//			}
		//		}
		//		return newLegacyId;
		return getSchemaTypeCache(schemaType, metadata).isNewLegacyId(legacyId);
	}

	class SchemaTypeUniqueMetadataMappingCache {

		String metadata;

		String schemaType;

		Map<String, String> idsMapping = new HashMap<>();

		Map<String, String> searchMapping = new HashMap<>();

		Set<String> recordsInFile = new HashSet<>();

		Set<String> unresolvedLegacyIds = new HashSet<>();

		private SchemaTypeUniqueMetadataMappingCache(String schemaType, String metadata) {
			this.schemaType = schemaType;
			this.metadata = metadata;
		}

		public void mapIds(String legacyId, String id) {
			idsMapping.put(legacyId, id);
			recordsInFile.remove(legacyId);
			unresolvedLegacyIds.remove(legacyId);
		}

		public void mapSearch(String search, String id) {
			searchMapping.put(search, id);
		}

		public void markLegacyIdAsRequired(String legacyId) {
			if (!idsMapping.containsKey(legacyId) && !recordsInFile.contains(legacyId)) {
				unresolvedLegacyIds.add(legacyId);
			}
		}

		public List<String> getUnresolvableLegacyIds() {
			for (String requiredLegacyId : new ArrayList<>(unresolvedLegacyIds)) {
				String id = resolve(schemaType, metadata + ":" + requiredLegacyId);
				if (id != null) {
					mapIds(requiredLegacyId, id);
				}
			}
			return new ArrayList<>(unresolvedLegacyIds);
		}

		public void markAsRecordInFile(String legacyId) {
			recordsInFile.add(legacyId);
			unresolvedLegacyIds.remove(legacyId);
		}

		public boolean isNewLegacyId(String legacyId) {
			return !idsMapping.containsKey(legacyId) && !recordsInFile.contains(legacyId);
		}
	}
}
