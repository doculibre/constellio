package com.constellio.model.services.schemas.validators;

import static com.constellio.model.entities.records.Record.PUBLIC_TOKEN;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class ManualTokenValidator implements RecordMetadataValidator<List<String>> {

	@Override
	public void validate(Metadata metadata, List<String> tokens, ConfigProvider configProvider,
			ValidationErrors validationErrors) {

		if (tokens != null) {
			for (String token : tokens) {
				if (token != null && !token.equals(PUBLIC_TOKEN)) {
					if (!token.startsWith("r") && !token.startsWith("w") && !token.startsWith("d")) {
						Map<String, Object> parameters = new HashMap<>();
						parameters.put("invalidToken", token);
						validationErrors.add(getClass(), "tokenMustStartWith_R_W_OR_D", parameters);
					}
				}
			}
		}
	}
}
