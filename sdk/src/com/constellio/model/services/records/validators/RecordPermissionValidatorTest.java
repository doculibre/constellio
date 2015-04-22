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
package com.constellio.model.services.records.validators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.schemas.validators.RecordPermissionValidator;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.sdk.tests.ConstellioTest;

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

		validator = new RecordPermissionValidator(transaction, authorizationsServices);

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
