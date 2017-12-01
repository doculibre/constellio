package com.constellio.model.entities.schemas.entries;

import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordImpl;

public class TransactionAggregatedValuesParams {
	RecordImpl record;
	Metadata metadata;
	AggregatedDataEntry aggregatedDataEntry;
	MetadataSchemaTypes types;
	List<Record> aggregatedRecords;

	public TransactionAggregatedValuesParams(RecordImpl record, Metadata metadata,
			AggregatedDataEntry aggregatedDataEntry, MetadataSchemaTypes types, List<Record> aggregatedRecords) {
		this.record = record;
		this.metadata = metadata;
		this.aggregatedDataEntry = aggregatedDataEntry;
		this.types = types;
		this.aggregatedRecords = aggregatedRecords;
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

	public List<Record> getAggregatedRecords() {
		return aggregatedRecords;
	}
}
