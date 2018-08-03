package com.constellio.model.entities.schemas.entries;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordImpl;

import java.util.List;

public class TransactionAggregatedValuesParams {
	RecordImpl record;
	Metadata metadata;
	AggregatedDataEntry aggregatedDataEntry;
	MetadataSchemaTypes types;

	public TransactionAggregatedValuesParams(RecordImpl record, Metadata metadata,
											 AggregatedDataEntry aggregatedDataEntry, MetadataSchemaTypes types) {
		this.record = record;
		this.metadata = metadata;
		this.aggregatedDataEntry = aggregatedDataEntry;
		this.types = types;
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

	public List<Metadata> getInputMetadatas() {
		return types.getMetadatas(getAggregatedDataEntry().getInputMetadatas());
	}
}
