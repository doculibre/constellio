package com.constellio.model.api.impl.schemas.validation.impl;

import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordMetadataValidatorParams;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class Maximum50CharsRecordMetadataValidatorTest extends ConstellioTest {

	String valueOf50Character, valueOf51Character;

	@Mock Metadata metadata;
	@Mock ConfigProvider configProvider;
	@Mock ValidationErrors validationErrors;

	Maximum50CharsRecordMetadataValidator validator;

	@Before
	public void setUp() {
		validator = new Maximum50CharsRecordMetadataValidator();
		valueOf50Character = "12345678901234567890123456789012345678901234567890";
		valueOf51Character = "123456789012345678901234567890123456789012345678901";
	}

	@Test
	public void whenValueHasMoreThan50CharacterThenAddValidationMessage()
			throws Exception {
		validator.validate(new RecordMetadataValidatorParams(metadata, valueOf51Character, configProvider, validationErrors, false));

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("maxSize", "50");
		parameters.put("wasSize", "51");
		verify(validationErrors).add(Maximum50CharsRecordMetadataValidator.class,
				Maximum50CharsRecordMetadataValidator.VALUE_LENGTH_TOO_LONG, parameters);
		verifyZeroInteractions(metadata);
	}

	@Test
	public void whenValueHas50CharacterThenNoValidationMessage()
			throws Exception {
		validator.validate(new RecordMetadataValidatorParams(metadata, valueOf50Character, configProvider, validationErrors, false));

		verifyZeroInteractions(validationErrors);
		verifyZeroInteractions(metadata);
	}

}
