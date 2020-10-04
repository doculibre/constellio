package com.constellio.model.services.schemas.validators;

import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.services.records.RecordMetadataValidatorParams;

import java.util.HashMap;
import java.util.Map;

public class EmailValidator implements RecordMetadataValidator<String> {

	@Override
	public void validate(RecordMetadataValidatorParams recordMetadataValidatorParams) {
		String email = (String) recordMetadataValidatorParams.getValue();
		if (email != null && !isValid(email)) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("EMAIL", email);
			recordMetadataValidatorParams.getValidationErrors().add(getClass(), "INVALID_EMAIL", parameters);
		}
	}

	public static boolean isValid(String email) {
		return email != null && email.matches(
				"^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$");
	}
}
