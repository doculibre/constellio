package com.constellio.model.services.records.validators;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.schemas.validators.RecordPermissionValidator;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

public class RecordPermissionValidatorTest extends ConstellioTest {

	public static final String UNAUTHORIZED =
			RecordPermissionValidator.class.getName() + "_userHasNoWriteAccess";

	@Mock Record record;

	@Mock Transaction transaction;

	@Mock User user;

	@Mock AuthorizationsServices authorizationsServices;

	RecordPermissionValidator validator;

	ValidationErrors validationErrors;

	@Before
	public void setUp() {

		given(transaction.getUser()).willReturn(user);

		validator = new RecordPermissionValidator(transaction, authorizationsServices, false);

		validationErrors = new ValidationErrors();
	}

	@Test
	public void givenWriteAccessThenNoError() {
		given(authorizationsServices.canWrite(user, record)).willReturn(true);

		validator.validate(record, validationErrors);

		assertThat(validationErrors.getValidationErrors()).isEmpty();
	}

	@Test
	public void givenNoWriteAccessThenError() {
		given(authorizationsServices.canWrite(user, record)).willReturn(false);
		when(record.isDirty()).thenReturn(true);
		when(record.isModified(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(false);

		validator.validate(record, validationErrors);

		assertThat(validationErrors.getValidationErrors()).isNotEmpty();
		assertThat(validationErrors.getValidationErrors().get(0).getCode()).isEqualTo(UNAUTHORIZED);
	}

	@Test
	public void givenNoWriteAccessAndRecordNotDirtyThenNoExceptionThenError() {
		given(authorizationsServices.canWrite(user, record)).willReturn(false);
		when(record.isDirty()).thenReturn(false);

		validator.validate(record, validationErrors);

		assertThat(validationErrors.getValidationErrors()).isEmpty();
	}

	@Test
	public void givenRecordIsLogicallyDeletedThenNoError() {
		given(authorizationsServices.canWrite(user, record)).willReturn(false);
		when(record.isDirty()).thenReturn(true);
		when(record.isModified(Schemas.LOGICALLY_DELETED_STATUS)).thenReturn(true);

		validator.validate(record, validationErrors);

		assertThat(validationErrors.getValidationErrors()).isEmpty();
	}

	@Test
	public void givenNullUserThenNoError() {
		given(transaction.getUser()).willReturn(null);

		validator.validate(record, validationErrors);

		given(authorizationsServices.canWrite(user, record)).willReturn(true);
		assertThat(validationErrors.getValidationErrors()).isEmpty();
	}
}
