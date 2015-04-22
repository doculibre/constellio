/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.schemas.validators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.Validator;

public class MetadataValueTypeValidator implements Validator<Record> {

	public static final long MAX_SIZE_FOR_STRING_VALUE = 10000;

	public static final String REQUIRED_VALUE_FOR_METADATA = "requiredValueForMetadata";
	public static final String INVALID_VALUE_FOR_METADATA = "invalidValueForMetadata";
	public static final String EXPECTED_TYPE_MESSAGE_PARAM = "expectedType";
	public static final String METADATA_CODE_MESSAGE_PARAM = "metadataCode";
	public static final String WAS_VALUE_CLASS_MESSAGE_PARAM = "wasValueOfClass";

	private final List<Metadata> metadatas;

	public MetadataValueTypeValidator(List<Metadata> metadatas) {
		this.metadatas = metadatas;
	}

	@Override
	public void validate(Record record, ValidationErrors validationErrors) {
		for (Metadata metadata : metadatas) {
			Object value = record.get(metadata);
			if (value != null) {
				if (metadata.getType() == MetadataValueType.STRING) {
					verifyTextValue(metadata, value, validationErrors);
				} else if (metadata.getType() == MetadataValueType.BOOLEAN) {
					verifyBooleanValue(metadata, value, validationErrors);
				} else if (metadata.getType() == MetadataValueType.NUMBER) {
					verifyNumberValue(metadata, value, validationErrors);
				} else if (metadata.getType() == MetadataValueType.DATE_TIME) {
					verifyDateValue(metadata, value, validationErrors);
				} else if (metadata.getType() == MetadataValueType.REFERENCE) {
					verifyReferenceValue(metadata, value, validationErrors);
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private void verifyTextValue(Metadata metadata, Object value, ValidationErrors validationErrors) {
		if (metadata.isMultivalue()) {
			List values = (List) value;
			for (Object object : values) {
				if (!(object instanceof String)) {
					addValidationErrors(metadata, object, validationErrors, "STRING");
				}
			}
		} else if (!(value instanceof String)) {
			addValidationErrors(metadata, value, validationErrors, "STRING");
		}
	}

	@SuppressWarnings("rawtypes")
	private void verifyBooleanValue(Metadata metadata, Object value, ValidationErrors validationErrors) {
		if (metadata.isMultivalue()) {
			List values = (List) value;
			for (Object object : values) {
				if (!(object instanceof Boolean)) {
					addValidationErrors(metadata, object, validationErrors, "BOOLEAN");
				}
			}
		} else if (!(value instanceof Boolean)) {
			addValidationErrors(metadata, value, validationErrors, "BOOLEAN");
		}
	}

	@SuppressWarnings("rawtypes")
	private void verifyNumberValue(Metadata metadata, Object value, ValidationErrors validationErrors) {
		if (metadata.isMultivalue()) {
			List values = (List) value;
			for (Object object : values) {
				if (!(object instanceof Number)) {
					addValidationErrors(metadata, object, validationErrors, "NUMBER");
				}
			}
		} else if (!(value instanceof Number)) {
			addValidationErrors(metadata, value, validationErrors, "NUMBER");
		}
	}

	@SuppressWarnings("rawtypes")
	private void verifyDateValue(Metadata metadata, Object value, ValidationErrors validationErrors) {
		if (metadata.isMultivalue()) {
			List values = (List) value;
			for (Object object : values) {
				if (!(object instanceof LocalDateTime)) {
					addValidationErrors(metadata, object, validationErrors, "DATE_TIME");
				}
			}
		} else if (!(value instanceof LocalDateTime)) {
			addValidationErrors(metadata, value, validationErrors, "DATE_TIME");
		}
	}

	@SuppressWarnings("rawtypes")
	private void verifyReferenceValue(Metadata metadata, Object value, ValidationErrors validationErrors) {
		if (metadata.isMultivalue()) {
			List values = (List) value;
			for (Object object : values) {
				if (!(object instanceof String)) {
					addValidationErrors(metadata, object, validationErrors, "REFERENCE");
				}
			}
		} else if (!(value instanceof String)) {
			addValidationErrors(metadata, value, validationErrors, "REFERENCE");
		}
	}

	//@formatter:off
	private void addValidationErrors(Metadata metadata, Object value, ValidationErrors validationErrors, String metadataValueType) {
		Map<String, String> parameters = createParameters(metadata, value, metadataValueType);
		validationErrors.add(getClass(), INVALID_VALUE_FOR_METADATA, parameters);
	}
	//@formatter:on

	private Map<String, String> createParameters(Metadata metadata, Object value, String expected) {
		Map<String, String> parameters = new HashMap<>();
		parameters.put(METADATA_CODE_MESSAGE_PARAM, metadata.getCode());
		parameters.put(EXPECTED_TYPE_MESSAGE_PARAM, expected);
		parameters.put(WAS_VALUE_CLASS_MESSAGE_PARAM, value.getClass().getName());
		return parameters;
	}
}
