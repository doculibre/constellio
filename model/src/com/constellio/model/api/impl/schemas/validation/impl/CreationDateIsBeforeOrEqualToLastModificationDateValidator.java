package com.constellio.model.api.impl.schemas.validation.impl;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class CreationDateIsBeforeOrEqualToLastModificationDateValidator implements RecordValidator {

	public static final String MODIFICATION_DATE_MESSAGE_PARAM = "modificationDate";

	public static final String CREATION_DATE_MESSAGE_PARAM = "creationDate";

	public static final String CREATION_DATE_IS_AFTER_MODIFICATION_DATE = "creationDateIsAfterModificationDate";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CreationDateIsBeforeOrEqualToLastModificationDateValidator.class);

	@Override
	public void validate(Record record, MetadataSchemaTypes types, MetadataSchema schema,
			ConfigProvider configProvider, ValidationErrors validationErrors) {

		Metadata creationDateMetadata = schema.getMetadata("creationDate");
		Metadata modificationDateMetadata = schema.getMetadata("modificationDate");

		LocalDateTime creationDate = record.get(creationDateMetadata);
		LocalDateTime modificationDate = record.get(modificationDateMetadata);
		validate(creationDate, modificationDate, validationErrors);

	}

	private void validate(LocalDateTime creationDate, LocalDateTime modificationDate, ValidationErrors validationErrors) {

		if (creationDate != null && modificationDate != null && creationDate.isAfter(modificationDate)) {

			Map<String, String> parameters = new HashMap<>();
			parameters.put(CREATION_DATE_MESSAGE_PARAM, creationDate.toString());
			parameters.put(MODIFICATION_DATE_MESSAGE_PARAM, modificationDate.toString());
			validationErrors.add(getClass(), CREATION_DATE_IS_AFTER_MODIFICATION_DATE, parameters);
		}
	}
}
