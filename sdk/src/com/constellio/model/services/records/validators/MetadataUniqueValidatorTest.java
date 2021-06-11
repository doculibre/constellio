package com.constellio.model.services.records.validators;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.ManualDataEntry;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.schemas.validators.MetadataUniqueValidator;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MetadataUniqueValidatorTest extends ConstellioTest {
	public static final String NON_UNIQUE_METADATA = MetadataUniqueValidator.class.getName() + "_nonUniqueMetadata";

	String zeId;
	String zeValue = "zeValue";

	@Mock Metadata metadata;
	@Mock MetadataSchemaTypes schemaTypes;
	@Mock MetadataSchemaType schemaType;
	@Mock MetadataSchema schema;
	@Mock Record record;
	@Mock SearchServices searchServices;

	MetadataUniqueValidator validator;
	ValidationErrors validationErrors;

	@Before
	public void setUp() {
		List<Metadata> metadatas = new ArrayList<>();
		metadatas.add(metadata);

		when(metadata.isUniqueValue()).thenReturn(true);
		when(metadata.getCode()).thenReturn("type_default_zeCompleteCode");
		when(schemaTypes.getSchema("type_default")).thenReturn(schema);
		when(schema.getCode()).thenReturn("type_default");
		when(metadata.getType()).thenReturn(MetadataValueType.STRING);
		when(schemaTypes.getSchemaType("type")).thenReturn(schemaType);

		validator = new MetadataUniqueValidator(metadatas, schemaTypes, searchServices);

		validationErrors = new ValidationErrors();

		when(record.getId()).thenReturn(zeId);
		when(record.isActive()).thenReturn(true);
	}

	@Test
	public void givenNonUniqueMetadataThenNotValidated() {
		when(metadata.isUniqueValue()).thenReturn(false);

		validator.validate(record, validationErrors, false);

		verify(record, never()).isModified(metadata);
		verify(searchServices, never()).hasResults(any(LogicalSearchQuery.class));
		assertThat(validationErrors.getValidationErrors()).isEmpty();
	}

	@Test
	public void givenUniqueMetadataIsNotModifiedThenNotValidated() {
		when(record.isModified(metadata)).thenReturn(false);
		when(record.get(metadata)).thenReturn(zeValue);

		validator.validate(record, validationErrors, false);

		verify(searchServices, never()).hasResults(any(LogicalSearchQuery.class));
		assertThat(validationErrors.getValidationErrors()).isEmpty();
	}

	@Test
	public void givenUniqueMetadataIsModifiedButIsNullThenNotValidated() {
		when(record.isModified(metadata)).thenReturn(true);
		when(record.get(metadata)).thenReturn(null);

		validator.validate(record, validationErrors, false);

		verify(searchServices, never()).hasResults(any(LogicalSearchQuery.class));
		assertThat(validationErrors.getValidationErrors()).isEmpty();
	}

	@Test
	public void givenUniqueMetadataAndNoExistingRecordsNoThenError() {
		ArgumentCaptor<LogicalSearchQuery> query = ArgumentCaptor.forClass(LogicalSearchQuery.class);
		when(searchServices.hasResults(query.capture())).thenReturn(false);

		when(record.isModified(metadata)).thenReturn(true);
		when(record.getValues(metadata)).thenReturn(Collections.singletonList(zeValue));

		validator.validate(record, validationErrors, false);

		assertThat(validationErrors.getValidationErrors()).isEmpty();
		assertThat(query.getValue().getCondition()).isEqualTo(
				from(schemaType)
						.where(Schemas.IDENTIFIER).isNotEqual(zeId)
						.andWhere(metadata).isEqualTo(zeValue));
	}

	@Test
	public void givenUniqueMetadataAndExistingRecordsThenError() {
		ArgumentCaptor<LogicalSearchQuery> query = ArgumentCaptor.forClass(LogicalSearchQuery.class);
		when(searchServices.hasResults(query.capture())).thenReturn(true);
		when(record.isModified(metadata)).thenReturn(true);
		when(record.getValues(metadata)).thenReturn(Collections.singletonList(zeValue));
		when(metadata.getDataEntry()).thenReturn(new ManualDataEntry());

		validator.validate(record, validationErrors, false);

		assertThat(validationErrors.getValidationErrors()).hasSize(1);
		assertThat(validationErrors.getValidationErrors().get(0).getCode()).isEqualTo(NON_UNIQUE_METADATA);
		assertThat(query.getValue().getCondition()).isEqualTo(
				from(schemaType)
						.where(Schemas.IDENTIFIER).isNotEqual(zeId)
						.andWhere(metadata).isEqualTo(zeValue));
	}

	@Test
	public void givenUniqueMetadataIsModifiedButIsEmptyThenNotValidated() {
		when(record.isModified(metadata)).thenReturn(true);
		when(record.get(metadata)).thenReturn("");

		validator.validate(record, validationErrors, false);

		verify(searchServices, never()).hasResults(any(LogicalSearchQuery.class));
		assertThat(validationErrors.getValidationErrors()).isEmpty();
	}

	@Test
	public void givenUniqueMultivalueMetadataAndExistingRecordsThenError() {
		ArgumentCaptor<LogicalSearchQuery> query = ArgumentCaptor.forClass(LogicalSearchQuery.class);
		when(searchServices.hasResults(query.capture())).thenReturn(true);
		when(record.getValues(metadata)).thenReturn(Arrays.asList(zeValue));
		when(record.isModified(metadata)).thenReturn(true);
		when(metadata.isMultivalue()).thenReturn(true);
		when(metadata.getDataEntry()).thenReturn(new ManualDataEntry());

		validator.validate(record, validationErrors, false);

		assertThat(validationErrors.getValidationErrors()).hasSize(1);
		assertThat(validationErrors.getValidationErrors().get(0).getCode()).isEqualTo(NON_UNIQUE_METADATA);
		assertThat(query.getValue().getCondition()).isEqualTo(
				from(schemaType)
						.where(Schemas.IDENTIFIER).isNotEqual(zeId)
						.andWhere(metadata).isContaining(Arrays.asList(zeValue)));
	}
}
