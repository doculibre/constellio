package com.constellio.model.services.records.reindexing;

import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReindexingRecordsProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReindexingRecordsProvider.class);

	int mainThreadQueryRows;

	ModelLayerFactory modelLayerFactory;


	public ReindexingRecordsProvider(ModelLayerFactory modelLayerFactory, int mainThreadQueryRows) {
		this.modelLayerFactory = modelLayerFactory;
		this.mainThreadQueryRows = mainThreadQueryRows;
	}

	ReindexingSchemaTypeRecordsProvider newSchemaTypeProvider(MetadataSchemaType type, int dependencyLevel,
															  int thresholdForReturningLastIgnoredDocumentById) {
		return new ReindexingSchemaTypeRecordsProvider(modelLayerFactory, mainThreadQueryRows, type, dependencyLevel,
				thresholdForReturningLastIgnoredDocumentById);
	}


}
