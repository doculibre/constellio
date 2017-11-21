package com.constellio.model.services.records.reindexing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;

public class ReindexingRecordsProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReindexingRecordsProvider.class);

	int mainThreadQueryRows;

	int thresholdForReturningLastIgnoredDocumentById = 1000;

	ReindexingRecordPriorityInfo info = new ReindexingRecordPriorityInfo();

	ModelLayerFactory modelLayerFactory;

	Map<String, List<HashMap<String, Integer>>> map;

	public ReindexingRecordsProvider(ModelLayerFactory modelLayerFactory, int mainThreadQueryRows) {
		this.modelLayerFactory = modelLayerFactory;
		this.mainThreadQueryRows = mainThreadQueryRows;
	}

	ReindexingSchemaTypeRecordsProvider newSchemaTypeProvider(MetadataSchemaType type, int dependencyLevel) {
		return new ReindexingSchemaTypeRecordsProvider(modelLayerFactory, mainThreadQueryRows, type, dependencyLevel,
				thresholdForReturningLastIgnoredDocumentById, info);
	}

	public int getThresholdForReturningLastIgnoredDocumentById() {
		return thresholdForReturningLastIgnoredDocumentById;
	}

	public ReindexingRecordsProvider setThresholdForReturningLastIgnoredDocumentById(
			int thresholdForReturningLastIgnoredDocumentById) {
		this.thresholdForReturningLastIgnoredDocumentById = thresholdForReturningLastIgnoredDocumentById;
		return this;
	}
}
