package com.constellio.model.entities.schemas;

import java.util.List;

import com.constellio.model.entities.schemas.preparationSteps.RecordPreparationStep;

public class MetadataSchemaCalculatedInfos {

	List<RecordPreparationStep> recordPreparationSteps;

	List<Metadata> automaticMetadatas;
	private List<Metadata> contentMetadatasForPopulate;

	public MetadataSchemaCalculatedInfos(
			List<RecordPreparationStep> recordPreparationSteps,
			List<Metadata> automaticMetadatas,
			List<Metadata> contentMetadatasForPopulate) {
		this.recordPreparationSteps = recordPreparationSteps;
		this.automaticMetadatas = automaticMetadatas;
		this.contentMetadatasForPopulate = contentMetadatasForPopulate;
	}

	public List<RecordPreparationStep> getRecordPreparationSteps() {
		return recordPreparationSteps;
	}

	public List<Metadata> getAutomaticMetadatas() {
		return automaticMetadatas;
	}

	public List<Metadata> getContentMetadatasForPopulate() {
		return contentMetadatasForPopulate;
	}
}
