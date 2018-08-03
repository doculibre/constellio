package com.constellio.model.services.schemas.testimpl.problems;

import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class TestRecordMetadataValidatorWithoutDefaultConstructor implements RecordMetadataValidator<String> {

	public TestRecordMetadataValidatorWithoutDefaultConstructor(String s) {
	}

	@Override
	public void validate(Metadata metadata, String value, ConfigProvider configProvider,
						 ValidationErrors validationErrors) {
		throw new UnsupportedOperationException("TODO");
	}

}
