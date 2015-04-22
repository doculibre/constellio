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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.mockito.Mock;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.schemas.validators.MetadataUnmodifiableValidator;
import com.constellio.sdk.tests.ConstellioTest;

public class MetadataUnmodifiableValidatorTest extends ConstellioTest {

	public static final String UNMODIFIABLE_METADATA =
			MetadataUnmodifiableValidator.class.getName() + "_modifiedUnmodifiableMetadata";

	@Mock Metadata metadata;

	@Mock Record record;

	MetadataUnmodifiableValidator validator;

	ValidationErrors validationErrors;

	@Before
	public void setUp() {
		List<Metadata> metadatas = new ArrayList<>();
		metadatas.add(metadata);

		validator = new MetadataUnmodifiableValidator(metadatas);

		validationErrors = new ValidationErrors();
	}

}
