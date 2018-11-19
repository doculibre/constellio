package com.constellio.model.services.search;

import com.constellio.data.utils.LazyIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.Iterator;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class RecordsOfSchemaTypesIterator extends LazyIterator<Record> {

	String collection;
	List<String> typeCodes;

	MetadataSchemasManager metadataSchemasManager;
	SearchServices searchServices;

	int index = 0;
	Iterator<Record> currentIterator;

	public RecordsOfSchemaTypesIterator(ModelLayerFactory modelLayerFactory, String collection,
										List<String> typeCodes) {
		this.typeCodes = typeCodes;
		this.collection = collection;
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.searchServices = modelLayerFactory.newSearchServices();
	}

	public RecordsOfSchemaTypesIterator(ModelLayerFactory modelLayerFactory, String collection, String... typeCodes) {
		this.typeCodes = asList(typeCodes);
		this.collection = collection;
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.searchServices = modelLayerFactory.newSearchServices();
	}

	@Override
	protected Record getNextOrNull() {
		if (currentIterator != null && currentIterator.hasNext()) {
			return currentIterator.next();
		}

		if (index >= typeCodes.size()) {
			return null;

		} else {
			String typeCode = typeCodes.get(index);
			MetadataSchemaType schemaType = metadataSchemasManager.getSchemaTypes(collection).getSchemaType(typeCode);
			currentIterator = searchServices.recordsIterator(new LogicalSearchQuery(from(schemaType).returnAll()));
			index++;
			return getNextOrNull();
		}
	}
}
