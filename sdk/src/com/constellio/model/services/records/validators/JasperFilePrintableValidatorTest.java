package com.constellio.model.services.records.validators;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordMetadataValidatorParams;
import com.constellio.model.services.schemas.validators.JasperFilePrintableValidator;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JasperFilePrintableValidatorTest extends ConstellioTest {

	private ValidationErrors validationErrors;
	private JasperFilePrintableValidator validator;
	@Mock Content correctContentName;
	@Mock Content incorrectValue;
	@Mock ContentVersion correctContentVersion;
	@Mock ContentVersion incorrectContentVersion;

	@Before
	public void Setup() {
		validator = new JasperFilePrintableValidator();
		correctContentName = mock(Content.class);
		incorrectValue = mock(Content.class);

		correctContentVersion = mock(ContentVersion.class);
		incorrectContentVersion = mock(ContentVersion.class);

		when(correctContentVersion.getFilename()).thenReturn("correctValue.jasper");
		when(incorrectContentVersion.getFilename()).thenReturn("test.test");

		when(correctContentName.getCurrentVersion()).thenReturn(correctContentVersion);
		when(incorrectValue.getCurrentVersion()).thenReturn(incorrectContentVersion);
	}

	@Test
	public void givenCorrectValueReturnsEmptyValidationError() {
		validationErrors = new ValidationErrors();

		validator.validate(new RecordMetadataValidatorParams(null, correctContentName, null, validationErrors, false));

		assertThat(validationErrors.getValidationErrors()).isEmpty();
	}

	@Test
	public void givenIncorrectValueReturnsValidationErrors() {
		validationErrors = new ValidationErrors();

		validator.validate(new RecordMetadataValidatorParams(null, incorrectValue, null, validationErrors, false));

		assertThat(validationErrors.getValidationErrors()).hasSize(1);
		assertThat(validationErrors.getValidationErrors().get(0).getValidatorClass()).isEqualTo(JasperFilePrintableValidator.class);
	}
}
