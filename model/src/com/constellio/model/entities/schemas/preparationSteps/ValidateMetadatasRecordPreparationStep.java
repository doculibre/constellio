package com.constellio.model.entities.schemas.preparationSteps;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.schemas.SchemaUtils;

import java.util.List;

public class ValidateMetadatasRecordPreparationStep implements RecordPreparationStep {

	private List<Metadata> metadatas;

	private boolean afterCalculate;

	public ValidateMetadatasRecordPreparationStep(List<Metadata> metadatas, boolean afterCalculate) {
		this.metadatas = metadatas;
		this.afterCalculate = afterCalculate;
	}

	public List<Metadata> getMetadatas() {
		return metadatas;
	}

	public boolean isAfterCalculate() {
		return afterCalculate;
	}

	public List<String> getMetadatasCodes() {
		return new SchemaUtils().toMetadataCodes(metadatas);
	}
}
