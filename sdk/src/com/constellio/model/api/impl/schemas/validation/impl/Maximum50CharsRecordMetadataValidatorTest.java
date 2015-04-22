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
package com.constellio.model.api.impl.schemas.validation.impl;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.sdk.tests.ConstellioTest;

public class Maximum50CharsRecordMetadataValidatorTest extends ConstellioTest {

	String valueOf50Character, valueOf51Character;

	@Mock Metadata metadata;
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
		validator.validate(metadata, valueOf51Character, validationErrors);

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("maxSize", "50");
		parameters.put("wasSize", "51");
		verify(validationErrors).add(Maximum50CharsRecordMetadataValidator.class,
				Maximum50CharsRecordMetadataValidator.VALUE_LENGTH_TOO_LONG, parameters);
		verifyZeroInteractions(metadata);
	}

	@Test
	public void whenValueHas50CharacterThenNoValidationMessage()
			throws Exception {
		validator.validate(metadata, valueOf50Character, validationErrors);

		verifyZeroInteractions(validationErrors);
		verifyZeroInteractions(metadata);
	}

}
