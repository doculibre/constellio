package com.constellio.app.modules.es.services.mapping;

import java.util.List;

import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class ConnectorFieldValidator implements RecordMetadataValidator<List<ConnectorField>> {

	public static final String MISSING_REQUIRED_ATTRIBUTE = "missingRequiredAttribute";
	public static final String ID_HAS_NO_COLON = "idMustContainTheSchemaType";

	@Override
	public void validate(Metadata metadata, List<ConnectorField> values, ConfigProvider configProvider,
			ValidationErrors validationErrors) {

		for (ConnectorField value : values) {

			if (value.getId() == null || value.getLabel() == null || value.getType() == null) {
				validationErrors.add(ConnectorFieldValidator.class, MISSING_REQUIRED_ATTRIBUTE);
			}

			if (value.getId().split(":").length != 2) {
				validationErrors.add(ConnectorFieldValidator.class, ID_HAS_NO_COLON);
			}

		}
	}
}
