package com.constellio.app.ui.pages.search.batchProcessing.entities;

import com.constellio.model.entities.schemas.MetadataSchemaType;

public class BatchProcessPossibleImpact {

	private final MetadataSchemaType schemaType;

	private final int count;

	public BatchProcessPossibleImpact(int count, MetadataSchemaType schemaType) {
		this.count = count;
		this.schemaType = schemaType;
	}

	public MetadataSchemaType getSchemaType() {
		return schemaType;
	}

	public int getCount() {
		return count;
	}

	@Override
	public String toString() {
		return "BatchProcessPossibleImpact{" +
				"schemaType=" + schemaType.getCode() +
				", count=" + count +
				'}';
	}
}
