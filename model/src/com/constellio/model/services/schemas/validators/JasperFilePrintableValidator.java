package com.constellio.model.services.schemas.validators;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.services.records.RecordMetadataValidatorParams;

import java.util.HashMap;
import java.util.Map;

public class JasperFilePrintableValidator implements RecordMetadataValidator<Content> {
	@Override
	public void validate(RecordMetadataValidatorParams recordMetadataValidatorParams) {
		Content value = (Content) recordMetadataValidatorParams.getValue();
		if (value != null && !value.getCurrentVersion().getFilename().endsWith(".jasper")) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put("FILE_TYPE", value);
			recordMetadataValidatorParams.getValidationErrors().add(getClass(), "INVALID_FILE_TYPE", parameters);
		}
	}
}
