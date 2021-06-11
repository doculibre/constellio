package com.constellio.model.services.schemas;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;

import java.util.ArrayList;
import java.util.List;

public class RecordsModification {

	private MetadataSchemaType metadataSchemaType;

	private List<Record> records = new ArrayList<>();

	private List<Metadata> modifiedMetadatas = new ArrayList<>();

	private RecordUpdateOptions options;

	public RecordsModification(List<Record> records,
							   List<Metadata> modifiedMetadatas, MetadataSchemaType metadataSchemaType,
							   RecordUpdateOptions options) {
		this.records = records;
		this.modifiedMetadatas = modifiedMetadatas;
		this.metadataSchemaType = metadataSchemaType;
		this.options = options;
	}

	public List<Record> getRecords() {
		return records;
	}

	public List<Metadata> getModifiedMetadatas() {
		return modifiedMetadatas;
	}

	public MetadataSchemaType getMetadataSchemaType() {
		return metadataSchemaType;
	}

	public RecordUpdateOptions getOptions() {
		return options;
	}

}
