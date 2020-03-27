package com.constellio.model.services.schemas.validators.metadatas;

import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class IllegalCharactersValidator implements RecordMetadataValidator<String> {

	private static final String MUST_NOT_CONTAINS_ILLEGAL_CHARACTERS = "illegalCharactersNotAllowed";
	private static final Pattern ILLEGAL_CHARACTERS = Pattern.compile("[\\\\/*:?”&<>|]");

	@Override
	public void validate(Metadata metadata, String value, ConfigProvider configProvider,
						 ValidationErrors validationErrors) {
		if (value != null && !isValid(value, configProvider)) {
			Map<String, Object> params = new HashMap<>();
			params.put(METADATA_LABEL, metadata.getLabelsByLanguageCodes());
			validationErrors.add(getClass(), MUST_NOT_CONTAINS_ILLEGAL_CHARACTERS, params);
		}
	}

	public static boolean isValid(String value, ConfigProvider configProvider) {
		if (configProvider.get(ConstellioEIMConfigs.ENABLE_ILLEGAL_CHARACTERS_VALIDATION).equals(Boolean.FALSE)) {
			return true;
		}

		return !ILLEGAL_CHARACTERS.matcher(value).find();
	}
}
