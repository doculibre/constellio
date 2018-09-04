package com.constellio.model.entities.schemas.preparationSteps;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.validation.RecordValidator;

import java.util.List;

public class ValidateUsingSchemaValidatorsRecordPreparationStep implements RecordPreparationStep {

	private List<RecordValidator> validators;

	public ValidateUsingSchemaValidatorsRecordPreparationStep(List<RecordValidator> validators) {
		this.validators = validators;
	}

	public List<RecordValidator> getValidators() {
		return validators;
	}

	@Override
	public List<Metadata> getMetadatas() {
		return null;
	}
}
