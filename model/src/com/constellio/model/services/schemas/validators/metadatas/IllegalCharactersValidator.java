package com.constellio.model.services.schemas.validators.metadatas;

import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordMetadataValidatorParams;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class IllegalCharactersValidator implements RecordMetadataValidator<String> {

	private static final String MUST_NOT_CONTAINS_ILLEGAL_CHARACTERS = "illegalCharactersNotAllowed";
	// do not add any "custom" character here
	public static final Pattern ILLEGAL_CHARACTERS = Pattern.compile("[\\\\/*:?”&<>|]");

	@Override
	public void validate(RecordMetadataValidatorParams recordMetadataValidatorParams) {
		String value = (String) recordMetadataValidatorParams.getValue();
		if (value != null && !isValid(value, recordMetadataValidatorParams.getConfigProvider())) {
			Map<String, Object> params = new HashMap<>();
			params.put(METADATA_LABEL, recordMetadataValidatorParams.getMetadata().getLabelsByLanguageCodes());
			recordMetadataValidatorParams.getValidationErrors().add(getClass(), MUST_NOT_CONTAINS_ILLEGAL_CHARACTERS, params);
		}
	}

	public static boolean isValid(String value, ConfigProvider configProvider) {
		if (configProvider.get(ConstellioEIMConfigs.ENABLE_ILLEGAL_CHARACTERS_VALIDATION).equals(Boolean.FALSE)) {
			return true;
		}

		return !match(value);
	}

	public static boolean match(String value) {
		return value != null && ILLEGAL_CHARACTERS.matcher(value).find();
	}
}
