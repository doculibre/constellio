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

import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.schemas.validators.ValueRequirementValidator;
import com.constellio.sdk.tests.ConstellioTest;

public class ValueRequirementValidatorTest extends ConstellioTest {

	public static final String REQUIRED_VALUE_FOR_METADATA = "requiredValueForMetadata";
	static final String UNDERSCORE = "_";
	static final String METADATA_CODE = "metadataCode";
	static final String METADATA_LABEL = "metadataLabel";

	@Mock Metadata optionalMetadata;
	@Mock Metadata requiredMetadata1;
	@Mock Metadata requiredMetadata2;

	@Mock Record record;

	ValueRequirementValidator validator;

	String aStringValue = "aStringValue";
	Boolean aBooleanValue = false;
	Integer aNumberValue = 5;

	List<String> aListStringValues;
	List<String> anEmptyList = new ArrayList<String>();

	ValidationErrors validationErrors;

	@Before
	public void setUp() {

		aListStringValues = asList(aStringValue);

		List<Metadata> metadatas = new ArrayList<>();
		metadatas.add(optionalMetadata);
		metadatas.add(requiredMetadata1);
		metadatas.add(requiredMetadata2);

		when(optionalMetadata.isDefaultRequirement()).thenReturn(false);
		when(requiredMetadata1.isDefaultRequirement()).thenReturn(true);
		when(requiredMetadata2.isDefaultRequirement()).thenReturn(true);

		when(optionalMetadata.isEnabled()).thenReturn(true);
		when(requiredMetadata1.isEnabled()).thenReturn(true);
		when(requiredMetadata2.isEnabled()).thenReturn(true);

		validator = new ValueRequirementValidator(metadatas);

		validationErrors = new ValidationErrors();
	}

	@Test
	public void givenAllValuesProvidedWhenValidatingThenEmptyErrorsList() {
		when(record.get(optionalMetadata)).thenReturn(aStringValue);
		when(record.get(requiredMetadata1)).thenReturn(aBooleanValue);
		when(record.get(requiredMetadata2)).thenReturn(aNumberValue);

		validator.validate(record, validationErrors);

		assertThat(validationErrors.getValidationErrors()).isEmpty();
	}

	@Test
	public void givenNoValuesProvidedWhenValidatingThenTwoErrorsInList() {
		when(record.get(optionalMetadata)).thenReturn(null);
		when(record.get(requiredMetadata1)).thenReturn(null);
		when(record.get(requiredMetadata2)).thenReturn(null);

		validator.validate(record, validationErrors);

		assertThat(validationErrors.getValidationErrors()).hasSize(2);
	}

	@Test
	public void givenOptionalMetadataMultivalueWithoutValueWhenValidateThenNoErrorInList()
			throws Exception {
		when(record.get(requiredMetadata1)).thenReturn(aBooleanValue);
		when(record.get(requiredMetadata2)).thenReturn(aNumberValue);
		when(record.get(optionalMetadata)).thenReturn(null);
		when(optionalMetadata.isMultivalue()).thenReturn(true);

		validator.validate(record, validationErrors);

		assertThat(validationErrors.getValidationErrors()).isEmpty();
	}

	@Test
	public void givenOptionalMetadataMultivalueWithAListOfStringValueWhenValidateThenNoErrorInList()
			throws Exception {
		when(record.get(requiredMetadata1)).thenReturn(aBooleanValue);
		when(record.get(requiredMetadata2)).thenReturn(aNumberValue);
		when(record.get(optionalMetadata)).thenReturn(aListStringValues);
		when(optionalMetadata.isMultivalue()).thenReturn(true);

		validator.validate(record, validationErrors);

		assertThat(validationErrors.getValidationErrors()).isEmpty();
	}

	@Test
	public void givenOptionalMetadataSinglevalueWithAStringValueWhenValidateThenNoErrorInList()
			throws Exception {
		when(record.get(requiredMetadata1)).thenReturn(aBooleanValue);
		when(record.get(requiredMetadata2)).thenReturn(aNumberValue);
		when(record.get(optionalMetadata)).thenReturn(aStringValue);
		when(optionalMetadata.isMultivalue()).thenReturn(false);

		validator.validate(record, validationErrors);

		assertThat(validationErrors.getValidationErrors()).isEmpty();
	}

	@Test
	public void givenOptionalMetadataSinglevalueWithoutValueWhenValidateThenNoErrorInList()
			throws Exception {
		when(record.get(requiredMetadata1)).thenReturn(aBooleanValue);
		when(record.get(requiredMetadata2)).thenReturn(aNumberValue);
		when(record.get(optionalMetadata)).thenReturn(null);
		when(optionalMetadata.isMultivalue()).thenReturn(false);

		validator.validate(record, validationErrors);

		assertThat(validationErrors.getValidationErrors()).isEmpty();
	}

	@Test
	public void givenOptionalMetadataMultivalueWithAnEmptyListWhenValidateThenNoErrorInList()
			throws Exception {
		when(record.get(requiredMetadata1)).thenReturn(aStringValue);
		when(record.get(requiredMetadata2)).thenReturn(aNumberValue);
		when(record.get(optionalMetadata)).thenReturn(anEmptyList);
		when(optionalMetadata.isMultivalue()).thenReturn(true);

		validator.validate(record, validationErrors);

		assertThat(validationErrors.getValidationErrors()).isEmpty();
	}

	@Test
	public void givenRequiredMetadataMultivalueWithAnEmptyListWhenValidateThenErrorInList()
			throws Exception {
		when(record.get(requiredMetadata1)).thenReturn(anEmptyList);
		when(record.get(requiredMetadata2)).thenReturn(aNumberValue);
		when(record.get(optionalMetadata)).thenReturn(null);
		when(requiredMetadata1.isMultivalue()).thenReturn(true);

		validator.validate(record, validationErrors);

		assertThat(validationErrors.getValidationErrors()).hasSize(1);
		assertThat(validationErrors.getValidationErrors().get(0).getCode()).isEqualTo(
				validator.getClass().getName() + UNDERSCORE + REQUIRED_VALUE_FOR_METADATA);
		assertThat(validationErrors.getValidationErrors().get(0).getParameters().get(METADATA_CODE)).isEqualTo(
				requiredMetadata1.getCode());
		assertThat(validationErrors.getValidationErrors().get(0).getParameters().get(METADATA_LABEL)).isEqualTo(
				requiredMetadata1.getLabel());
	}
}
