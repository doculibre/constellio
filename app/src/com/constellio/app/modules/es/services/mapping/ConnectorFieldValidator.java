package com.constellio.app.modules.es.services.mapping;

import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.services.records.RecordMetadataValidatorParams;

import java.util.List;

public class ConnectorFieldValidator implements RecordMetadataValidator<List<ConnectorField>> {

	public static final String MISSING_REQUIRED_ATTRIBUTE = "missingRequiredAttribute";
	public static final String ID_HAS_NO_COLON = "idMustContainTheSchemaType";

	@Override
	public void validate(RecordMetadataValidatorParams recordMetadataValidatorParams) {
		List<ConnectorField> values = (List<ConnectorField>) recordMetadataValidatorParams.getValue();
		for (ConnectorField value : values) {

			if (value.getId() == null || value.getLabel() == null || value.getType() == null) {
				recordMetadataValidatorParams.getValidationErrors().add(ConnectorFieldValidator.class, MISSING_REQUIRED_ATTRIBUTE);
			}

			if (value.getId().split(":").length != 2) {
				recordMetadataValidatorParams.getValidationErrors().add(ConnectorFieldValidator.class, ID_HAS_NO_COLON);
			}

		}
	}
}
