package com.constellio.model.services.records.validators;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordProvider;
import com.constellio.model.services.schemas.validators.CyclicHierarchyValidator;
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

public class CyclicHierarchyValidatorTest extends ConstellioTest {

	@Mock MetadataSchemaTypes schemaTypes;

	@Mock Metadata metadataWithoutReferences;
	@Mock Metadata metadataWithAReference;
	@Mock Metadata metadataWithAChildOfReference;
	@Mock Metadata metadataWithTwoReferences;
	@Mock Metadata metadataWithAlistOfReference;

	@Mock AllowedReferences oneAllowedReferences;
	@Mock AllowedReferences twoAllowedReferences;

	@Mock MetadataSchema nonAllowedSchema;
	@Mock MetadataSchema anAllowedSchema;
	@Mock MetadataSchema anotherAllowedSchema;

	@Mock Record record;

	CyclicHierarchyValidator validator;

	@Mock RecordProvider recordProvider;

	@Mock Record anAllowedRecord;

	@Mock Record anUnallowedRecord;

	@Mock Record descendantRecord;

	String theRecordId = "theRecordId";

	List<String> aListOfAllowedReferencesCodes = new ArrayList<String>();

	@Before
	public void setUp() {

		when(record.getId()).thenReturn(theRecordId);

		aListOfAllowedReferencesCodes.add("anAllowedRecordId");

		when(record.getSchemaCode()).thenReturn("anAllowedSchemaCode");
		when(descendantRecord.getSchemaCode()).thenReturn("anAllowedSchemaCode");
		when(anAllowedRecord.getSchemaCode()).thenReturn("anAllowedSchemaCode");
		when(anUnallowedRecord.getSchemaCode()).thenReturn("nonAllowedSchemaCode");

		when(recordProvider.getRecord("theRecordId")).thenReturn(record);
		when(recordProvider.getRecord("aDescendantId")).thenReturn(descendantRecord);
		when(recordProvider.getRecord("anAllowedRecordId")).thenReturn(anAllowedRecord);
		when(recordProvider.getRecord("anUnallowedRecordId")).thenReturn(anUnallowedRecord);

		when(recordProvider.getRecordSummary("theRecordId")).thenReturn(record);
		when(recordProvider.getRecordSummary("aDescendantId")).thenReturn(descendantRecord);
		when(recordProvider.getRecordSummary("anAllowedRecordId")).thenReturn(anAllowedRecord);
		when(recordProvider.getRecordSummary("anUnallowedRecordId")).thenReturn(anUnallowedRecord);

		when(anAllowedRecord.get(Schemas.PRINCIPAL_PATH)).thenReturn("/concept/anAllowedRecordId");
		when(descendantRecord.get(Schemas.PRINCIPAL_PATH)).thenReturn("/concept/theRecordId/aDescendantId");

		when(anAllowedRecord.getParentId(any(MetadataSchema.class))).thenReturn("anAllowedRecordId");
		when(descendantRecord.getParentId(any(MetadataSchema.class))).thenReturn("theRecordId");

		when(metadataWithoutReferences.getType()).thenReturn(MetadataValueType.STRING);
		when(metadataWithAReference.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(metadataWithTwoReferences.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(metadataWithAChildOfReference.getType()).thenReturn(MetadataValueType.REFERENCE);
		when(metadataWithAlistOfReference.getType()).thenReturn(MetadataValueType.REFERENCE);

		when(record.isModified(metadataWithoutReferences)).thenReturn(true);
		when(record.isModified(metadataWithAReference)).thenReturn(true);
		when(record.isModified(metadataWithTwoReferences)).thenReturn(true);
		when(record.isModified(metadataWithAChildOfReference)).thenReturn(true);
		when(record.isModified(metadataWithAlistOfReference)).thenReturn(true);

		when(metadataWithAlistOfReference.isMultivalue()).thenReturn(true);
		when(metadataWithAChildOfReference.isChildOfRelationship()).thenReturn(true);

		List<Metadata> metadatas = new ArrayList<>();
		metadatas.add(metadataWithoutReferences);
		metadatas.add(metadataWithAReference);
		metadatas.add(metadataWithAChildOfReference);
		metadatas.add(metadataWithTwoReferences);
		metadatas.add(metadataWithAlistOfReference);

		when(schemaTypes.getSchema("nonAllowedSchemaCode")).thenReturn(nonAllowedSchema);
		when(schemaTypes.getSchema("anAllowedSchemaCode")).thenReturn(anAllowedSchema);
		when(schemaTypes.getSchema("anotherAllowedSchemaCode")).thenReturn(anotherAllowedSchema);

		when(oneAllowedReferences.isAllowed(anAllowedSchema)).thenReturn(true);
		when(twoAllowedReferences.isAllowed(anAllowedSchema)).thenReturn(true);
		when(twoAllowedReferences.isAllowed(anotherAllowedSchema)).thenReturn(true);

		when(metadataWithAReference.getAllowedReferences()).thenReturn(oneAllowedReferences);
		when(metadataWithTwoReferences.getAllowedReferences()).thenReturn(twoAllowedReferences);
		when(metadataWithTwoReferences.getAllowedReferences()).thenReturn(oneAllowedReferences);
		when(metadataWithAlistOfReference.getAllowedReferences()).thenReturn(oneAllowedReferences);
		when(metadataWithAChildOfReference.getAllowedReferences()).thenReturn(oneAllowedReferences);

		validator = new CyclicHierarchyValidator(schemaTypes, metadatas, recordProvider);
	}

	@Test
	public void givenARecordIsReferencingADescendantInAReferenceMetadataThenNoError() {

		when(record.get(metadataWithAlistOfReference)).thenReturn(Arrays.asList("aDescendantId"));

		ValidationErrors validationErrors = new ValidationErrors();

		validator.validate(record, validationErrors);

		assertThat(validationErrors.getValidationErrors()).isEmpty();
	}

	@Test
	public void givenARecordIsReferencingADescendantInAChildOfReferenceMetadataThenError() {

		when(record.get(metadataWithAChildOfReference)).thenReturn("aDescendantId");

		ValidationErrors validationErrors = new ValidationErrors();

		validator.validate(record, validationErrors);

		assertThat(validationErrors.getValidationErrors()).hasSize(1);
		assertThat(validationErrors.getValidationErrors().get(0).getCode())
				.endsWith(CyclicHierarchyValidator.CANNOT_REFERENCE_A_DESCENDANT_IN_A_CHILD_OF_REFERENCE);
	}

	@Test
	public void givenARecordIsReferencingAnotherRecordInAChildOfReferenceMetadataThenOk() {

		when(record.get(metadataWithAChildOfReference)).thenReturn("anAllowedRecordId");

		ValidationErrors validationErrors = new ValidationErrors();

		validator.validate(record, validationErrors);

		assertThat(validationErrors.getValidationErrors()).isEmpty();
	}

	@Test
	public void givenARecordWithIdInSubstringOfPrincipalPathThenNoError() {

		when(record.get(metadataWithAChildOfReference)).thenReturn("anAllowedRecordId");
		when(anAllowedRecord.get(Schemas.PRINCIPAL_PATH)).thenReturn("/anAllowedRecordIdanAllowedRecordId");

		ValidationErrors validationErrors = new ValidationErrors();

		validator.validate(record, validationErrors);

		assertThat(validationErrors.getValidationErrors()).isEmpty();

	}

}
