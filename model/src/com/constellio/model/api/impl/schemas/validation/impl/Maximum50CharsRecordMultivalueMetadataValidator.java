package com.constellio.model.api.impl.schemas.validation.impl;

import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.services.records.RecordMetadataValidatorParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Maximum50CharsRecordMultivalueMetadataValidator implements RecordMetadataValidator<List<String>> {

	public static final String WAS_SIZE = "wasSize";
	public static final String MAX_SIZE = "maxSize";
	public static final String VALUE_LENGTH_TOO_LONG = "valueLengthTooLong";

	@Override
	public void validate(RecordMetadataValidatorParams recordMetadataValidatorParams) {
		List<String> values = (List<String>) recordMetadataValidatorParams.getValue();
		for (String value : values) {
			if (value != null && value.length() > 50) {
				Map<String, Object> parameters = new HashMap<>();
				parameters.put(MAX_SIZE, "50");
				parameters.put(WAS_SIZE, "" + value.length());
				recordMetadataValidatorParams.getValidationErrors().add(getClass(), VALUE_LENGTH_TOO_LONG, parameters);
			}
		}
	}
}
