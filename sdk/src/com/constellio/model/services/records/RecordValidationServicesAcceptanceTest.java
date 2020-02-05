package com.constellio.model.services.records;

import com.constellio.model.api.impl.schemas.validation.impl.CreationDateIsBeforeOrEqualToLastModificationDateValidator;
import com.constellio.model.api.impl.schemas.validation.impl.Maximum50CharsRecordMetadataValidator;
import com.constellio.model.api.impl.schemas.validation.impl.Maximum50CharsRecordMultivalueMetadataValidator;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.validators.AllowedReferencesValidator;
import com.constellio.model.services.schemas.validators.CyclicHierarchyValidator;
import com.constellio.model.services.schemas.validators.MaskedMetadataValidator;
import com.constellio.model.services.schemas.validators.MetadataValueTypeValidator;
import com.constellio.model.services.schemas.validators.ValueRequirementValidator;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ThirdSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import com.constellio.sdk.tests.setups.Users;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Condition;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.api.impl.schemas.validation.impl.Maximum50CharsRecordMetadataValidator.MAX_SIZE;
import static com.constellio.model.api.impl.schemas.validation.impl.Maximum50CharsRecordMetadataValidator.VALUE_LENGTH_TOO_LONG;
import static com.constellio.model.api.impl.schemas.validation.impl.Maximum50CharsRecordMetadataValidator.WAS_SIZE;
import static com.constellio.model.services.schemas.validators.MaskedMetadataValidator.VALUE_INCOMPATIBLE_WITH_SPECIFIED_MASK;
import static com.constellio.model.services.schemas.validators.MetadataValueTypeValidator.EXPECTED_TYPE_MESSAGE_PARAM;
import static com.constellio.model.services.schemas.validators.MetadataValueTypeValidator.INVALID_VALUE_FOR_METADATA;
import static com.constellio.model.services.schemas.validators.MetadataValueTypeValidator.METADATA_CODE_MESSAGE_PARAM;
import static com.constellio.model.services.schemas.validators.MetadataValueTypeValidator.WAS_VALUE_CLASS_MESSAGE_PARAM;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.limitedTo50Characters;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichAllowsAnotherDefaultSchema;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasDefaultRequirement;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasInputMask;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasLabel;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivaluesAndLimitedTo50Characters;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichMaxLengthIs7;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RecordValidationServicesAcceptanceTest extends ConstellioTest {

	Record record;
	RecordValidationServices services;
	@Mock Transaction transaction;
	@Mock RecordUpdateOptions recordUpdateOptions;
	@Mock ConfigProvider configProvider;

	LocalDateTime january1_2010 = new LocalDateTime(2010, 1, 1, 0, 0);
	LocalDateTime january1_2011 = new LocalDateTime(2011, 1, 1, 0, 0);
	String correctStringValue = "ze title is ok";
	List<String> correctStringValues = Arrays.asList("ze value is ok", "ze other value is also ok");
	String titleTooLong = "this title is too lonnnnnnnnnnnnnnnnnnnnnnnnnnnnnng";
	List<String> valuesToLong = Arrays.asList("ze value is ok", "this title is too lonnnnnnnnnnnnnnnnnnnnnnnnnnnnnng");

	RecordValidationServicesTestsSetup schemas;
	ZeSchemaMetadatas zeSchema;
	AnotherSchemaMetadatas anotherSchema;
	ThirdSchemaMetadatas thirdSchema;
	Users users = new Users();

	RecordServices recordServices;

	RecordProvider recordProvider;

	@Before
	public void setUp() {
		prepareSystem(withZeCollection().withAllTest(users));
		schemas = new RecordValidationServicesTestsSetup();
		zeSchema = schemas.new ZeSchemaMetadatas();
		anotherSchema = schemas.new AnotherSchemaMetadatas();
		thirdSchema = schemas.new ThirdSchemaMetadatas();

		services = new RecordValidationServices(configProvider, recordProvider,
				getModelLayerFactory().getMetadataSchemasManager(),
				getModelLayerFactory().newSearchServices(), getModelLayerFactory().newAuthorizationsServices(),
				new RecordAutomaticMetadataServices(getModelLayerFactory()));

		recordServices = getModelLayerFactory().newCachelessRecordServices();
		recordProvider = getModelLayerFactory().newCachelessRecordServices().newRecordProvider(null, new Transaction());
		when(transaction.getRecordUpdateOptions()).thenReturn(recordUpdateOptions);
	}

	@Test
	public void givenRecordMetadataValidatorPassingWhenValidatingThenEmptyErrorList()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(limitedTo50Characters));
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		record.set(zeSchema.stringMetadata(), correctStringValue);
		List<ValidationError> errors = services.validateUsingCustomSchemaValidatorsReturningErrors(record).getValidationErrors();

		assertThat(errors).isEmpty();
	}

	@Test
	public void givenRecordMetadataValidatorFailingWhenValidatingThenOneErrorInList()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(limitedTo50Characters));
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		record.set(zeSchema.stringMetadata(), titleTooLong);
		List<ValidationError> errors = services.validateUsingCustomSchemaValidatorsReturningErrors(record).getValidationErrors();

		assertThat(errors).hasSize(1);
		assertThat(errors.get(0)).has(codeBasedOn(Maximum50CharsRecordMetadataValidator.class, VALUE_LENGTH_TOO_LONG));
		assertThat(errors.get(0).getParameters()).containsEntry(MAX_SIZE, "50").containsEntry(WAS_SIZE, "51")
				.containsEntry(RecordMetadataValidator.METADATA_CODE, zeSchema.stringMetadata().getCode())
				.containsEntry(RecordMetadataValidator.METADATA_VALUE, titleTooLong);

	}

	@Test
	public void givenRecordMetadataValidatorPassingWhenValidatingMultivalueThenEmptyErrorList()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(whichIsMultivaluesAndLimitedTo50Characters));
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		record.set(zeSchema.stringMetadata(), correctStringValues);
		List<ValidationError> errors = services.validateUsingCustomSchemaValidatorsReturningErrors(record).getValidationErrors();

		assertThat(errors).isEmpty();
	}

	@Test
	public void givenRecordMetadataValidatorFailingWhenValidatingMultivalueThenOneErrorInList()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(whichIsMultivaluesAndLimitedTo50Characters));
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		record.set(zeSchema.stringMetadata(), valuesToLong);
		List<ValidationError> errors = services.validateUsingCustomSchemaValidatorsReturningErrors(record).getValidationErrors();

		String expectedConcatenatedStringValues = "[" + StringUtils.join(valuesToLong, ", ") + "]";
		assertThat(errors).hasSize(1);
		assertThat(errors.get(0)).has(codeBasedOn(Maximum50CharsRecordMultivalueMetadataValidator.class, VALUE_LENGTH_TOO_LONG));
		assertThat(errors.get(0).getParameters()).containsEntry(MAX_SIZE, "50").containsEntry(WAS_SIZE, "51")
				.containsEntry(RecordMetadataValidator.METADATA_CODE, zeSchema.stringMetadata().getCode())
				.containsEntry(RecordMetadataValidator.METADATA_VALUE, expectedConcatenatedStringValues);

	}

	@Test
	public void givenRecordValidatorPassingWhenValidatingThenEmptyErrorList()
			throws Exception {
		defineSchemasManager().using(schemas.withCreationAndModificationDateInZeSchema());
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		record.set(zeSchema.creationDate(), january1_2010);
		record.set(zeSchema.modificationDate(), january1_2011);

		List<ValidationError> errors = services.validateUsingCustomSchemaValidatorsReturningErrors(record).getValidationErrors();

		assertThat(errors).isEmpty();
	}

	@Test
	public void givenRecordValidatorFailingWhenValidatingThenOneErrorInList()
			throws Exception {
		defineSchemasManager().using(schemas.withCreationAndModificationDateInZeSchema());
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		record.set(zeSchema.creationDate(), january1_2011);
		record.set(zeSchema.modificationDate(), january1_2010);

		List<ValidationError> errors = services.validateUsingCustomSchemaValidatorsReturningErrors(record).getValidationErrors();

		assertThat(errors).hasSize(1);
		assertThat(errors.get(0)).has(
				codeBasedOn(CreationDateIsBeforeOrEqualToLastModificationDateValidator.class,
						CreationDateIsBeforeOrEqualToLastModificationDateValidator.CREATION_DATE_IS_AFTER_MODIFICATION_DATE));
		assertThat(errors.get(0).getParameters()).containsEntry(
				CreationDateIsBeforeOrEqualToLastModificationDateValidator.CREATION_DATE_MESSAGE_PARAM, january1_2011.toString())
				.containsEntry(CreationDateIsBeforeOrEqualToLastModificationDateValidator.MODIFICATION_DATE_MESSAGE_PARAM,
						january1_2010.toString());
	}

	@Test
	public void givenStringTypeValidatorPassingWhenValidatingThenEmptyErrorList()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata());
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		record.set(zeSchema.stringMetadata(), "aValidStringValue");
		List<ValidationError> errors = services.validateManualMetadatasReturningErrors(record, recordProvider, transaction)
				.getValidationErrors();

		assertThat(errors).isEmpty();
	}

	@Test
	public void givenMultivalueStringTypeValidatorPassingWhenValidatingMultivalueThenEmptyErrorList()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(whichIsMultivalue));
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		record.set(zeSchema.stringMetadata(), Arrays.asList("aValidStringValue"));
		List<ValidationError> errors = services.validateManualMetadatasReturningErrors(record, recordProvider, transaction)
				.getValidationErrors();

		assertThat(errors).isEmpty();
	}

	@Test
	public void givenStringTypeValidatorFailingWhenValidatingThenOneErrorInList()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata());
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		try {
			record.set(zeSchema.stringMetadata(), 1);
			fail("Exception expected");
		} catch (IllegalArgumentException e) {
			//OK
		}
