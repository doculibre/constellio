package com.constellio.app.services.schemas.bulkImport;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class ResolverCache {

	private static final int MAX_NUMBER_OF_RECORDS_BEFORE_LOADING_ALL_LEGACY_IDS = 1000;

	Map<String, Map<String, SchemaTypeUniqueMetadataMappingCache>> cache = new HashMap<>();

	MetadataSchemaTypes types;

	RecordServices recordServices;

	SearchServices searchServices;

	ImportDataProvider importDataProvider;

	public ResolverCache(RecordServices recordServices, SearchServices searchServices, MetadataSchemaTypes types,
			ImportDataProvider importDataProvider) {
		this.recordServices = recordServices;
		this.types = types;
		this.searchServices = searchServices;
		this.importDataProvider = importDataProvider;
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

	public List<MetadataSchemaType> getCachedSchemaTypes() {
		List<MetadataSchemaType> returnedTypes = new ArrayList<>();
		for (String schemaType : cache.keySet()) {
			MetadataSchemaType type = types.getSchemaType(schemaType);
			returnedTypes.add(type);
		}
		return returnedTypes;
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

	public boolean isRecordUpdate(String schemaType, String legacyId) {
		return getSchemaTypeCache(schemaType, Schemas.LEGACY_ID.getLocalCode()).isRecordUpdate(legacyId);
	}

	public String resolve(String schemaType, String resolver) {
		if (resolver == null) {
			return null;
		} else {
			int colonIndex = resolver.indexOf(":");
			if (colonIndex != -1) {
				String resolverMetadata = resolver.substring(0, colonIndex);
				String resolverValue = resolver.substring(colonIndex + 1);
				String id = getSchemaTypeCache(schemaType, resolverMetadata).searchMapping.get(resolverValue);
				if (id == null) {
					MetadataSchemaType type = types.getSchemaType(schemaType);
					Metadata metadata = type.getAllMetadatas().getMetadataWithLocalCode(resolverMetadata);
					Record result = recordServices.getRecordByMetadata(metadata, resolverValue);
					id = result == null ? null : result.getId();
					getSchemaTypeCache(schemaType, resolverMetadata).mapSearch(resolver, id);
				}
				return id;
			} else {
				return getSchemaTypeCache(schemaType, Schemas.LEGACY_ID.getLocalCode()).idsMapping.get(resolver);
			}
		}
	}

	public void markUniqueValueAsRequired(String schemaType, String metadata, String uniqueValue) {
		getSchemaTypeCache(schemaType, metadata).markLegacyIdAsRequired(uniqueValue);
	}

	public List<String> getUnresolvableUniqueValues(String schemaType, String metadata) {
		return getSchemaTypeCache(schemaType, metadata).getUnresolvableLegacyIds();
	}

	public Set<String> getNotYetImportedLegacyIds(String schemaType) {
		return getSchemaTypeCache(schemaType, Schemas.LEGACY_ID.getLocalCode()).recordsInFile;
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

		int importDataSize = -1;

		String metadata;

		String schemaType;

		Map<String, String> idsMapping = new HashMap<>();

		Map<String, String> searchMapping = new HashMap<>();

		Set<String> recordsInFile = new HashSet<>();

		Set<String> unresolvedLegacyIds = new HashSet<>();

		Set<String> legacyIds = null;

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

		public boolean isRecordUpdate(String legacyId) {

			if (importDataSize == -1) {
				importDataSize = importDataProvider.size(schemaType);
			}
			if (importDataSize <= MAX_NUMBER_OF_RECORDS_BEFORE_LOADING_ALL_LEGACY_IDS) {
				MetadataSchemaType type = types.getSchemaType(schemaType);
				return searchServices.hasResults(from(type).where(Schemas.LEGACY_ID).isEqualTo(legacyId));

			} else {
				if (legacyIds == null) {

					MetadataSchemaType type = types.getSchemaType(schemaType);
					//List<String> ids = searchServices.searchRecordIds(from(type).where(Schemas.LEGACY_ID).isNotNull());
					legacyIds = new HashSet<>();
					Iterator<Record> iterators = searchServices.recordsIterator(new LogicalSearchQuery()
							.setCondition(from(type).where(Schemas.LEGACY_ID).isNotNull())
							.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(Schemas.LEGACY_ID)), 5000);
					while (iterators.hasNext()) {
						String aLegacyId = iterators.next().get(Schemas.LEGACY_ID);
						legacyIds.add(aLegacyId);
					}
				}

				return legacyIds.contains(legacyId);
			}
		}
	}

}
