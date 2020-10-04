package com.constellio.model.services.schemas.validators;

import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.services.records.RecordMetadataValidatorParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.entities.records.Record.PUBLIC_TOKEN;

public class ManualTokenValidator implements RecordMetadataValidator<List<String>> {

	@Override
	public void validate(RecordMetadataValidatorParams recordMetadataValidatorParams) {
		List<String> tokens = (List<String>) recordMetadataValidatorParams.getValue();
		if (tokens != null) {
			for (String token : tokens) {
				if (token != null && !token.equals(PUBLIC_TOKEN)) {
					if (!token.startsWith("r") && !token.startsWith("w") && !token.startsWith("d")) {
						Map<String, Object> parameters = new HashMap<>();
						parameters.put("invalidToken", token);
						recordMetadataValidatorParams.getValidationErrors().add(getClass(), "tokenMustStartWith_R_W_OR_D", parameters);
					}
				}
			}
		}
	}
}
