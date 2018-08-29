package com.constellio.model.entities.schemas.preparationSteps;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.schemas.SchemaUtils;

import java.util.List;

public class SequenceRecordPreparationStep implements RecordPreparationStep {

	List<Metadata> metadatas;

	public SequenceRecordPreparationStep(List<Metadata> metadatas) {
		this.metadatas = metadatas;
	}

	@Override
	public List<Metadata> getMetadatas() {
		return metadatas;
	}

	public List<String> getMetadatasCodes() {
		return new SchemaUtils().toMetadataCodes(metadatas);
	}
}