//		List<ValidationError> errors = services.validateManualMetadatasReturningErrors(record, recordProvider, transaction)
		//				.getValidationErrors();
		//
		//		assertThat(errors).hasSize(1);
		//		assertThat(errors.get(0)).has(codeBasedOn(MetadataValueTypeValidator.class, INVALID_VALUE_FOR_METADATA));
		//		assertThat(errors.get(0).getParameters()).containsEntry(EXPECTED_TYPE_MESSAGE_PARAM, "STRING")
		//				.containsEntry(METADATA_CODE_MESSAGE_PARAM, zeSchema.stringMetadata().getCode())
		//				.containsEntry(WAS_VALUE_CLASS_MESSAGE_PARAM, "java.lang.Double");

	}

	@Test
	public void givenMultipleStringTypeValidatorFailingForElementWhenValidatingThenOneErrorInList()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(whichIsMultivalue));
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		record.set(zeSchema.stringMetadata(), Arrays.asList("aStringValue", 1));
		List<ValidationError> errors = services.validateManualMetadatasReturningErrors(record, recordProvider, transaction)
				.getValidationErrors();

		assertThat(errors).hasSize(1);
		assertThat(errors.get(0)).has(codeBasedOn(MetadataValueTypeValidator.class, INVALID_VALUE_FOR_METADATA));
		assertThat(errors.get(0).getParameters()).containsEntry(EXPECTED_TYPE_MESSAGE_PARAM, "STRING")
				.containsEntry(METADATA_CODE_MESSAGE_PARAM, zeSchema.stringMetadata().getCode())
				.containsEntry(WAS_VALUE_CLASS_MESSAGE_PARAM, "java.lang.Integer");

	}

	@Test
	public void givenMultipleStringTypeValidatorFailingForMultipleElementWhenValidatingThenMultipleErrorInList()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(whichIsMultivalue));
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		record.set(zeSchema.stringMetadata(), Arrays.asList("aStringValue", 1, true));
		List<ValidationError> errors = services.validateManualMetadatasReturningErrors(record, recordProvider, transaction)
				.getValidationErrors();

		assertThat(errors).hasSize(2);
		assertThat(errors.get(0)).has(codeBasedOn(MetadataValueTypeValidator.class, INVALID_VALUE_FOR_METADATA));
		assertThat(errors.get(0).getParameters()).containsEntry(EXPECTED_TYPE_MESSAGE_PARAM, "STRING")
				.containsEntry(METADATA_CODE_MESSAGE_PARAM, zeSchema.stringMetadata().getCode())
				.containsEntry(WAS_VALUE_CLASS_MESSAGE_PARAM, "java.lang.Integer");
		assertThat(errors.get(1)).has(codeBasedOn(MetadataValueTypeValidator.class, INVALID_VALUE_FOR_METADATA));
		assertThat(errors.get(1).getParameters()).containsEntry(EXPECTED_TYPE_MESSAGE_PARAM, "STRING")
				.containsEntry(METADATA_CODE_MESSAGE_PARAM, zeSchema.stringMetadata().getCode())
				.containsEntry(WAS_VALUE_CLASS_MESSAGE_PARAM, "java.lang.Boolean");

	}

	@Test
	public void givenDateTypeValidatorPassingWhenValidatingThenEmptyErrorList()
			throws Exception {
		defineSchemasManager().using(schemas.withADateTimeMetadata());
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		record.set(zeSchema.dateTimeMetadata(), new LocalDateTime());
		List<ValidationError> errors = services.validateManualMetadatasReturningErrors(record, recordProvider, transaction)
				.getValidationErrors();

		assertThat(errors).isEmpty();
	}

	@Test
	public void givenDateTypeValidatorFailingWhenValidatingThenOneErrorInList()
			throws Exception {
		defineSchemasManager().using(schemas.withADateTimeMetadata());
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		record.set(zeSchema.dateTimeMetadata(), true);
		List<ValidationError> errors = services.validateManualMetadatasReturningErrors(record, recordProvider, transaction)
				.getValidationErrors();

		assertThat(errors).hasSize(1);
		assertThat(errors.get(0)).has(codeBasedOn(MetadataValueTypeValidator.class, INVALID_VALUE_FOR_METADATA));
		assertThat(errors.get(0).getParameters()).containsEntry(EXPECTED_TYPE_MESSAGE_PARAM, "DATE_TIME")
				.containsEntry(METADATA_CODE_MESSAGE_PARAM, zeSchema.dateTimeMetadata().getCode())
				.containsEntry(WAS_VALUE_CLASS_MESSAGE_PARAM, "java.lang.Boolean");

	}

	@Test
	public void givenNumberTypeValidatorPassingWhenValidatingThenEmptyErrorList()
			throws Exception {
		defineSchemasManager().using(schemas.withANumberMetadata());
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		record.set(zeSchema.numberMetadata(), 1.4f);
		List<ValidationError> errors = services.validateManualMetadatasReturningErrors(record, recordProvider, transaction)
				.getValidationErrors();

		assertThat(errors).isEmpty();
	}

	@Test
	public void givenNumberTypeValidatorFailingWhenValidatingThenOneErrorInList()
			throws Exception {
		defineSchemasManager().using(schemas.withANumberMetadata());
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		try {
			record.set(zeSchema.numberMetadata(), "not a number");
			fail("Exception expected");
		} catch (IllegalArgumentException e) {
			//OK
		}
//		List<ValidationError> errors = services.validateManualMetadatasReturningErrors(record, recordProvider, transaction)
		//				.getValidationErrors();
		//
		//		assertThat(errors).hasSize(1);
		//		assertThat(errors.get(0)).has(codeBasedOn(MetadataValueTypeValidator.class, INVALID_VALUE_FOR_METADATA));
		//		assertThat(errors.get(0).getParameters()).containsEntry(EXPECTED_TYPE_MESSAGE_PARAM, "NUMBER")
		//				.containsEntry(METADATA_CODE_MESSAGE_PARAM, zeSchema.numberMetadata().getCode())
		//				.containsEntry(WAS_VALUE_CLASS_MESSAGE_PARAM, "java.lang.String");

	}

	@Test
	public void givenBooleanTypeValidatorPassingWhenValidatingThenEmptyErrorList()
			throws Exception {
		defineSchemasManager().using(schemas.withABooleanMetadata());
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		record.set(zeSchema.booleanMetadata(), true);
		List<ValidationError> errors = services.validateManualMetadatasReturningErrors(record, recordProvider, transaction)
				.getValidationErrors();

		assertThat(errors).isEmpty();
	}

	@Test
	public void givenBooleanTypeValidatorFailingWhenValidatingThenOneErrorInList()
			throws Exception {
		defineSchemasManager().using(schemas.withABooleanMetadata());
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		record.set(zeSchema.booleanMetadata(), "1.4");
		List<ValidationError> errors = services.validateManualMetadatasReturningErrors(record, recordProvider, transaction)
				.getValidationErrors();

		assertThat(errors).hasSize(1);
		assertThat(errors.get(0)).has(codeBasedOn(MetadataValueTypeValidator.class, INVALID_VALUE_FOR_METADATA));
		assertThat(errors.get(0).getParameters()).containsEntry(EXPECTED_TYPE_MESSAGE_PARAM, "BOOLEAN")
				.containsEntry(METADATA_CODE_MESSAGE_PARAM, zeSchema.booleanMetadata().getCode())
				.containsEntry(WAS_VALUE_CLASS_MESSAGE_PARAM, "java.lang.String");

	}

	@Test
	public void givenTypeValidatorFailingWithMultipleMetadataWhenValidatingThenMultipleErrorsInList()
			throws Exception {
		defineSchemasManager().using(schemas.withABooleanMetadata().withADateTimeMetadata());
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		record.set(zeSchema.booleanMetadata(), "invalid").set(zeSchema.dateTimeMetadata(), "alsoInvalid");
		List<ValidationError> errors = services.validateManualMetadatasReturningErrors(record, recordProvider, transaction)
				.getValidationErrors();

		assertThat(errors).hasSize(2);
	}

	@Test
	public void givenRequirementValidatorPassingWhenValidatingThenNoErrorInList()
			throws Exception {
		defineSchemasManager().using(schemas.withABooleanMetadata(whichHasDefaultRequirement));
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		record.set(zeSchema.booleanMetadata(), true);
		List<ValidationError> errors = services.validateManualMetadatasReturningErrors(record, recordProvider, transaction)
				.getValidationErrors();

		assertThat(errors).isEmpty();
	}

	@Test
	public void givenRequirementValidatorPassingWhenValidatingMultivalueThenNoErrorInList()
			throws Exception {
		defineSchemasManager().using(schemas.withABooleanMetadata(whichIsMultivalue, whichHasDefaultRequirement));
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		record.set(zeSchema.booleanMetadata(), Arrays.asList(true));
		List<ValidationError> errors = services.validateManualMetadatasReturningErrors(record, recordProvider, transaction)
				.getValidationErrors();

		assertThat(errors).isEmpty();
	}

	@Test
	public void givenRequirementValidatorFailingWhenValidatingThenErrorInList()
			throws Exception {
		defineSchemasManager().using(schemas.withABooleanMetadata(whichHasDefaultRequirement));
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		List<ValidationError> errors = services.validateManualMetadatasReturningErrors(record, recordProvider, transaction)
				.getValidationErrors();

		assertThat(errors).hasSize(1);
		assertThat(errors.get(0)).has(
				codeBasedOn(ValueRequirementValidator.class, ValueRequirementValidator.REQUIRED_VALUE_FOR_METADATA));
		assertThat(errors.get(0).getParameters()).containsEntry(ValueRequirementValidator.METADATA_CODE,
				zeSchema.booleanMetadata().getCode());
	}

	@Test
	public void givenRequirementsNotValidatedWhenValidatingIncompleteRecordThenOk()
			throws Exception {
		defineSchemasManager().using(schemas.withABooleanMetadata(whichHasDefaultRequirement));
		when(transaction.isSkippingRequiredValuesValidation()).thenReturn(true);
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		List<ValidationError> errors = services.validateManualMetadatasReturningErrors(record, recordProvider, transaction)
				.getValidationErrors();

		assertThat(errors).isEmpty();
	}

	@Test
	public void givenUserAccessNotValidatedWhenValidatingRecordWithUserWithReadAuthorizationThenOk()
			throws Exception {
		services = spy(services);
		defineSchemasManager().using(schemas.withAStringMetadata());
		when(transaction.isSkippingUserAccessValidation()).thenReturn(true);
		when(transaction.getUser()).thenReturn(users.aliceIn(zeCollection));
		record = recordServices.newRecordWithSchema(zeSchema.instance());
		record.set(zeSchema.stringMetadata(), correctStringValue);
		doReturn(true).when(services).hasSecurityOnSchema(record);

		services.validateAccess(record, transaction);
	}

	@Test(expected = RecordServicesException.class)
	public void givenUserAccessValidatedWhenValidatingRecordWithUserWithReadAuthorizationThenThrowValidationException()
			throws Exception {
		services = spy(services);
		defineSchemasManager().using(schemas.withAStringMetadata());
		when(transaction.isSkippingUserAccessValidation()).thenReturn(false);
		when(transaction.getUser()).thenReturn(users.aliceIn(zeCollection));
		record = recordServices.newRecordWithSchema(zeSchema.instance());
		record.set(zeSchema.stringMetadata(), correctStringValue);
		doReturn(true).when(services).hasSecurityOnSchema(record);

		services.validateAccess(record, transaction);
	}


	@Test
	public void givenRequirementValidatorFailingWhenValidatingUndefinedMultivalueMetadataThenErrorInList()
			throws Exception {
		defineSchemasManager().using(schemas.withABooleanMetadata(whichIsMultivalue, whichHasDefaultRequirement));
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		List<ValidationError> errors = services.validateManualMetadatasReturningErrors(record, recordProvider, transaction)
				.getValidationErrors();

		assertThat(errors).hasSize(1);
		assertThat(errors.get(0)).has(
				codeBasedOn(ValueRequirementValidator.class, ValueRequirementValidator.REQUIRED_VALUE_FOR_METADATA));
		assertThat(errors.get(0).getParameters()).containsEntry(ValueRequirementValidator.METADATA_CODE,
				zeSchema.booleanMetadata().getCode());
	}

	@Test
	public void givenRequirementValidatorFailingWhenValidatingEmptyMultivalueMetadataThenErrorInList()
			throws Exception {
		defineSchemasManager().using(schemas.withABooleanMetadata(whichIsMultivalue, whichHasDefaultRequirement));
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		record.set(zeSchema.booleanMetadata(), Collections.emptyList());
		List<ValidationError> errors = services.validateManualMetadatasReturningErrors(record, recordProvider, transaction)
				.getValidationErrors();

		assertThat(errors).hasSize(1);
		assertThat(errors.get(0)).has(
				codeBasedOn(ValueRequirementValidator.class, ValueRequirementValidator.REQUIRED_VALUE_FOR_METADATA));
		assertThat(errors.get(0).getParameters()).containsEntry(ValueRequirementValidator.METADATA_CODE,
				zeSchema.booleanMetadata().getCode());
	}

	@Test
	public void givenRequirementValidatorFailingWithMultipleMetadataWhenValidatingThenMultipleErrorsInList()
			throws Exception {
		defineSchemasManager().using(
				schemas.withABooleanMetadata(whichHasDefaultRequirement).withADateTimeMetadata(whichHasDefaultRequirement));
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		List<ValidationError> errors = services.validateManualMetadatasReturningErrors(record, recordProvider, transaction)
				.getValidationErrors();

		assertThat(errors).hasSize(2);
	}

	@Test
	public void givenMetadataLengthIsEqualToMaxLengthThenOk() throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(whichMaxLengthIs7));
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		record.set(zeSchema.stringMetadata(), "1234567");

		List<ValidationError> errors = services.validateManualMetadatasReturningErrors(record, recordProvider, transaction)
				.getValidationErrors();

		assertThat(errors).isEmpty();
	}

	@Test
	public void givenMetadataLengthIsLesserThenMaxLengthThenOk() throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(whichMaxLengthIs7));
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		record.set(zeSchema.stringMetadata(), "123456");

		List<ValidationError> errors = services.validateManualMetadatasReturningErrors(record, recordProvider, transaction)
				.getValidationErrors();

		assertThat(errors).isEmpty();
	}

	@Test
	public void givenMetadataLengthIsBiggerThenMaxLengthThenSingleError() throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(whichMaxLengthIs7));
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		record.set(zeSchema.stringMetadata(), "12345678");

		List<ValidationError> errors = services.validateManualMetadatasReturningErrors(record, recordProvider, transaction)
				.getValidationErrors();

		assertThat(errors).hasSize(1);
	}

	@Test
	public void givenAllowedReferencesValidatorFailingWhenValidatingThenErrorInList()
			throws Exception {
		defineSchemasManager().using(schemas.withAReferenceMetadata(whichAllowsAnotherDefaultSchema));
		record = recordServices.newRecordWithSchema(zeSchema.instance());
		Record thirdSchemaRecord = givenRecordInThirdSchema();

		record.set(zeSchema.referenceMetadata(), thirdSchemaRecord.getId());
		List<ValidationError> errors = services.validateManualMetadatasReturningErrors(record, recordProvider, transaction)
				.getValidationErrors();

		assertThat(errors).hasSize(1);
		assertThat(errors.get(0)).has(
				codeBasedOn(AllowedReferencesValidator.class, AllowedReferencesValidator.UNALLOWED_REFERENCE_FOR_METADATA));
		assertThat(errors.get(0).getParameters()).containsEntry(AllowedReferencesValidator.METADATA_CODE,
				zeSchema.referenceMetadata().getCode());
	}

	@Test
	public void givenAllowedReferencesValidatorFailingWhenValidatingMultivalueThenErrorInList()
			throws Exception {
		defineSchemasManager().using(schemas.withAReferenceMetadata(whichIsMultivalue, whichAllowsAnotherDefaultSchema));
		Record anotherSchemaRecord = givenRecordInAnotherSchema();
		Record thirdSchemaRecord = givenRecordInThirdSchema();
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		record.set(zeSchema.referenceMetadata(), Arrays.asList(anotherSchemaRecord.getId(), thirdSchemaRecord.getId()));
		List<ValidationError> errors = services.validateManualMetadatasReturningErrors(record, recordProvider, transaction)
				.getValidationErrors();

		assertThat(errors).hasSize(1);
		assertThat(errors.get(0)).has(
				codeBasedOn(AllowedReferencesValidator.class, AllowedReferencesValidator.UNALLOWED_REFERENCE_FOR_METADATA));
		assertThat(errors.get(0).getParameters()).containsEntry(AllowedReferencesValidator.METADATA_CODE,
				zeSchema.referenceMetadata().getCode());
	}

	@Test
	public void givenAllowedReferencesValidatorPassingWhenValidatingThenNoErrorInList()
			throws Exception {
		defineSchemasManager().using(schemas.withAReferenceMetadata(whichAllowsAnotherDefaultSchema));
		Record anotherSchemaRecord = givenRecordInAnotherSchema();
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		record.set(zeSchema.referenceMetadata(), anotherSchemaRecord.getId());
		List<ValidationError> errors = services.validateManualMetadatasReturningErrors(record, recordProvider, transaction)
				.getValidationErrors();

		assertThat(errors).isEmpty();
	}

	@Test
	public void givenAllowedReferencesValidatorPassingWhenValidatingMultivalueThenNoErrorInList()
			throws Exception {
		defineSchemasManager().using(schemas.withAReferenceMetadata(whichIsMultivalue, whichAllowsAnotherDefaultSchema));
		Record anotherSchemaRecord1 = givenRecordInAnotherSchema();
		Record anotherSchemaRecord2 = givenRecordInAnotherSchema();
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		record.set(zeSchema.referenceMetadata(), Arrays.asList(anotherSchemaRecord1.getId(), anotherSchemaRecord2.getId()));
		List<ValidationError> errors = services.validateManualMetadatasReturningErrors(record, recordProvider, transaction)
				.getValidationErrors();

		assertThat(errors).isEmpty();
	}

	//@Test
	public void givenMetadataWithInputMaskWhenSavingRecordWithIncompatibleValueThenValidationException()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(whichHasInputMask("(###) ###-####"), whichHasLabel("Ze meta!")));
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		record.set(zeSchema.stringMetadata(), "(415)  666-4242");

		try {
			recordServices.add(record);
			fail("Exception expected");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(e.getErrors().getValidationErrors()).hasSize(1);
			ValidationError error = e.getErrors().getValidationErrors().get(0);
			assertThat(error).has(codeBasedOn(MaskedMetadataValidator.class, VALUE_INCOMPATIBLE_WITH_SPECIFIED_MASK));
			assertThat(error.getParameters()).containsOnly(
					entry(MaskedMetadataValidator.METADATA_CODE, zeSchema.stringMetadata().getCode()),
					entry(MaskedMetadataValidator.METADATA_LABEL, "Ze meta!"),
					entry(MaskedMetadataValidator.MASK, "(###) ###-####"),
					entry(MaskedMetadataValidator.VALUE, "(415)  666-4242")
			);
		}

		record.set(zeSchema.stringMetadata(), "4156664242");
		recordServices.add(record);
		String formattedValue = recordServices.getDocumentById(record.getId()).get(zeSchema.stringMetadata());
		assertThat(formattedValue).isEqualTo("(415) 666-4242");

		record.set(zeSchema.stringMetadata(), "(412) 666-4242");
		recordServices.update(record);
		formattedValue = recordServices.getDocumentById(record.getId()).get(zeSchema.stringMetadata());
		assertThat(formattedValue).isEqualTo("(412) 666-4242");

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getMetadata(zeSchema.stringMetadata().getCode()).setInputMask("###-###-####");
			}
		});
		record.set(Schemas.TITLE, "A new Title");
		try {
			recordServices.update(record);
			fail("Exception expected");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(e.getErrors().getValidationErrors()).hasSize(1);
			ValidationError error = e.getErrors().getValidationErrors().get(0);
			assertThat(error).has(codeBasedOn(MaskedMetadataValidator.class, VALUE_INCOMPATIBLE_WITH_SPECIFIED_MASK));
			assertThat(error.getParameters()).containsOnly(
					entry(MaskedMetadataValidator.METADATA_CODE, zeSchema.stringMetadata().getCode()),
					entry(MaskedMetadataValidator.METADATA_LABEL, "Ze meta!"),
					entry(MaskedMetadataValidator.MASK, "###-###-####"),
					entry(MaskedMetadataValidator.VALUE, "(412) 666-4242")
			);
		}
	}

	//@Test
	public void givenMetadataWithInputMaskWhenSavingRecordWithValidUnformattedValueThenFormatted()
			throws Exception {
		defineSchemasManager().using(schemas);//.withATitle(whichHasInputMask("(###) ###-####"), whichHasLabel("Ze meta!")));
		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).get(Schemas.TITLE_CODE).setInputMask("(###) ###-####");
			}
		});

		record = recordServices.newRecordWithSchema(zeSchema.instance());
		record.set(Schemas.TITLE, "4156664242");
		assertThat(record.<String>get(zeSchema.metadata(Schemas.TITLE_CODE))).isEqualTo("4156664242");

		recordServices.add(record);

		String formattedValue = recordServices.getDocumentById(record.getId()).get(Schemas.TITLE);
		assertThat(formattedValue).isEqualTo("(415) 666-4242");

	}

	@Test
	public void givenTheNewParentOfARecordIsOneOfItsDescendantThenException()
			throws Exception {
		Map<Language, String> labelTitle = new HashMap<>();
		labelTitle.put(Language.French, "taxo");

		defineSchemasManager()
				.using(schemas.withAParentReferenceFromZeSchemaToZeSchema().withAReferenceMetadataToZeSchema());
		Taxonomy taxonomy = Taxonomy.createPublic("taxo", labelTitle, zeCollection, Arrays.asList("zeSchemaType"));
		getModelLayerFactory().getTaxonomiesManager()
				.addTaxonomy(taxonomy, getModelLayerFactory().getMetadataSchemasManager());
		getModelLayerFactory().getTaxonomiesManager()
				.setPrincipalTaxonomy(taxonomy, getModelLayerFactory().getMetadataSchemasManager());

		Record grandParent = new TestRecord(zeSchema, "grandParent");
		Record parent = new TestRecord(zeSchema, "parent");
		Record child = new TestRecord(zeSchema, "child");
		parent.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), grandParent);
		child.set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), parent);

		recordServices.execute(new Transaction(grandParent, parent, child));

		try {
			recordServices.update(refreshed(parent).set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), child));
			fail("Exception expected");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(e.getErrors().getValidationErrors().get(0).getCode())
					.endsWith(CyclicHierarchyValidator.CANNOT_REFERENCE_A_DESCENDANT_IN_A_CHILD_OF_REFERENCE);
		}

		try {
			recordServices.update(refreshed(grandParent).set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), child));
			fail("Exception expected");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(e.getErrors().getValidationErrors().get(0).getCode())
					.endsWith(CyclicHierarchyValidator.CANNOT_REFERENCE_A_DESCENDANT_IN_A_CHILD_OF_REFERENCE);
		}

		try {
			recordServices.update(refreshed(parent).set(zeSchema.parentReferenceFromZeSchemaToZeSchema(), refreshed(parent)));
			fail("Exception expected");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(e.getErrors().getValidationErrors().get(0).getCode())
					.endsWith(AllowedReferencesValidator.CANNOT_REFERENCE_ITSELF);
		}

		try {
			recordServices.update(refreshed(parent).set(zeSchema.referenceMetadata(), refreshed(parent)));
			fail("Exception expected");
		} catch (RecordServicesException.ValidationException e) {
			assertThat(e.getErrors().getValidationErrors().get(0).getCode())
					.endsWith(AllowedReferencesValidator.CANNOT_REFERENCE_ITSELF);
		}

		recordServices.update(refreshed(parent).set(zeSchema.referenceMetadata(), child));
	}

	private Record refreshed(Record record) {
		return recordServices.getDocumentById(record.getId());
	}

	private Condition<? super ValidationError> codeBasedOn(final Class<?> validatorClass, final String errorName) {
		return new Condition<ValidationError>() {

			@Override
			public boolean matches(ValidationError value) {
				String expectedCode = validatorClass.getName() + "_" + errorName;
				assertThat(value.getCode()).isEqualTo(expectedCode);
				return true;
			}

		};
	}

	private Record givenRecordInAnotherSchema() {
		Record record = new TestRecord(anotherSchema);
		try {
			recordServices.add(record);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		return record;

	}

	private Record givenRecordInThirdSchema() {
		Record record = new TestRecord(thirdSchema);
		try {
			recordServices.add(record);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		return record;

	}

}
