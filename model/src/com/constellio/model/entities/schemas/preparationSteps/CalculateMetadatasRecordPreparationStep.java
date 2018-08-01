package com.constellio.model.entities.schemas.preparationSteps;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.schemas.SchemaUtils;

import java.util.List;

public class CalculateMetadatasRecordPreparationStep implements RecordPreparationStep {

	private List<Metadata> metadatas;

	public CalculateMetadatasRecordPreparationStep(List<Metadata> metadatas) {
		this.metadatas = metadatas;
	}

	public List<Metadata> getMetadatas() {
		return metadatas;
	}

	public List<String> getMetadatasCodes() {
		return new SchemaUtils().toMetadataCodes(metadatas);
	}

}