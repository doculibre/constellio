package com.constellio.model.services.schemas.validators;

import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.services.records.RecordMetadataValidatorParams;

import java.util.HashMap;
import java.util.Map;

public class PercentageValidator implements RecordMetadataValidator<Number> {

	@Override
	public void validate(RecordMetadataValidatorParams recordMetadataValidatorParams) {
		Number percentage = (Number) recordMetadataValidatorParams.getValue();
		if (percentage != null &&
			(percentage.doubleValue() < 0 || percentage.doubleValue() > 100)) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("PERCENTAGE", percentage.toString());
			recordMetadataValidatorParams.getValidationErrors().add(getClass(), "INVALID_PERCENTAGE", parameters);
		}
	}
}
