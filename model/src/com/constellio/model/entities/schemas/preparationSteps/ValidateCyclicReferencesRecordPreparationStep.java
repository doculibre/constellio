package com.constellio.model.entities.schemas.preparationSteps;

import java.util.List;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.schemas.SchemaUtils;

public class ValidateCyclicReferencesRecordPreparationStep implements RecordPreparationStep {

	List<Metadata> metadatas;

	public ValidateCyclicReferencesRecordPreparationStep(List<Metadata> metadatas) {
		this.metadatas = metadatas;
	}

	public List<Metadata> getMetadatas() {
		return metadatas;
	}

	public List<String> getMetadatasCodes() {
		return new SchemaUtils().toMetadataCodes(metadatas);
	}
}
