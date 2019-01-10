package com.constellio.model.services.records.aggregations;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.entries.AggregatedDataEntry;

import java.util.ArrayList;
import java.util.List;

public abstract class GetMetadatasUsedToCalculateParams {

	private Metadata aggregatedMetadata;
	private String currentReferenceMetadata;

	public GetMetadatasUsedToCalculateParams(Metadata aggregatedMetadata) {
		this.aggregatedMetadata = aggregatedMetadata;
	}

	public GetMetadatasUsedToCalculateParams(Metadata aggregatedMetadata, String currentReferenceMetadata) {
		this.aggregatedMetadata = aggregatedMetadata;
		this.currentReferenceMetadata = currentReferenceMetadata;
	}

	public Metadata getAggregatedMetadata() {
		return aggregatedMetadata;
	}

	public String getCurrentReferenceMetadata() {
		return currentReferenceMetadata;
	}

	public List<Metadata> getReferenceMetadatas() {
		return getMetadatas(getAggregatedDataEntry().getReferenceMetadatas());
	}

	public List<Metadata> getInputMetadatas() {
		AggregatedDataEntry aggregatedDataEntry = (AggregatedDataEntry) aggregatedMetadata.getDataEntry();

		List<Metadata> metadatas = new ArrayList<>();
		for (String inputMetadataCode : aggregatedDataEntry.getInputMetadatas()) {
			metadatas.add(getMetadata(inputMetadataCode));
		}
		return metadatas;
	}

	public List<Metadata> getInputMetadatas(String referenceMetadata) {
		AggregatedDataEntry aggregatedDataEntry = (AggregatedDataEntry) aggregatedMetadata.getDataEntry();

		List<Metadata> metadatas = new ArrayList<>();
		for (String metadataCode : aggregatedDataEntry.getInputMetadatasByReferenceMetadata().get(referenceMetadata)) {
			metadatas.add(getMetadata(metadataCode));
		}
		return metadatas;
	}

	public AggregatedDataEntry getAggregatedDataEntry() {
		return (AggregatedDataEntry) aggregatedMetadata.getDataEntry();
	}

	private List<Metadata> getMetadatas(List<String> metadataCodes) {
		List<Metadata> metadatas = new ArrayList<>();
		for (String metadataCode : metadataCodes) {
			metadatas.add(getMetadata(metadataCode));
		}
		return metadatas;
	}

	public abstract Metadata getMetadata(String metadataCode);
}
