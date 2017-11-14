package com.constellio.model.services.records.aggregations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.entries.AggregatedDataEntry;

public abstract class GetMetadatasUsedToCalculateParams {

	Map<String, MetadataSchemaType> typesMap;
	Metadata metadata;

	public GetMetadatasUsedToCalculateParams(
			Map<String, MetadataSchemaType> typesMap, Metadata metadata) {
		this.typesMap = typesMap;
		this.metadata = metadata;
	}

	public Map<String, MetadataSchemaType> getTypesMap() {
		return typesMap;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public Metadata getFirstInputMetadata() {
		AggregatedDataEntry aggregatedDataEntry = (AggregatedDataEntry) metadata.getDataEntry();
		return getMetadata(aggregatedDataEntry.getFirstInputMetadata());
	}

	public List<Metadata> getInputMetadatas() {
		AggregatedDataEntry aggregatedDataEntry = (AggregatedDataEntry) metadata.getDataEntry();

		List<Metadata> metadatas = new ArrayList<>();

		for (String inputMetadataCode : aggregatedDataEntry.getInputMetadatas()) {
			metadatas.add(getMetadata(inputMetadataCode));
		}

		return metadatas;
	}

	public AggregatedDataEntry getAggregatedDataEntry() {
		return (AggregatedDataEntry) metadata.getDataEntry();
	}

	public abstract Metadata getMetadata(String metadataCode);
}
