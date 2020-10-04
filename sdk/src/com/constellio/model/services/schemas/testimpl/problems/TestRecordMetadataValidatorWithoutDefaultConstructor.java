package com.constellio.model.services.schemas.testimpl.problems;

import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.services.records.RecordMetadataValidatorParams;

public class TestRecordMetadataValidatorWithoutDefaultConstructor implements RecordMetadataValidator<String> {

	public TestRecordMetadataValidatorWithoutDefaultConstructor(String s) {
	}

	@Override
	public void validate(RecordMetadataValidatorParams recordMetadataValidatorParams) {
		throw new UnsupportedOperationException("TODO");
	}

}
