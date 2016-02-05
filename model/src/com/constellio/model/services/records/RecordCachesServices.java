package com.constellio.model.services.records;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.Iterator;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.CacheConfig;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class RecordCachesServices {

	ModelLayerFactory modelLayerFactory;

	public RecordCachesServices(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	public void loadCachesIn(String collection) {
		RecordsCache recordsCache = modelLayerFactory.getRecordsCaches().getCache(collection);
		for (CacheConfig config : recordsCache.getConfiguredCaches()) {
			if (config.isPermanent()) {
				MetadataSchemaType type = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection)
						.getSchemaType(config.getSchemaType());
				LogicalSearchQuery query = new LogicalSearchQuery().setCondition(from(type).returnAll());
				Iterator<Record> recordsIterator = modelLayerFactory.newSearchServices().recordsIterator(query, 5000);

				while (recordsIterator.hasNext()) {
					recordsCache.insert(recordsIterator.next());
				}
			}
		}

	}
}
