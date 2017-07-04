package com.constellio.model.services.records.reindexing;

import java.util.Collections;
import java.util.List;

import com.constellio.model.entities.schemas.MetadataSchemaType;

public class ReindexationParams {

	private boolean background;
	private ReindexationMode reindexationMode;
	private int batchSize = 0;
	private List<MetadataSchemaType> reindexedSchemaTypes = Collections.emptyList();

	public ReindexationParams(ReindexationMode reindexationMode) {
		this.reindexationMode = reindexationMode;
	}

	public ReindexationMode getReindexationMode() {
		return reindexationMode;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public ReindexationParams setBatchSize(int batchSize) {
		this.batchSize = batchSize;
		return this;
	}

	public List<MetadataSchemaType> getReindexedSchemaTypes() {
		return reindexedSchemaTypes;
	}

	public ReindexationParams setReindexedSchemaTypes(List<MetadataSchemaType> schemaTypes) {
		reindexedSchemaTypes = Collections.unmodifiableList(schemaTypes);
		return this;
	}

	public boolean isBackground() {
		return background;
	}

	public ReindexationParams setBackground(boolean background) {
		this.background = background;
		return this;
	}

	public static ReindexationParams recalculateAndRewriteSchemaTypesInBackground(List<MetadataSchemaType> schemaTypes) {
		ReindexationParams params = new ReindexationParams(ReindexationMode.RECALCULATE_AND_REWRITE);
		params.setBackground(true);
		params.setReindexedSchemaTypes(schemaTypes);
		return params;
	}

}
