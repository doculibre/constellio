package com.constellio.model.api.impl.schemas.validation.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class Maximum50CharsRecordMultivalueMetadataValidator implements RecordMetadataValidator<List<String>> {

	public static final String WAS_SIZE = "wasSize";
	public static final String MAX_SIZE = "maxSize";
	public static final String VALUE_LENGTH_TOO_LONG = "valueLengthTooLong";

	@Override
	public void validate(Metadata metadata, List<String> values, ConfigProvider configProvider,
			ValidationErrors validationErrors) {
		for (String value : values) {
			if (value != null && value.length() > 50) {
				Map<String, Object> parameters = new HashMap<>();
				parameters.put(MAX_SIZE, "50");
				parameters.put(WAS_SIZE, "" + value.length());
				validationErrors.add(getClass(), VALUE_LENGTH_TOO_LONG, parameters);
			}
		}
	}
}
