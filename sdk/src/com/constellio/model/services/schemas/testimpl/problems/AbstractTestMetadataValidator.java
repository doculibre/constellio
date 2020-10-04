package com.constellio.model.services.schemas.testimpl.problems;

import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.services.records.RecordMetadataValidatorParams;

public abstract class AbstractTestMetadataValidator implements RecordMetadataValidator<String> {

	@Override
	public void validate(RecordMetadataValidatorParams recordMetadataValidatorParams) {
		throw new UnsupportedOperationException("TODO");
	}

}
