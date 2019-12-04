package com.constellio.model.services.records.cache.cacheIndexHook.impl;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.cacheIndexHook.RecordCountHookDataIndexRetriever;
import com.constellio.model.services.schemas.MetadataSchemasManager;

import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class RecordUsageCounterHookRetriever implements StatefulService {

	RecordCountHookDataIndexRetriever<Integer> retriever;
	ModelLayerFactory modelLayerFactory;
	MetadataSchemasManager schemasManager;

	public RecordUsageCounterHookRetriever(
			RecordCountHookDataIndexRetriever<Integer> retriever, ModelLayerFactory modelLayerFactory) {
		this.retriever = retriever;
		this.modelLayerFactory = modelLayerFactory;
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();
	}

	public int countRecordsReferencing(Record record) {
		RecordCacheType cacheType = schemasManager.getSchemaTypeOf(record).getCacheType();
		if (cacheType == null || !cacheType.hasVolatileCache()) {
			throw new IllegalArgumentException("Hook only compatible for records stored in summary cache");
		}

		if (modelLayerFactory.getRecordsCaches().areSummaryCachesInitialized()) {
			return retriever.getRecordsCountWith(record.getRecordId().intValue());

		} else {
			List<MetadataSchemaType> summaryCachedSchemaTypes = schemasManager.getSchemaTypes(record.getCollection())
					.getSchemaTypes().stream().filter((schemaType -> schemaType.getCacheType().isSummaryCache()))
					.collect(Collectors.toList());

			return (int) modelLayerFactory.newSearchServices().getResultsCount(from(summaryCachedSchemaTypes)
					.where(Schemas.ALL_REFERENCES).isEqualTo(record.getId()));

		}
	}

	@Override
	public void initialize() {

	}

	@Override
	public void close() {

	}
}

