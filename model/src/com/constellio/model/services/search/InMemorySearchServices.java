package com.constellio.model.services.search;

import java.util.ArrayList;
import java.util.List;

import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.CacheConfig;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.memoryConditions.EqualMemoryCondition;
import com.constellio.model.services.search.memoryConditions.InMemoryCondition;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import com.constellio.model.services.search.query.logical.condition.DataStoreFieldLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.DataStoreFilters;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.SchemaFilters;
import com.constellio.model.services.search.query.logical.criteria.IsEqualCriterion;

public class InMemorySearchServices {

	RecordsCaches caches;

	MetadataSchemasManager metadataSchemasManager;

	public InMemorySearchServices(ModelLayerFactory modelLayerFactory) {
		this.caches = modelLayerFactory.getRecordsCaches();
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
	}

	public boolean isExecutableInCache(LogicalSearchQuery query) {

		MetadataSchemaType type = getSchemaTypeOfQuery(query);

		if (type != null) {
			CacheConfig cacheConfig = caches.getCache(type.getCollection()).getCacheConfigOf(type.getCode());
			if (cacheConfig == null || cacheConfig.isVolatile()) {
				return false;
			}

			try {
				buildInMemoryCondition(query);

			} catch (Throwable e) {
				e.printStackTrace();
				return false;
			}

			return true;
		}

		return false;
	}

	public SPEQueryResponse executeInCache(LogicalSearchQuery query) {

		MetadataSchemaType type = getSchemaTypeOfQuery(query);

		List<Record> records = caches.getCache(type.getCollection()).getAllValues(type.getCode());
		List<Record> returnableRecords = new ArrayList<>();

		InMemoryCondition inMemoryCondition = buildInMemoryCondition(query);
		for (Record record : records) {

			if (inMemoryCondition.isReturnable(record)) {
				returnableRecords.add(record);
			}

		}

		//TODO returnableRecords and returnedRecords is not the same
		List<Record> returnedRecords = new ArrayList<>(returnableRecords);
		long numFound = returnableRecords.size();

		return new SPEQueryResponse(returnedRecords, numFound);
	}

	private InMemoryCondition buildInMemoryCondition(LogicalSearchQuery query) {
		MetadataSchemaType type = getSchemaTypeOfQuery(query);
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(type.getCollection());
		LogicalSearchCondition condition = query.getCondition();

		InMemoryCondition builtInMemoryCondition;
		if (condition instanceof DataStoreFieldLogicalSearchCondition) {
			DataStoreFieldLogicalSearchCondition fieldCondition = (DataStoreFieldLogicalSearchCondition) condition;

			DataStoreField field = fieldCondition.getDataStoreFields().get(0);
			//TODO Support multiple metadatas

			LogicalSearchValueCondition valueCondition = fieldCondition.getValueCondition();

			if (valueCondition instanceof IsEqualCriterion) {

				builtInMemoryCondition = new EqualMemoryCondition(types, field, ((IsEqualCriterion) valueCondition).getValue());

			} else {
				throw new ImpossibleRuntimeException("Unsupported query");
			}

		} else {
			throw new ImpossibleRuntimeException("Unsupported query");
		}

		return builtInMemoryCondition;

	}

	private MetadataSchemaType getSchemaTypeOfQuery(LogicalSearchQuery query) {

		DataStoreFilters filters = query.getCondition().getFilters();

		if (filters instanceof SchemaFilters) {
			String collection = filters.getCollection();
			String metadataSchemaTypeCode = ((SchemaFilters) filters).getSchemaType();
			return metadataSchemasManager.getSchemaTypes(collection).getSchemaType(metadataSchemaTypeCode);
		}

		return null;
	}

}
