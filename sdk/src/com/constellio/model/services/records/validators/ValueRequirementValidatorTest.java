package com.constellio.model.services.records.validators;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.entries.ManualDataEntry;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordAutomaticMetadataServices;
import com.constellio.model.services.schemas.validators.ValueRequirementValidator;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.constellio.sdk.tests.TestUtils.asMap;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class ValueRequirementValidatorTest extends ConstellioTest {

	public static final String REQUIRED_VALUE_FOR_METADATA = "requiredValueForMetadata";
	static final String UNDERSCORE = "_";
	static final String METADATA_CODE = "metadataCode";
	static final String METADATA_LABEL = "metadataLabel";

	@Mock Metadata optionalMetadata;
	@Mock Metadata requiredMetadata1;
	@Mock Metadata requiredMetadata2;

	@Mock Record record;

	@Mock RecordAutomaticMetadataServices recordAutomaticMetadataServices;

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

		when(optionalMetadata.getLocalCode()).thenReturn("localCode1");
		when(requiredMetadata1.getLocalCode()).thenReturn("localCode2");
		when(requiredMetadata2.getLocalCode()).thenReturn("localCode3");

		when(optionalMetadata.getDataEntry()).thenReturn(new ManualDataEntry());
		when(requiredMetadata1.getDataEntry()).thenReturn(new ManualDataEntry());
		when(requiredMetadata2.getDataEntry()).thenReturn(new ManualDataEntry());

		when(recordAutomaticMetadataServices.isValueAutomaticallyFilled(any(Metadata.class), any(Record.class)))
				.thenReturn(false);

		validator = new ValueRequirementValidator(metadatas, false, recordAutomaticMetadataServices, true);

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
		when(requiredMetadata1.getLabelsByLanguageCodes()).thenReturn(asMap("fr", "ze French label"));

		validator.validate(record, validationErrors);

		assertThat(validationErrors.getValidationErrors()).hasSize(1);
		assertThat(validationErrors.getValidationErrors().get(0).getCode()).isEqualTo(
				validator.getClass().getName() + UNDERSCORE + REQUIRED_VALUE_FOR_METADATA);
		assertThat(validationErrors.getValidationErrors().get(0).getParameters().get(METADATA_CODE)).isEqualTo(
				requiredMetadata1.getCode());
		assertThat((Map<String, String>) validationErrors.getValidationErrors().get(0).getParameters().get(METADATA_LABEL))
				.containsOnly(
						entry("fr", "ze French label")
				);
	}
}
