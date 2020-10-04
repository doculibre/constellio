package com.constellio.model.services.schemas.validators;

import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.services.records.RecordMetadataValidatorParams;

import java.util.HashMap;
import java.util.Map;

public class DecisionValidator implements RecordMetadataValidator<String> {

	public static final String DECISION_APPROVED = "approved";
	public static final String DECISION_REFUSED = "refused";

	@Override
	public void validate(RecordMetadataValidatorParams recordMetadataValidatorParams) {
		String value = (String) recordMetadataValidatorParams.getValue();
		if (value != null && !DECISION_APPROVED.equals(DECISION_APPROVED) && !DECISION_REFUSED.equals(value)) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("DECISION", value);
			recordMetadataValidatorParams.getValidationErrors().add(this.getClass(), "INVALID_DECISION_VALUE", parameters);
		}
	}
}
