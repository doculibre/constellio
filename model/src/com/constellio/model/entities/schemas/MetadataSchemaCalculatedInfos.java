package com.constellio.model.entities.schemas;

import java.util.List;

import com.constellio.model.entities.schemas.preparationSteps.RecordPreparationStep;

public class MetadataSchemaCalculatedInfos {

	List<RecordPreparationStep> recordPreparationSteps;

	List<Metadata> automaticMetadatas;
	List<Metadata> lazyVolatilesMetadatas;
	List<Metadata> eagerVolatilesMetadatas;
	private List<Metadata> contentMetadatasForPopulate;

	public MetadataSchemaCalculatedInfos(
			List<RecordPreparationStep> recordPreparationSteps,
			List<Metadata> automaticMetadatas,
			List<Metadata> contentMetadatasForPopulate,
			List<Metadata> lazyVolatilesMetadatas,
			List<Metadata> eagerVolatilesMetadatas) {
		this.recordPreparationSteps = recordPreparationSteps;
		this.automaticMetadatas = automaticMetadatas;
		this.contentMetadatasForPopulate = contentMetadatasForPopulate;
		this.lazyVolatilesMetadatas = lazyVolatilesMetadatas;
		this.eagerVolatilesMetadatas = eagerVolatilesMetadatas;
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

	public List<Metadata> getLazyVolatilesMetadatas() {
		return lazyVolatilesMetadatas;
	}

	public List<Metadata> getEagerVolatilesMetadatas() {
		return eagerVolatilesMetadatas;
	}
}
