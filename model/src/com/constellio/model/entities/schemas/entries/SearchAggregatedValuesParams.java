package com.constellio.model.entities.schemas.entries;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchAggregatedValuesParams {
	LogicalSearchQuery query;
	RecordImpl record;
	Metadata metadata;
	AggregatedDataEntry aggregatedDataEntry;
	MetadataSchemaTypes types;
	SearchServices searchServices;

	public SearchAggregatedValuesParams(LogicalSearchQuery query, RecordImpl record, Metadata metadata,
										AggregatedDataEntry aggregatedDataEntry, MetadataSchemaTypes types,
										SearchServices searchServices) {
		this.query = query;
		this.record = record;
		this.metadata = metadata;
		this.aggregatedDataEntry = aggregatedDataEntry;
		this.types = types;
		this.searchServices = searchServices;
	}

	public LogicalSearchQuery getQuery() {
		return query;
	}

	public RecordImpl getRecord() {
		return record;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public AggregatedDataEntry getAggregatedDataEntry() {
		return aggregatedDataEntry;
	}

	public MetadataSchemaTypes getTypes() {
		return types;
	}

	public SearchServices getSearchServices() {
		return searchServices;
	}

	public List<Metadata> getInputMetadatas() {
		return types.getMetadatas(getAggregatedDataEntry().getInputMetadatas());
	}

	public Map<MetadataSchemaType, List<Metadata>> getInputMetatasBySchemaType() {
		Map<MetadataSchemaType, List<Metadata>> inputMetatasBySchemaType = new HashMap<>();
		for (String referenceMetadata : aggregatedDataEntry.getReferenceMetadatas()) {
			MetadataSchemaType schemaType = types.getSchemaType(SchemaUtils.getSchemaTypeCode(referenceMetadata));
			List<Metadata> metadatas = types.getMetadatas(aggregatedDataEntry.getInputMetadatas(referenceMetadata));
			inputMetatasBySchemaType.put(schemaType, metadatas);
		}

		return inputMetatasBySchemaType;
	}
}
