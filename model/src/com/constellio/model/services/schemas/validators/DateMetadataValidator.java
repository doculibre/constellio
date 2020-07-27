package com.constellio.model.services.schemas.validators;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.Validator;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DateMetadataValidator implements Validator<Record> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MaskedMetadataValidator.class);
	public static final String DATE_IS_AFTER_YEAR_9999 = "yearIsAfterYear9999";
	public static final String METADATA_LABEL = "metadataLabel";
	public static final String VALUE = "value";

	private final List<Metadata> metadatas;
	private boolean skipIfNotEssential;

	public DateMetadataValidator(List<Metadata> metadatas, boolean skipIfNotEssential) {
		this.metadatas = metadatas;
		this.skipIfNotEssential = skipIfNotEssential;
	}

	@Override
	public boolean isEssential() {
		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void validate(Record record, ValidationErrors validationErrors) {
		if (!skipValidation()) {
			for (Metadata metadata : metadatas) {
				if (metadata.getType().isDateOrDateTime() && record.isModified(metadata)) {
					validateMetadata(validationErrors, metadata, record.get(metadata));
				}
			}
		}
	}

	private boolean skipValidation() {
		return !isEssential() && skipIfNotEssential;
	}

	public void validateMetadata(ValidationErrors validationErrors, Metadata metadata, Object value) {
		validate(validationErrors, metadata, value);
	}

	public static void validate(ValidationErrors validationErrors, Metadata metadata, Object value) {
		if (value != null && value instanceof LocalDate) {
			if (((LocalDate) value).getYear() > 9999) {
				LOGGER.info("LocalDate is after year 9999: " + ((LocalDate) value).toString("yyyy-MM-dd"));
				addValidationErrors(validationErrors, ((LocalDate) value).toString("yyyy-MM-dd"), metadata);
			}
		} else if (value != null && value instanceof LocalDateTime) {
			if (((LocalDateTime) value).getYear() > 9999) {
				LOGGER.info("LocalDateTime is after year 9999: " + ((LocalDateTime) value).toString("yyyy-MM-dd HH:mm:ss"));
				addValidationErrors(validationErrors, ((LocalDate) value).toString("yyyy-MM-dd"), metadata);
			}
		} else if (value != null && value instanceof List) {
			for (Object valueInList : (List) value) {
				validate(validationErrors, metadata, valueInList);
			}
		}
	}

	private static void addValidationErrors(ValidationErrors validationErrors, String value, Metadata metadata) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(METADATA_CODE, metadata.getCode());
		parameters.put(METADATA_LABEL, metadata.getLabelsByLanguageCodes());
		parameters.put(VALUE, value);
		validationErrors.add(DateMetadataValidator.class, DATE_IS_AFTER_YEAR_9999, parameters);
	}
}