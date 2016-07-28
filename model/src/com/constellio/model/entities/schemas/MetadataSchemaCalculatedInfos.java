package com.constellio.model.entities.schemas;

import java.util.List;

import com.constellio.model.entities.schemas.preparationSteps.RecordPreparationStep;

public class MetadataSchemaCalculatedInfos {

	List<RecordPreparationStep> recordPreparationSteps;

	List<Metadata> automaticMetadatas;

	public MetadataSchemaCalculatedInfos(
			List<RecordPreparationStep> recordPreparationSteps,
			List<Metadata> automaticMetadatas) {
		this.recordPreparationSteps = recordPreparationSteps;
		this.automaticMetadatas = automaticMetadatas;
	}

	public List<RecordPreparationStep> getRecordPreparationSteps() {
		return recordPreparationSteps;
	}

	public List<Metadata> getAutomaticMetadatas() {
		return automaticMetadatas;
	}
}
