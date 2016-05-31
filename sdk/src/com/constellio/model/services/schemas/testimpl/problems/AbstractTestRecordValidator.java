package com.constellio.model.services.schemas.testimpl.problems;

import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.records.RecordValidatorParams;

public abstract class AbstractTestRecordValidator implements RecordValidator {

	@Override
	public void validate(RecordValidatorParams params) {
		throw new UnsupportedOperationException("TODO");
	}

}
