package com.constellio.model.services.schemas;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;

public class RecordsModification {

	private MetadataSchemaType metadataSchemaType;

	private List<Record> records = new ArrayList<>();

	private List<Metadata> modifiedMetadatas = new ArrayList<>();

	public RecordsModification(List<Record> records,
			List<Metadata> modifiedMetadatas, MetadataSchemaType metadataSchemaType) {
		this.records = records;
		this.modifiedMetadatas = modifiedMetadatas;
		this.metadataSchemaType = metadataSchemaType;
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
}
