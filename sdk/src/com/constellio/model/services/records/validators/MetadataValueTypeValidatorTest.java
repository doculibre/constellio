package com.constellio.model.services.records.validators;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.schemas.validators.MetadataValueTypeValidator;
import com.constellio.sdk.tests.ConstellioTest;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class MetadataValueTypeValidatorTest extends ConstellioTest {

	static final String UNDERSCORE = "_";
	static final String MULTI_VALUES_NOT_ALLOWED_IN_SINGLE_VALUE_METADATA = "multiValuesNotAllowedInSingleValueMetadata";
	static final String SINGLE_VALUE_NOT_ALLOWED_IN_MULTI_VALUES_METADATA = "singleValueNotAllowedInMultiValuesMetadata";
	static final String METADATA_CODE = "metadataCode";
	static final String METADATA_LABEL = "metadataLabel";

	@Mock Metadata textMetadata;
	@Mock Metadata booleanMetadata;
	@Mock Metadata aNumberMetadata;
	@Mock Metadata dateMetadata;
	@Mock Metadata referenceMetadata;

	@Mock Record record;

	MetadataValueTypeValidator validator;

	String aStringValue = "aStringValue";
	Boolean aBooleanValue = false;
	Integer aNumberValue = 5;
	LocalDateTime aDateValue = new LocalDateTime();
	String aReferenceValue = "aReferenceValue";

	List<String> aListOfStringValues = new ArrayList<String>();
	List<Boolean> aListOfBooleanValues = new ArrayList<Boolean>();
	List<Integer> aListOfNumberValues = new ArrayList<Integer>();
	List<LocalDateTime> aListOfDateValues = new ArrayList<LocalDateTime>();

	@SuppressWarnings("rawtypes") List anEmptyList = new ArrayList();
	ValidationErrors validationErrors;

	@Before
	public void setUp() {

		aListOfStringValues.add(aStringValue);
		aListOfBooleanValues.add(aBooleanValue);
		aListOfNumberValues.add(aNumberValue);
		aListOfDateValues.add(aDateValue);

		List<Metadata> metadatas = new ArrayList<>();
		metadatas.add(textMetadata);
		metadatas.add(booleanMetadata);
		metadatas.add(aNumberMetadata);
		metadatas.add(dateMetadata);
		metadatas.add(referenceMetadata);
		when(textMetadata.getType()).thenReturn(MetadataValueType.STRING);
		when(booleanMetadata.getType()).thenReturn(MetadataValueType.BOOLEAN);
		when(aNumberMetadata.getType()).thenReturn(MetadataValueType.NUMBER);
		when(dateMetadata.getType()).thenReturn(MetadataValueType.DATE_TIME);
		when(referenceMetadata.getType()).thenReturn(MetadataValueType.REFERENCE);

		validator = new MetadataValueTypeValidator(metadatas, false);

		validationErrors = new ValidationErrors();
	}

	@Test
	public void givenAllValuesAreTheRightTypeWhenValidatingThenEmptyErrorsList() {
		when(record.get(textMetadata)).thenReturn(aStringValue);
		when(record.get(booleanMetadata)).thenReturn(aBooleanValue);
		when(record.get(aNumberMetadata)).thenReturn(aNumberValue);
		when(record.get(dateMetadata)).thenReturn(aDateValue);
		when(record.get(referenceMetadata)).thenReturn(aReferenceValue);

		validator.validate(record, validationErrors);

		assertThat(validationErrors.getValidationErrors()).isEmpty();
	}

	@Test
	public void givenAllValuesAreTheWrongTypeWhenValidatingThenSixErrorsInList() {
		when(record.get(textMetadata)).thenReturn(aDateValue);
		when(record.get(booleanMetadata)).thenReturn(aReferenceValue);
		when(record.get(aNumberMetadata)).thenReturn(aStringValue);
		when(record.get(dateMetadata)).thenReturn(aNumberValue);
		when(record.get(referenceMetadata)).thenReturn(aDateValue);

		validator.validate(record, validationErrors);

		assertThat(validationErrors.getValidationErrors()).hasSize(5);
	}

	@Test
	public void givenAllMetadatasAsMultivalueWithAListOfRightTypeValuesWhenValidateThenNoErrorInList()
			throws Exception {
		when(record.get(textMetadata)).thenReturn(aListOfStringValues);
		when(record.get(booleanMetadata)).thenReturn(aListOfBooleanValues);
		when(record.get(aNumberMetadata)).thenReturn(aListOfNumberValues);
		when(record.get(dateMetadata)).thenReturn(aListOfDateValues);
		when(record.get(referenceMetadata)).thenReturn(aListOfStringValues);

		when(textMetadata.isMultivalue()).thenReturn(true);
		when(booleanMetadata.isMultivalue()).thenReturn(true);
		when(aNumberMetadata.isMultivalue()).thenReturn(true);
		when(dateMetadata.isMultivalue()).thenReturn(true);
		when(referenceMetadata.isMultivalue()).thenReturn(true);

		validator.validate(record, validationErrors);

		assertThat(validationErrors.getValidationErrors()).hasSize(0);
	}

	@Test
	public void givenAllMetadatasAsMultivalueWithAListOfWrongTypeValuesWhenValidateThenErrorsInList()
			throws Exception {
		when(record.get(textMetadata)).thenReturn(aListOfBooleanValues);
		when(record.get(booleanMetadata)).thenReturn(aListOfStringValues);
		when(record.get(aNumberMetadata)).thenReturn(aListOfDateValues);
		when(record.get(dateMetadata)).thenReturn(aListOfNumberValues);
		when(record.get(referenceMetadata)).thenReturn(aListOfBooleanValues);

		when(textMetadata.isMultivalue()).thenReturn(true);
		when(booleanMetadata.isMultivalue()).thenReturn(true);
		when(aNumberMetadata.isMultivalue()).thenReturn(true);
		when(dateMetadata.isMultivalue()).thenReturn(true);
		when(referenceMetadata.isMultivalue()).thenReturn(true);

		validator.validate(record, validationErrors);

		assertThat(validationErrors.getValidationErrors()).hasSize(5);
	}
}
