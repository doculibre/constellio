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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordImplRuntimeException.RecordImplException_RecordCannotHaveTwoParents;
import com.constellio.model.services.schemas.validators.MetadataChildOfValidator;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;

public class MetadataChildOfValidatorTest extends ConstellioTest {

	public static final String MULTIPLE_PARENTS = MetadataChildOfValidator.class.getName() + "_multipleParentForRecord";

	@Mock Metadata metadata;
	@Mock Metadata parentMetadata;

	@Mock MetadataSchemaTypes schemaTypes;

	@Mock MetadataSchema schema;

	@Mock Record record;

	@Mock SearchServices searchServices;

	MetadataChildOfValidator validator;

	List<Metadata> validParents;
	List<Metadata> invalidParents;

	ValidationErrors validationErrors;

	@Before
	public void setUp() {
		List<Metadata> metadatas = new ArrayList<>();
		metadatas.add(metadata);

		validParents = Arrays.asList(null, null, parentMetadata);
		invalidParents = Arrays.asList(null, parentMetadata, parentMetadata);

		when(record.getSchemaCode()).thenReturn("zeType_zeSchemaCode");
		when(metadata.isChildOfRelationship()).thenReturn(true);
		when(record.getNonNullValueIn(validParents)).thenReturn(null);
		when(record.getNonNullValueIn(invalidParents)).thenThrow(RecordImplException_RecordCannotHaveTwoParents.class);

		when(metadata.getCode()).thenReturn("zeType_zeSchemaCode_zeCompleteCode");
		when(schemaTypes.getSchema("zeType_zeSchemaCode")).thenReturn(schema);

		when(schema.getCode()).thenReturn("zeType_zeSchemaCode");
		when(metadata.getType()).thenReturn(MetadataValueType.STRING);

		validator = new MetadataChildOfValidator(metadatas, schemaTypes);

		validationErrors = new ValidationErrors();
	}

	@Test
	public void givenChildOfMetadataAndOneNotNullParentThenNoError() {
		when(schema.getParentReferences()).thenReturn(validParents);

		validator.validate(record, validationErrors);

		assertThat(validationErrors.getValidationErrors()).isEmpty();
	}

	@Test
	public void givenChildOfMetadataAndTwoNotNullParentThenError() {
		when(schema.getParentReferences()).thenReturn(invalidParents);

		validator.validate(record, validationErrors);

		assertThat(validationErrors.getValidationErrors()).isNotEmpty();
		assertThat(validationErrors.getValidationErrors().get(0).getCode()).isEqualTo(MULTIPLE_PARENTS);
	}

}
