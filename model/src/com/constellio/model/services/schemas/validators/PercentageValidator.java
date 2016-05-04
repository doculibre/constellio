package com.constellio.model.services.schemas.validators;

import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;

import java.util.HashMap;
import java.util.Map;

public class PercentageValidator implements RecordMetadataValidator<Number> {

	@Override
	public void validate(Metadata metadata, Number percentage, ConfigProvider configProvider, ValidationErrors validationErrors) {
		if(percentage != null&&
				(percentage.doubleValue() < 0 || percentage.doubleValue() > 100)){
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("PERCENTAGE", percentage.toString());
			validationErrors.add(getClass(), "INVALID_PERCENTAGE", parameters);
		}
	}
}
