package com.constellio.model.services.schemas.validators.metadatas;

import java.util.HashMap;
import java.util.Map;

import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class IntegerStringValidator implements RecordMetadataValidator<String> {

	public static final String MUST_ONLY_CONTAINS_DIGITS = "digitsOnly";

	@Override
	public void validate(Metadata metadata, String value, ConfigProvider configProvider, ValidationErrors validationErrors) {
		if (value != null && !value.matches("[0-9]+")) {

			Map<String, String> params = new HashMap<>();
			//params.put("value", value);

			validationErrors.add(getClass(), MUST_ONLY_CONTAINS_DIGITS, params);
		}
	}
}
