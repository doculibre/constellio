package com.constellio.model.services.schemas.validators;

import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordMetadataValidatorParams;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;

public class EmailValidatorTest extends ConstellioTest {

	EmailValidator validator = new EmailValidator();
	@Mock Metadata theMetadata;
	@Mock ConfigProvider configProvider;

	@Test
	public void whenValidatingCorrectEmailThenNoError()
			throws Exception {
		ValidationErrors errors = new ValidationErrors();
		validator.validate(new RecordMetadataValidatorParams(theMetadata, "tester@test.com", configProvider, errors, false));

		assertThat(errors.getValidationErrors()).isEmpty();
	}

	@Test
	public void whenValidatingIncorrectEmailThenNoError()
			throws Exception {
		ValidationErrors errors = new ValidationErrors();
		validator.validate(new RecordMetadataValidatorParams(theMetadata, "tester@test", configProvider, errors, false));

		assertThat(errors.getValidationErrors()).isNotEmpty();
	}
}
