package com.constellio.model.services.schemas.validators.metadatas;

import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.services.records.RecordMetadataValidatorParams;

import java.util.HashMap;
import java.util.Map;

public class IntegerStringValidator implements RecordMetadataValidator<String> {

	public static final String MUST_ONLY_CONTAINS_DIGITS = "digitsOnly";

	@Override
	public void validate(RecordMetadataValidatorParams recordMetadataValidatorParams) {
		String value = (String) recordMetadataValidatorParams.getValue();
		if (value != null && !value.matches("[0-9]+")) {

			Map<String, Object> params = new HashMap<>();
			//params.put("value", value);

			recordMetadataValidatorParams.getValidationErrors().add(getClass(), MUST_ONLY_CONTAINS_DIGITS, params);
		}
	}
}
