package com.constellio.model.services.schemas.validators;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.services.records.RecordMetadataValidatorParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JasperFilesPrintableValidator implements RecordMetadataValidator<List<Content>> {

	@Override
	public void validate(RecordMetadataValidatorParams recordMetadataValidatorParams) {
		List<Content> values = (List<Content>) recordMetadataValidatorParams.getValue();
		if (values != null) {
			values.forEach(value -> {
				if (!value.getCurrentVersion().getFilename().endsWith(".jasper")) {
					Map<String, Object> parameters = new HashMap<>();
					parameters.put("FILE_TYPE", value);
					recordMetadataValidatorParams.getValidationErrors().add(getClass(), "INVALID_FILE_TYPE", parameters);
				}
			});
		}
	}
}
