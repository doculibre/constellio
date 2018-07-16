package com.constellio.model.services.search;

import java.util.ArrayList;
import java.util.List;

import com.constellio.data.dao.services.factories.LayerFactory;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.CacheConfig;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.memoryConditions.CompositeMemoryCondition;
import com.constellio.model.services.search.memoryConditions.ContainMemoryCondition;
import com.constellio.model.services.search.memoryConditions.EqualMemoryCondition;
import com.constellio.model.services.search.memoryConditions.InMemoryCondition;
import com.constellio.model.services.search.query.logical.LogicalOperator;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import com.constellio.model.services.search.query.logical.condition.*;
import com.constellio.model.services.search.query.logical.criteria.IsContainingElementsCriterion;
import com.constellio.model.services.search.query.logical.criteria.IsEqualCriterion;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchCondition;

import static com.constellio.model.services.search.query.logical.LogicalOperator.AND;

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
			//TODO Support multiple metadatas

			LogicalSearchValueCondition valueCondition = fieldCondition.getValueCondition();
			LogicalOperator operator = ((DataStoreFieldLogicalSearchCondition) condition).getMetadataLogicalOperator();

			InMemoryCondition composite = null;
			for (DataStoreField field : fieldCondition.getDataStoreFields()) {

				if (valueCondition instanceof IsEqualCriterion) {

					builtInMemoryCondition = new EqualMemoryCondition(types, field, ((IsEqualCriterion) valueCondition).getValue());
					composite = new CompositeMemoryCondition(operator);
					((CompositeMemoryCondition) composite).addconditions(builtInMemoryCondition);


				} else {
                    if (valueCondition instanceof IsContainingElementsCriterion) {

                        builtInMemoryCondition = new ContainMemoryCondition(((IsContainingElementsCriterion) valueCondition).getElements(), field, condition);
                        composite = new CompositeMemoryCondition(operator);
                        ((CompositeMemoryCondition) composite).addconditions(builtInMemoryCondition);


                    }
                    throw new ImpossibleRuntimeException("Unsupported query");
				}
			}
			return composite;
		} else {
            InMemoryCondition composite = null;
			if(condition instanceof CompositeLogicalSearchCondition){

				LogicalOperator operator = ((CompositeLogicalSearchCondition) condition).getLogicalOperator();
				for (LogicalSearchCondition conditionone : ((CompositeLogicalSearchCondition) condition).getNestedSearchConditions()) {
					composite = new CompositeMemoryCondition(operator);
					((CompositeMemoryCondition) composite).addconditions((InMemoryCondition) conditionone);
				}
                return composite;

            }else {
					throw new ImpossibleRuntimeException("Unsupported query");
				}

			}


        
       
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
