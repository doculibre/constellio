package com.constellio.model.services.schemas.validators;

import java.util.HashMap;
import java.util.Map;

import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class DecisionValidator implements RecordMetadataValidator<String> {

	public static final String DECISION_APPROVED = "approved";
	public static final String DECISION_REFUSED = "refused";

	@Override
	public void validate(Metadata metadata, String value, ConfigProvider configProvider, ValidationErrors validationErrors) {
		if (value != null && !DECISION_APPROVED.equals(DECISION_APPROVED) && !DECISION_REFUSED.equals(value)) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("DECISION", value);
			validationErrors.add(this.getClass(), "INVALID_DECISION_VALUE", parameters);
		}
	}
}
