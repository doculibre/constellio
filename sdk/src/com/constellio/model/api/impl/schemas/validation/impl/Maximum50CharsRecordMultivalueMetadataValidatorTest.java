package com.constellio.model.api.impl.schemas.validation.impl;

import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordMetadataValidatorParams;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class Maximum50CharsRecordMultivalueMetadataValidatorTest extends ConstellioTest {

	List<String> emptyValues, valuesOf50Character, valuesOf51Character;

	@Mock Metadata metadata;
	@Mock ValidationErrors validationErrors;
	@Mock ConfigProvider configProvider;

	Maximum50CharsRecordMultivalueMetadataValidator validator;

	@Before
	public void setUp() {
		validator = new Maximum50CharsRecordMultivalueMetadataValidator();
		String valueOf50Character = "12345678901234567890123456789012345678901234567890";
		String valueOf51Character = "123456789012345678901234567890123456789012345678901";
		emptyValues = Collections.emptyList();
		valuesOf50Character = Arrays.asList(valueOf50Character, valueOf50Character);
		valuesOf51Character = Arrays.asList(valueOf50Character, valueOf51Character);
	}

	@Test
	public void whenValueHasMoreThan50CharacterThenAddValidationMessage()
			throws Exception {
		validator.validate(new RecordMetadataValidatorParams(metadata, valuesOf51Character, configProvider, validationErrors, false));

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("maxSize", "50");
		parameters.put("wasSize", "51");
		verify(validationErrors).add(Maximum50CharsRecordMultivalueMetadataValidator.class,
				Maximum50CharsRecordMultivalueMetadataValidator.VALUE_LENGTH_TOO_LONG, parameters);
		verifyZeroInteractions(metadata);
	}

	@Test
	public void whenValueHas50CharacterThenNoValidationMessage()
			throws Exception {
		validator.validate(new RecordMetadataValidatorParams(metadata, valuesOf50Character, configProvider, validationErrors, false));

		verifyZeroInteractions(validationErrors);
		verifyZeroInteractions(metadata);
	}

	@Test
	public void whenValuesEmptyThenNoValidationMessage()
			throws Exception {
		validator.validate(new RecordMetadataValidatorParams(metadata, emptyValues, configProvider, validationErrors, false));

		verifyZeroInteractions(validationErrors);
		verifyZeroInteractions(metadata);
	}

}
