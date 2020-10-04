package com.constellio.model.services.schemas.validators;

import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.services.records.RecordMetadataValidatorParams;

import java.util.HashMap;
import java.util.Map;

public class TemporaryRecordValidator implements RecordMetadataValidator<Double> {

	public static final int MIN_NUMBER_OF_DAYS = -1;

	@Override
	public void validate(RecordMetadataValidatorParams recordMetadataValidatorParams) {
		Double value = (Double) recordMetadataValidatorParams.getValue();
		if (value != null && !validateNumberOfDays(value)) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("NUMBER_OF_DAYS", value);
			recordMetadataValidatorParams.getValidationErrors().add(getClass(), "INVALID_NUMBER_OF_DAYS", parameters);
		}
	}

	public boolean validateNumberOfDays(Double value) {
		return value >= MIN_NUMBER_OF_DAYS;
	}
}
