package com.constellio.model.services.schemas.testimpl.problems;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class TestRecordValidatorWithoutDefaultConstructor implements RecordValidator {

	public TestRecordValidatorWithoutDefaultConstructor(String s) {
	}

	@Override
	public void validate(Record record, MetadataSchemaTypes types, MetadataSchema schema, ConfigProvider configProvider,
			ValidationErrors validationErrors) {
		throw new UnsupportedOperationException("TODO");
	}

}
