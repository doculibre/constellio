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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.sdk.tests.ConstellioTest;

public class EmailValidatorTest extends ConstellioTest {

	EmailValidator validator = new EmailValidator();
	@Mock Metadata theMetadata;

	@Test
	public void whenValidatingCorrectEmailThenNoError()
			throws Exception {
		ValidationErrors errors = new ValidationErrors();
		validator.validate(theMetadata, "tester@test.com", errors);

		assertThat(errors.getValidationErrors()).isEmpty();
	}

	@Test
	public void whenValidatingIncorrectEmailThenNoError()
			throws Exception {
		ValidationErrors errors = new ValidationErrors();
		validator.validate(theMetadata, "tester@test", errors);

		assertThat(errors.getValidationErrors()).isNotEmpty();
	}
}
