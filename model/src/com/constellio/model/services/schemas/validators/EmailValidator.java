package com.constellio.model.services.schemas.validators;

import java.util.HashMap;
import java.util.Map;

import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class EmailValidator implements RecordMetadataValidator<String> {

	@Override
	public void validate(Metadata metadata, String email, ConfigProvider configProvider, ValidationErrors validationErrors) {
		if (email != null && !email.matches(
				"^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$")) {
			Map<String, String> parameters = new HashMap<>();
			parameters.put("EMAIL", email);
			validationErrors.add(getClass(), "INVALID_EMAIL", parameters);
		}
	}
}
