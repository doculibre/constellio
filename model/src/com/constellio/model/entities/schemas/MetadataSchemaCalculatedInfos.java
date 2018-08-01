package com.constellio.model.entities.schemas;

import com.constellio.model.entities.schemas.preparationSteps.RecordPreparationStep;

import java.io.Serializable;
import java.util.List;

public class MetadataSchemaCalculatedInfos implements Serializable {

	List<RecordPreparationStep> recordPreparationSteps;

	List<Metadata> automaticMetadatas;
	List<Metadata> lazyTransientsMetadatas;
	List<Metadata> eagerTransientsMetadatas;
	private List<Metadata> contentMetadatasForPopulate;

	public MetadataSchemaCalculatedInfos(
			List<RecordPreparationStep> recordPreparationSteps,
			List<Metadata> automaticMetadatas,
			List<Metadata> contentMetadatasForPopulate,
			List<Metadata> lazyTransientsMetadatas,
			List<Metadata> eagerTransientsMetadatas) {
		this.recordPreparationSteps = recordPreparationSteps;
		this.automaticMetadatas = automaticMetadatas;
		this.contentMetadatasForPopulate = contentMetadatasForPopulate;
		this.lazyTransientsMetadatas = lazyTransientsMetadatas;
		this.eagerTransientsMetadatas = eagerTransientsMetadatas;
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

	public List<Metadata> getLazyTransientMetadatas() {
		return lazyTransientsMetadatas;
	}

	public List<Metadata> getEagerTransientMetadatas() {
		return eagerTransientsMetadatas;
	}
}
