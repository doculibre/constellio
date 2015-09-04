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
package com.constellio.app.modules.rm.wrappers.structures;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.data.utils.TimeProvider;
import com.constellio.sdk.tests.ConstellioTest;

public class DecomListValidationTest extends ConstellioTest {
	public static final String THE_USER = "theUserId";

	DecomListValidationFactory factory;
	DecomListValidation validation;

	@Before
	public void setUp() {
		factory = new DecomListValidationFactory();
	}

	@Test
	public void whenSetAttributeValueThenBecomeDirty() {
		validation = new DecomListValidation();
		assertThat(validation.isDirty()).isFalse();

		validation = new DecomListValidation();
		validation.setUserId(THE_USER);
		assertThat(validation.isDirty()).isTrue();

		validation = new DecomListValidation();
		validation.setRequestDate(TimeProvider.getLocalDate());
		assertThat(validation.isDirty()).isTrue();

		validation = new DecomListValidation();
		validation.setValidationDate(TimeProvider.getLocalDate());
		assertThat(validation.isDirty()).isTrue();
	}

	@Test
	public void whenConvertingStructureWithAllValuesThenRemainsEqual()
			throws Exception {
		validation = new DecomListValidation()
				.setUserId(THE_USER)
				.setRequestDate(TimeProvider.getLocalDate().minusDays(1))
				.setValidationDate(TimeProvider.getLocalDate());

		String stringValue = factory.toString(validation);
		DecomListValidation deserialized = (DecomListValidation) factory.build(stringValue);

		assertThat(deserialized).isEqualTo(validation);
		assertThat(deserialized.isDirty()).isFalse();
	}

	@Test
	public void whenConvertingStructureWithNullValuesThenRemainsEqual()
			throws Exception {
		validation = new DecomListValidation();

		String stringValue = factory.toString(validation);
		DecomListValidation deserialized = (DecomListValidation) factory.build(stringValue);

		assertThat(deserialized).isEqualTo(validation);
	}
}
