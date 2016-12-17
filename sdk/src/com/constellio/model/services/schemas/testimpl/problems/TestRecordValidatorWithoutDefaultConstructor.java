package com.constellio.model.services.schemas.testimpl.problems;

import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.records.RecordValidatorParams;

public class TestRecordValidatorWithoutDefaultConstructor implements RecordValidator {

	public TestRecordValidatorWithoutDefaultConstructor(String s) {
	}

	@Override
	public void validate(RecordValidatorParams params) {
		throw new UnsupportedOperationException("TODO");
	}

}
