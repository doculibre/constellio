package com.constellio.model.services.records.validators;

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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

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
		when(schemaTypes.getSchemaOf(any(Record.class))).thenReturn(schema);

		when(schema.getCode()).thenReturn("zeType_zeSchemaCode");
		when(metadata.getType()).thenReturn(MetadataValueType.STRING);

		validator = new MetadataChildOfValidator(metadatas, schemaTypes, false);

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
