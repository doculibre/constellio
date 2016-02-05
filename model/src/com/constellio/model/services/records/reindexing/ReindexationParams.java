package com.constellio.model.services.records.reindexing;

import java.util.Collections;
import java.util.List;

public class ReindexationParams {

	private ReindexationMode reindexationMode;
	private int batchSize = 100;
	private List<String> reindexedSchemaTypes = Collections.emptyList();

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

	public List<String> getReindexedSchemaTypes() {
		return reindexedSchemaTypes;
	}

	public static ReindexationParams recalculateSchemaTypes(List<String> schemaTypes) {
		ReindexationParams params = new ReindexationParams(ReindexationMode.RECALCULATE);
		params.reindexedSchemaTypes = Collections.unmodifiableList(schemaTypes);
		return params;
	}

}
