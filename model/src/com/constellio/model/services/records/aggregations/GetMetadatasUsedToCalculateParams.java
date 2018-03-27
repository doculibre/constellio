package com.constellio.model.services.records.aggregations;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.entries.AggregatedDataEntry;

public abstract class GetMetadatasUsedToCalculateParams {

	Metadata aggregatedMetadata;

	public GetMetadatasUsedToCalculateParams(Metadata aggregatedMetadata) {
		this.aggregatedMetadata = aggregatedMetadata;
	}

	public Metadata getAggregatedMetadata() {
		return aggregatedMetadata;
	}

	public Metadata getReferenceMetadata() {
		return getMetadata(getAggregatedDataEntry().getReferenceMetadata());
	}

	public Metadata getFirstInputMetadata() {
		AggregatedDataEntry aggregatedDataEntry = (AggregatedDataEntry) aggregatedMetadata.getDataEntry();
		return getMetadata(aggregatedDataEntry.getFirstInputMetadata());
	}

	public List<Metadata> getInputMetadatas() {
		AggregatedDataEntry aggregatedDataEntry = (AggregatedDataEntry) aggregatedMetadata.getDataEntry();

		List<Metadata> metadatas = new ArrayList<>();

		for (String inputMetadataCode : aggregatedDataEntry.getInputMetadatas()) {
			metadatas.add(getMetadata(inputMetadataCode));
		}

		return metadatas;
	}

	public AggregatedDataEntry getAggregatedDataEntry() {
		return (AggregatedDataEntry) aggregatedMetadata.getDataEntry();
	}

	public abstract Metadata getMetadata(String metadataCode);
}
