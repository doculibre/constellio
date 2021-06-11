package com.constellio.model.services.schemas.validators.metadatas;

import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;

import java.util.regex.Pattern;

public class O365IllegalCharactersValidator {

	public static final Pattern ILLEGAL_CHARACTERS = Pattern.compile("[~#%&*{}+/\\\\:<>?|'\\\"&‘”]");

	public static boolean isValid(String value, ConfigProvider configProvider) {
		if (configProvider.get(ConstellioEIMConfigs.ENABLE_ILLEGAL_CHARACTERS_VALIDATION).equals(Boolean.FALSE)) {
			return true;
		}

		return !match(value);
	}

	public static boolean match(String value) {
		return ILLEGAL_CHARACTERS.matcher(value).find();
	}
}
