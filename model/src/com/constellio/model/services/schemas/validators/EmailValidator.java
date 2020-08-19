package com.constellio.model.services.schemas.validators;

import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;

import java.util.HashMap;
import java.util.Map;

public class EmailValidator implements RecordMetadataValidator<String> {

	@Override
	public void validate(Metadata metadata, String email, ConfigProvider configProvider,
						 ValidationErrors validationErrors) {
		if (email != null && !isValid(email)) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("EMAIL", email);
			validationErrors.add(getClass(), "INVALID_EMAIL", parameters);
		}
	}

	public static boolean isValid(String email) {
		return email != null && email.matches(
				"^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$");
	}
}
