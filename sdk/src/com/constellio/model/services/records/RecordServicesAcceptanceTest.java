package com.constellio.model.services.records;

import static com.constellio.model.frameworks.validation.Validator.METADATA_CODE;
import static com.constellio.model.services.records.RecordServicesAcceptanceTestUtils.calculatedReferenceFromDummyCalculatorUsingOtherMetadata;
import static com.constellio.model.services.records.RecordServicesAcceptanceTestUtils.calculatedTextFromDummyCalculator;
import static com.constellio.model.services.records.RecordServicesAcceptanceTestUtils.calculatedTextListFromDummyCalculator;
import static com.constellio.model.services.records.RecordServicesAcceptanceTestUtils.calculatedTextListFromDummyCalculatorReturningInvalidType;
import static com.constellio.model.services.schemas.validators.MetadataUnmodifiableValidator.UNMODIFIABLE_METADATA;
import static com.constellio.sdk.tests.TestUtils.asList;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.limitedTo50Characters;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichAllowsAnotherDefaultSchema;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasDefaultRequirement;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEncrypted;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsUnmodifiable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.ArgumentCaptor;

import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.dao.services.sequence.SequencesManager;
import com.constellio.data.utils.Factory;
import com.constellio.model.conf.PropertiesModelLayerConfiguration.InMemoryModelLayerConfiguration;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.Validator;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.encrypt.EncryptionKeyFactory;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_TransactionHasMoreThan100000Records;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_TransactionWithMoreThan1000RecordsCannotHaveTryMergeOptimisticLockingResolution;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataBuilder_EnumClassTest.AValidEnum;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.validators.MetadataUnmodifiableValidator;
import com.constellio.sdk.FakeEncryptionServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.ModelLayerConfigurationAlteration;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.annotations.SlowTest;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RecordServicesAcceptanceTest extends ConstellioTest {
	LocalDate shishDay = new LocalDate().minusDays(42);
	LocalDate tockDay = new LocalDate().minusDays(666);

	private final String valueTooLong = "this title is too lonnnnnnnnnnnnnnnnnnnnnnnnnnnnnng";
	String idReferencedRecordWithAStringAndADateValue, idReferencedRecordWithAnotherDateValue, idReferencedRecordWithoutValue;
	RecordServicesTestSchemaSetup schemas;
	RecordServicesTestSchemaSetup.ZeSchemaMetadatas zeSchema;
	RecordServicesTestSchemaSetup.AnotherSchemaMetadatas anotherSchema;
	RecordServicesTestSchemaSetup.ThirdSchemaMetadatas thirdSchema;
	LocalDateTime january1 = new LocalDateTime(2014, 1, 1, 0, 0, 0);
	LocalDateTime january2 = new LocalDateTime(2014, 1, 2, 0, 0, 0);
	Record record;
	List<Metadata> allFields;
	BatchProcessesManager batchProcessesManager;
	LocalDateTime now = new LocalDateTime();
	LocalDateTime shishOClock = new LocalDateTime();
	LocalDateTime tockOClock = new LocalDateTime();
	private RecordServicesImpl recordServices;
	RecordDao recordDao;

	@Before
	public void setup()
			throws Exception {
		System.out.println("\n\n--RecordServicesAcceptanceTest.setup--\n\n");

		configure(new ModelLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryModelLayerConfiguration configuration) {

				Factory<EncryptionServices> encryptionServicesFactory = new Factory<EncryptionServices>() {

					@Override
					public EncryptionServices get() {
						Key key = EncryptionKeyFactory.newApplicationKey("zePassword", "zeUltimateSalt");
						try {
							return new EncryptionServices(false).withKey(key);
						} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
							throw new RuntimeException(e);
						}
					}
				};

				configuration.setEncryptionServicesFactory(encryptionServicesFactory);
			}
		});

		recordServices = spy((RecordServicesImpl) getModelLayerFactory().newCachelessRecordServices());
		batchProcessesManager = getModelLayerFactory().getBatchProcessesManager();
		schemas = new RecordServicesTestSchemaSetup();
		zeSchema = schemas.new ZeSchemaMetadatas();
		anotherSchema = schemas.new AnotherSchemaMetadatas();
		thirdSchema = schemas.new ThirdSchemaMetadatas();
		record = new TestRecord(zeSchema, "zeUltimateRecord");
		recordDao = getDataLayerFactory().newRecordDao();
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void givenRequiredAutomaticMetadataWhenSavingWithNullValueThenValidationException()
			throws Exception {
		System.out.println("\n\n--ze test--\n\n");
		defineSchemasManager().using(
				schemas.withAStringMetadata(whichHasDefaultRequirement, calculatedTextFromDummyCalculator()));

		record.set(zeSchema.metadata("other"), null);
		recordServices.add(record);
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void givenAutomaticMetadataWhenSavingWithValueWhichIsInvalidValueThenValidationException()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(limitedTo50Characters, calculatedTextFromDummyCalculator()));

		record.set(zeSchema.metadata("other"), valueTooLong);
		recordServices.add(record);
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void givenAutomaticReferenceMetadataWhenSavingWithValueOfUnallowedSchemaThenValidationException()
			throws Exception {
		defineSchemasManager().using(
				schemas.withAReferenceMetadata(whichAllowsAnotherDefaultSchema,
						calculatedReferenceFromDummyCalculatorUsingOtherMetadata()));
		Record recordUnallowed = saveThirdSchemaRecord();

		record.set(zeSchema.metadata("other"), recordUnallowed.getId());
		recordServices.add(record);
	}

	@Test
	public void givenAutomaticReferenceMetadataWhenSavingWithValueOfAllowedSchemaThenOK()
			throws Exception {
		defineSchemasManager().using(
				schemas.withAReferenceMetadata(whichAllowsAnotherDefaultSchema,
						calculatedReferenceFromDummyCalculatorUsingOtherMetadata()));
		Record recordUnallowed = saveAnotherSchemaRecord();

		record.set(zeSchema.metadata("other"), recordUnallowed.getId());
		recordServices.add(record);
	}

	@Test
	public void givenRequiredAutomaticMetadataWhenSavingWithValueThenOK()
			throws Exception {
		defineSchemasManager().using(
				schemas.withAStringMetadata(whichHasDefaultRequirement, calculatedTextFromDummyCalculator()));

		record.set(zeSchema.metadata("other"), "aValue");
		recordServices.add(record);
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void givenRequiredAutomaticMultivalueMetadataWhenSavingWithNullListThenValidationException()
			throws Exception {
		defineSchemasManager().using(
				schemas.withAStringMetadata(whichHasDefaultRequirement, calculatedTextListFromDummyCalculator()));

		record.set(zeSchema.metadata("other"), null);
		recordServices.add(record);
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void givenRequiredAutomaticMultivalueMetadataWhenSavingWithEmptyListThenValidationException()
			throws Exception {
		defineSchemasManager().using(
				schemas.withAStringMetadata(whichHasDefaultRequirement, calculatedTextListFromDummyCalculator()));

		record.set(zeSchema.metadata("other"), new ArrayList<>());
		recordServices.add(record);
	}

	@Test
	public void givenRequiredAutomaticMultivalueMetadataWhenSavingWithValueThenOK()
			throws Exception {
		defineSchemasManager().using(
				schemas.withAStringMetadata(whichHasDefaultRequirement, calculatedTextListFromDummyCalculator()));

		record.set(zeSchema.metadata("other"), asList("aValue"));
		recordServices.add(record);
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void givenRequiredAutomaticMetadataWhenSavingWithInvalidTypeThenValidationException()
			throws Exception {
		defineSchemasManager().using(
				schemas.withAStringMetadata(whichHasDefaultRequirement,
						calculatedTextListFromDummyCalculatorReturningInvalidType()));

		record.set(zeSchema.metadata("other"), 1);
		recordServices.add(record);
	}

	@Test
	public void whenAddingSomeRecordsThenDocumentsCountIsCorrect()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata().withAnotherStringMetadata());
		long initialDocumentsCount = recordServices.documentsCount();
		for (int i = 0; i < 11; i++) {
			record = new TestRecord(zeSchema);
			recordServices.add(record.set(zeSchema.stringMetadata(), "value " + i));
		}

		assertThat(recordServices.documentsCount() - initialDocumentsCount).isEqualTo(11);
	}

	@Test
	public void whenGettingRecordsTitlesThenTitlesReturned()
			throws Exception {
		defineSchemasManager().using(schemas);
		Record record1 = new TestRecord(zeSchema);
		recordServices.add(record1.set(Schemas.TITLE, "zeTitle1"));
		Record record2 = new TestRecord(zeSchema);
		recordServices.add(record2.set(Schemas.TITLE, "zeTitle2"));

		assertThat(recordServices.getRecordTitles(zeCollection, Arrays.asList(record1.getId(), record2.getId()))).contains(
				"zeTitle1", "zeTitle2");
	}

	@Test
	public void givenSchemaWithEnumListWhenAddingRecordThenValuesPersisted()
			throws Exception {
		defineSchemasManager().using(schemas.withAnEnumMetadata(AValidEnum.class, whichIsMultivalue));
		record.set(zeSchema.enumMetadata(), asList(AValidEnum.SECOND_VALUE, AValidEnum.FIRST_VALUE));

		recordServices.add(record);
		record = recordServices.getDocumentById(record.getId());

		assertThat(record.get(zeSchema.enumMetadata())).isEqualTo(asList(AValidEnum.SECOND_VALUE, AValidEnum.FIRST_VALUE));
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void givenSchemaWithValidatorsWhenAddingRecordFailingValidationThenThrowValidationException()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(limitedTo50Characters));
		record.set(zeSchema.stringMetadata(), valueTooLong);

		recordServices.add(record);
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void givenSchemaWithValidatorsWhenUpdatingRecordFailingValidationThenThrowValidationException()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(limitedTo50Characters));
		recordServices.add(record.set(zeSchema.stringMetadata(), "Banana"));
		record.set(zeSchema.stringMetadata(), valueTooLong);

		recordServices.update(record);
	}

	@Test()
	public void givenSchemaWithFixedSequenceMetadataWhenAddingValidRecordThenSetNewSequenceValue()
			throws Exception {
		defineSchemasManager().using(schemas.withAFixedSequence());
		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).get("title").setDefaultRequirement(true);
				types.getSchema(zeSchema.code()).create("calculatedOnFixedSequence").setType(MetadataValueType.STRING)
						.defineDataEntry().asJexlScript("'F'+ fixedSequenceMetadata + '.00'");

			}
		});

		record = recordServices.newRecordWithSchema(zeSchema.instance());

		try {
			recordServices.add(record);
		} catch (RecordServicesException.ValidationException e) {
			//OK
		}

		assertThat(record.get(zeSchema.fixedSequenceMetadata())).isNull();
		assertThat(record.get(zeSchema.metadata("calculatedOnFixedSequence"))).isNull();

		record.set(Schemas.TITLE, "Ze title");

		try {
			recordServices.add(record);
		} catch (RecordServicesException.ValidationException e) {
			//OK
		}

		assertThat(record.get(zeSchema.fixedSequenceMetadata())).isEqualTo("1");
		assertThat(record.get(zeSchema.metadata("calculatedOnFixedSequence"))).isEqualTo("F1.00");

		record = recordServices.newRecordWithSchema(zeSchema.instance());
		record.set(Schemas.TITLE, "Ze title");
		try {
			recordServices.add(record);
		} catch (RecordServicesException.ValidationException e) {
			//OK
		}

		assertThat(record.get(zeSchema.fixedSequenceMetadata())).isEqualTo("2");
		assertThat(record.get(zeSchema.metadata("calculatedOnFixedSequence"))).isEqualTo("F2.00");

	}

	@Test()
	public void givenSchemaWithDynamicSequenceMetadataWhenChangeSequenceSourceThenGetNewSequenceUsingNewSource()
			throws Exception {

		SequencesManager sequencesManager = getDataLayerFactory().getSequencesManager();
		sequencesManager.set("sequence1", 42);
		sequencesManager.set("sequence2", 666);

		defineSchemasManager().using(schemas.withADynamicSequence());
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		recordServices.add(record);

		assertThat(record.get(zeSchema.dynamicSequenceMetadata())).isNull();

		recordServices.update(record.set(zeSchema.metadataDefiningSequenceNumber(), "sequence1"));
		assertThat(record.get(zeSchema.dynamicSequenceMetadata())).isEqualTo("43");

		recordServices.update(record.set(zeSchema.metadataDefiningSequenceNumber(), "sequence2"));
		assertThat(record.get(zeSchema.dynamicSequenceMetadata())).isEqualTo("667");

		recordServices.update(record.set(zeSchema.metadataDefiningSequenceNumber(), "sequence1"));
		assertThat(record.get(zeSchema.dynamicSequenceMetadata())).isEqualTo("44");

		recordServices.update(record.set(Schemas.TITLE, "zeTitle"));
		assertThat(record.get(zeSchema.dynamicSequenceMetadata())).isEqualTo("44");

	}

	@Test()
	public void givenSchemaWithCopiedMetadataWhenAddingRecordThenCopyValues()
			throws Exception {
		defineSchemasManager().using(
				schemas.withTwoMetadatasCopyingAnotherSchemaValuesUsingTwoDifferentReferenceMetadata(false, false, false));
		String referencedRecordId = addRecordInAnotherSchemaWithStringMetadataValue(false);
		record = recordServices.newRecordWithSchema(zeSchema.instance());
		record.set(zeSchema.firstReferenceToAnotherSchema(), referencedRecordId);

		recordServices.add(record);

		assertThat(record.get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo("Banana");
	}

	@Test()
	public void givenSchemaWithCopiedMultivalueMetadataWhenAddingRecordThenCopyValues()
			throws Exception {
		defineSchemasManager().using(
				schemas.withTwoMetadatasCopyingAnotherSchemaValuesUsingTwoDifferentReferenceMetadata(true, false, false));
		String referencedRecordId = addRecordInAnotherSchemaWithStringMetadataValue(true);
		record = recordServices.newRecordWithSchema(zeSchema.instance());
		record.set(zeSchema.firstReferenceToAnotherSchema(), referencedRecordId);

		recordServices.add(record);

		assertThat(record.get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo(Arrays.asList("Banana", "Apple"));
	}

	@Test()
	public void givenSchemaWithCopiedMetadataWhenUpdatingRecordThenCopyValues()
			throws Exception {
		defineSchemasManager().using(
				schemas.withTwoMetadatasCopyingAnotherSchemaValuesUsingTwoDifferentReferenceMetadata(false, false, false));
		String referencedRecordId = addRecordInAnotherSchemaWithStringMetadataValue(false);
		record = recordServices.newRecordWithSchema(zeSchema.instance());
		record.set(zeSchema.firstReferenceToAnotherSchema(), referencedRecordId);
		recordServices.add(record);

		record.set(zeSchema.firstReferenceToAnotherSchema(), null);
		recordServices.update(record);

		assertThat(record.get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isNull();
	}

	@Test()
	public void givenSchemaWithCopiedMultivalueMetadataWhenUpdatingRecordThenCopyValues()
			throws Exception {
		defineSchemasManager().using(
				schemas.withTwoMetadatasCopyingAnotherSchemaValuesUsingTwoDifferentReferenceMetadata(true, false, false));
		String referencedRecordId = addRecordInAnotherSchemaWithStringMetadataValue(true);
		record = recordServices.newRecordWithSchema(zeSchema.instance());
		record.set(zeSchema.firstReferenceToAnotherSchema(), referencedRecordId);
		recordServices.add(record);

		record.set(zeSchema.firstReferenceToAnotherSchema(), null);
		recordServices.update(record);

		assertThat(record.get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo(new ArrayList<>());
	}

	@Test()
	public void givenSchemaWithCalculatedMetadataWhenAddingRecordThenCalculateValues()
			throws Exception {
		defineSchemasManager().using(schemas.withCalculatedDaysBetweenLocalDateAndAnotherSchemaRequiredDate(false));
		Record anotherRecord = saveAnotherSchemaRecordWithDateMetadataToJanuary1();

		Record record = recordServices.newRecordWithSchema(zeSchema.instance());
		record.set(zeSchema.dateTimeMetadata(), january2);
		record.set(zeSchema.secondReferenceToAnotherSchema(), anotherRecord.getId());
		recordServices.add(record);

		assertThat(record.get(zeSchema.calculatedDaysBetween())).isEqualTo(1.0);
	}

	@Test()
	public void givenSchemaWithCalculatedMetadataWhenUpdatingRecordWithModifiedDependenciesThenCalculateValues()
			throws Exception {
		defineSchemasManager().using(schemas.withCalculatedDaysBetweenLocalDateAndAnotherSchemaRequiredDate(false));
		Record anotherRecord = saveAnotherSchemaRecordWithDateMetadataToJanuary1();

		Record record = recordServices.newRecordWithSchema(zeSchema.instance());
		record.set(zeSchema.dateTimeMetadata(), january2);
		record.set(zeSchema.secondReferenceToAnotherSchema(), anotherRecord.getId());
		recordServices.add(record);
		assertThat(record.get(zeSchema.calculatedDaysBetween())).isEqualTo(1.0);

		record.set(zeSchema.dateTimeMetadata(), january1);
		recordServices.update(record);

		assertThat(record.get(zeSchema.calculatedDaysBetween())).isEqualTo(0.0);
	}

	@Test()
	public void givenSchemaWithCalculatedMetadataWhenUpdatingRecordWithRemovedCalculatorDependencyThenSetCalculatedValueToNull()
			throws Exception {
		defineSchemasManager().using(schemas.withCalculatedDaysBetweenLocalDateAndAnotherSchemaRequiredDate(false));
		Record anotherRecord = saveAnotherSchemaRecordWithDateMetadataToJanuary1();

		Record record = recordServices.newRecordWithSchema(zeSchema.instance());
		record.set(zeSchema.dateTimeMetadata(), january2);
		record.set(zeSchema.secondReferenceToAnotherSchema(), anotherRecord.getId());
		recordServices.add(record);
		assertThat(record.get(zeSchema.calculatedDaysBetween())).isEqualTo(1.0);

		record.set(zeSchema.dateTimeMetadata(), null);
		recordServices.update(record);

		assertThat(record.get(zeSchema.calculatedDaysBetween())).isEqualTo(-1.0);
	}

	private Record reloadRecord() {
		return recordServices.getDocumentById(record.getId());
	}

	@Test
	public void whenAddingOrUpdatingLargeTextMetadataThenValueIsSet()
			throws Exception {
		defineSchemasManager().using(schemas.withATitle().withALargeTextMetadata());

		recordServices.add(record.set(zeSchema.title(), "title").set(zeSchema.largeTextMetadata(), "firstValue"));
		Record record = reloadRecord();
		assertThat(reloadRecord().get(zeSchema.largeTextMetadata())).isEqualTo("firstValue");

		recordServices.update(reloadRecord().set(zeSchema.largeTextMetadata(), "secondValue"));
		assertThat(reloadRecord().get(zeSchema.largeTextMetadata())).isEqualTo("secondValue");

		recordServices.update(reloadRecord().set(zeSchema.largeTextMetadata(), null));
		assertThat(reloadRecord().get(zeSchema.largeTextMetadata())).isEqualTo(null);
	}

	@Test
	public void whenAddingOrUpdatingMultivalueLargeTextMetadataThenValueIsSet()
			throws Exception {
		defineSchemasManager().using(schemas.withATitle().withALargeTextMetadata(whichIsMultivalue));

		recordServices.add(record.set(zeSchema.title(), "title")
				.set(zeSchema.largeTextMetadata(), asList("firstValue", "secondValue")));
		assertThat(reloadRecord().get(zeSchema.largeTextMetadata())).isEqualTo(asList("firstValue", "secondValue"));

		recordServices.update(reloadRecord().set(zeSchema.largeTextMetadata(), asList("secondValue", "thirdValue")));
		assertThat(reloadRecord().get(zeSchema.largeTextMetadata())).isEqualTo(asList("secondValue", "thirdValue"));

		recordServices.update(reloadRecord().set(zeSchema.largeTextMetadata(), null));
		assertThat(reloadRecord().getList(zeSchema.largeTextMetadata())).isEqualTo(new ArrayList<>());

		recordServices.update(reloadRecord().set(zeSchema.largeTextMetadata(), new ArrayList<>()));
		assertThat(reloadRecord().getList(zeSchema.largeTextMetadata())).isEqualTo(new ArrayList<>());

		recordServices.update(reloadRecord().set(zeSchema.largeTextMetadata(), asList("zeValue")));
		assertThat(reloadRecord().get(zeSchema.largeTextMetadata())).isEqualTo(asList("zeValue"));
	}

	@Test
	public void givenAddedRecordWhenModifyingSingleValueAndUpdatingThenModificationsSavedAndVersionChanged()
			throws Exception {
		defineSchemasManager().using(schemas.withATitle().withABooleanMetadata(whichIsMultivalue));
		record.set(zeSchema.title(), "aValue");
		record.set(zeSchema.booleanMetadata(), Arrays.asList(true, false, true));
		recordServices.add(record);
		long initialVersion = record.getVersion();

		record.set(zeSchema.title(), "anOtherValue");
		recordServices.update(record);

		assertThat(record.get(zeSchema.title())).isEqualTo("anOtherValue");
		assertThat(record.get(zeSchema.booleanMetadata())).isEqualTo(Arrays.asList(true, false, true));
		assertThat(record.getVersion()).isNotEqualTo(initialVersion);
	}

	@Test
	public void givenRecordWithDateFieldWhenAddUpdateThenOk()
			throws Exception {

		defineSchemasManager().using(schemas.withADateMetadata());

		record.set(zeSchema.dateMetadata(), shishDay);
		recordServices.add(record);
		assertThat(recordServices.getDocumentById(record.getId()).get(zeSchema.dateMetadata())).isEqualTo(shishDay);

		record.set(zeSchema.dateMetadata(), tockDay);
		recordServices.update(record);
		assertThat(recordServices.getDocumentById(record.getId()).get(zeSchema.dateMetadata())).isEqualTo(tockDay);
	}

	@Test
	public void givenAddedRecordWhenModifyingMultivalueAndUpdatingThenModificationsSavedAndVersionChanged()
			throws Exception {
		defineSchemasManager().using(schemas.withATitle().withABooleanMetadata(whichIsMultivalue));
		record.set(zeSchema.title(), "aValue");
		record.set(zeSchema.booleanMetadata(), Arrays.asList(true, false, true));
		recordServices.add(record);
		long initialVersion = record.getVersion();

		record.set(zeSchema.booleanMetadata(), Arrays.asList(false, true, false));
		recordServices.update(record);

		assertThat(record.get(zeSchema.title())).isEqualTo("aValue");
		assertThat(record.get(zeSchema.booleanMetadata())).isEqualTo(Arrays.asList(false, true, false));
		assertThat(record.getVersion()).isNotEqualTo(initialVersion);
	}

	@Test
	public void givenAddedRecordWhenUpdatingNonModifiedRecordThenVersionUnchanged()
			throws Exception {
		defineSchemasManager().using(schemas.withATitle().withABooleanMetadata(whichIsMultivalue));
		record.set(zeSchema.title(), "aValue");
		record.set(zeSchema.booleanMetadata(), Arrays.asList(true, false, true));
		recordServices.add(record);
		long initialVersion = record.getVersion();

		recordServices.update(record);

		assertThat(record.get(zeSchema.title())).isEqualTo("aValue");
		assertThat(record.get(zeSchema.booleanMetadata())).isEqualTo(Arrays.asList(true, false, true));
		assertThat(record.getVersion()).isEqualTo(initialVersion);
	}

	@Test
	public void givenEncryptedMetadataThenDecryptedInRecordAndEncryptedInSolr()
			throws Exception {

		defineSchemasManager()
				.using(schemas.withATitle().withAStringMetadata(whichIsEncrypted)
						.withAnotherStringMetadata(whichIsEncrypted, whichIsMultivalue));

		assertThat(getModelLayerFactory().newEncryptionServices()).isNotNull();
		assertThat(getModelLayerFactory().newEncryptionServices()).isNotInstanceOf(FakeEncryptionServices.class);
		recordServices.add(record
				.set(zeSchema.title(), "neverEncryptedValue")
				.set(zeSchema.stringMetadata(), "decryptedValue1")
				.set(zeSchema.anotherStringMetadata(), asList("decryptedValue2", "decryptedValue3")));

		assertThat(record.get(zeSchema.title())).isEqualTo("neverEncryptedValue");
		assertThat(record.get(zeSchema.stringMetadata())).isEqualTo("decryptedValue1");
		assertThat(record.get(zeSchema.anotherStringMetadata())).isEqualTo(asList("decryptedValue2", "decryptedValue3"));

		record = recordServices.getDocumentById(record.getId());
		assertThat(record.get(zeSchema.title())).isEqualTo("neverEncryptedValue");
		assertThat(record.get(zeSchema.stringMetadata())).isEqualTo("decryptedValue1");
		assertThat(record.get(zeSchema.anotherStringMetadata())).isEqualTo(asList("decryptedValue2", "decryptedValue3"));

		RecordDTO recordDTO = recordDao.get(record.getId());
		assertThat(recordDTO.getFields().get(zeSchema.title().getDataStoreCode())).isEqualTo("neverEncryptedValue");
		assertThat(recordDTO.getFields().get(zeSchema.stringMetadata().getDataStoreCode())).isEqualTo("AN1Qletvk4b6cysfpDjWUg==");
		assertThat(recordDTO.getFields().get(zeSchema.anotherStringMetadata().getDataStoreCode()))
				.isEqualTo(asList("2xz/K3dNfajma8DJQVMBnQ==", "0d6Amw6w/rOUYwTrjNK4LQ=="));

		record.set(zeSchema.stringMetadata(), "decryptedValue2")
				.set(zeSchema.anotherStringMetadata(), asList("decryptedValue3", "decryptedValue4"));
		assertThat(record.get(zeSchema.title())).isEqualTo("neverEncryptedValue");
		assertThat(record.get(zeSchema.stringMetadata())).isEqualTo("decryptedValue2");
		assertThat(record.get(zeSchema.anotherStringMetadata())).isEqualTo(asList("decryptedValue3", "decryptedValue4"));

		recordServices.update(record);

		assertThat(record.get(zeSchema.title())).isEqualTo("neverEncryptedValue");
		assertThat(record.get(zeSchema.stringMetadata())).isEqualTo("decryptedValue2");
		assertThat(record.get(zeSchema.anotherStringMetadata())).isEqualTo(asList("decryptedValue3", "decryptedValue4"));
		record = recordServices.getDocumentById(record.getId());
		assertThat(record.get(zeSchema.title())).isEqualTo("neverEncryptedValue");
		assertThat(record.get(zeSchema.stringMetadata())).isEqualTo("decryptedValue2");
		assertThat(record.get(zeSchema.anotherStringMetadata())).isEqualTo(asList("decryptedValue3", "decryptedValue4"));

		recordDTO = recordDao.get(record.getId());
		assertThat(recordDTO.getFields().get(zeSchema.title().getDataStoreCode())).isEqualTo("neverEncryptedValue");
		assertThat(recordDTO.getFields().get(zeSchema.stringMetadata().getDataStoreCode())).isEqualTo("2xz/K3dNfajma8DJQVMBnQ==");
		assertThat(recordDTO.getFields().get(zeSchema.anotherStringMetadata().getDataStoreCode()))
				.isEqualTo(asList("0d6Amw6w/rOUYwTrjNK4LQ==", "bLMsWh344pykcDFxbBvrvg=="));

	}

	@Test
	public void givenModificationImpactWhenUpdatingRecordThenHandledInSameTransaction()
			throws Exception {
		defineSchemasManager().using(schemas.withAMetadataCopiedInAnotherSchema());

		Record zeSchemaRecord = zeSchemaRecordWithCopiedMeta("a");
		recordServices.add(zeSchemaRecord);

		Record anotherSchemaRecord = anotherSchemaRecordLinkedTo(zeSchemaRecord);
		recordServices.add(anotherSchemaRecord);

		Record thirdSchemaRecord = thirdSchemaRecordLinkedTo(anotherSchemaRecord);
		recordServices.add(thirdSchemaRecord);

		reset(recordServices);
		zeSchemaRecord.set(zeSchema.getCopiedMeta(), "b");
		recordServices.update(zeSchemaRecord);

		verify(recordServices, times(1)).saveContentsAndRecords(any(Transaction.class),
				any(RecordModificationImpactHandler.class), anyInt());

		assertThat(anotherSchemaRecord.get(anotherSchema.metadataWithCopiedEntry())).isEqualTo("a");
		assertThat(thirdSchemaRecord.get(thirdSchema.metadataWithCopiedEntry())).isEqualTo("a");

		recordServices.refresh(asList(anotherSchemaRecord, thirdSchemaRecord));

		assertThat(anotherSchemaRecord.get(anotherSchema.metadataWithCopiedEntry())).isEqualTo("b");
		assertThat(thirdSchemaRecord.get(thirdSchema.metadataWithCopiedEntry())).isEqualTo("b");
	}

	@Test
	public void givenModificationImpactWhenExecutingTransactionThenHandledInSameTransaction()
			throws Exception {
		defineSchemasManager().using(schemas.withAMetadataCopiedInAnotherSchema());

		ArgumentCaptor<Transaction> savedTransaction = ArgumentCaptor.forClass(Transaction.class);

		Record zeSchemaRecord = zeSchemaRecordWithCopiedMeta("a");
		recordServices.add(zeSchemaRecord);

		Record anotherSchemaRecord = anotherSchemaRecordLinkedTo(zeSchemaRecord);
		recordServices.add(anotherSchemaRecord);

		Record thirdSchemaRecord = thirdSchemaRecordLinkedTo(anotherSchemaRecord);
		recordServices.add(thirdSchemaRecord);

		reset(recordServices);
		zeSchemaRecord.set(zeSchema.getCopiedMeta(), "b");
		Transaction transaction = new Transaction();
		transaction.update(zeSchemaRecord);
		recordServices.execute(transaction);

		zeSchemaRecord.set(zeSchema.getCopiedMeta(), "b");

		verify(recordServices, times(3)).execute(savedTransaction.capture());
		Transaction firstNestedTransaction = savedTransaction.getAllValues().get(1);
		Transaction secondNestedTransaction = savedTransaction.getAllValues().get(2);

		assertThat(firstNestedTransaction).isNotSameAs(transaction);
		assertThat(firstNestedTransaction.getRecords()).hasSize(2);
		assertThat(firstNestedTransaction.getRecords().get(0)).isEqualTo(zeSchemaRecord);
		assertThat(firstNestedTransaction.getRecords().get(1).getId()).isEqualTo(anotherSchemaRecord.getId());
		assertThat(firstNestedTransaction.getRecords().get(1).get(anotherSchema.metadataWithCopiedEntry())).isEqualTo("b");

		assertThat(secondNestedTransaction).isNotSameAs(transaction);
		assertThat(secondNestedTransaction.getRecords()).hasSize(3);
		assertThat(secondNestedTransaction.getRecords().get(0)).isEqualTo(zeSchemaRecord);
		assertThat(secondNestedTransaction.getRecords().get(1).getId()).isEqualTo(anotherSchemaRecord.getId());
		assertThat(secondNestedTransaction.getRecords().get(1).get(anotherSchema.metadataWithCopiedEntry())).isEqualTo("b");
		assertThat(secondNestedTransaction.getRecords().get(2).getId()).isEqualTo(thirdSchemaRecord.getId());
		assertThat(secondNestedTransaction.getRecords().get(2).get(anotherSchema.metadataWithCopiedEntry())).isEqualTo("b");

		recordServices.refresh(asList(anotherSchemaRecord, thirdSchemaRecord));
		assertThat(anotherSchemaRecord.get(anotherSchema.metadataWithCopiedEntry())).isEqualTo("b");
		assertThat(thirdSchemaRecord.get(thirdSchema.metadataWithCopiedEntry())).isEqualTo("b");
	}

	@Test
	public void givenUpdatingMultipleRecordsInTransactionThenHandleThemInCorrectOrderReducingChancesOfModificationImpact()
			throws Exception {
		defineSchemasManager().using(schemas.withAMetadataCopiedInAnotherSchema());

		ArgumentCaptor<Transaction> savedTransaction = ArgumentCaptor.forClass(Transaction.class);

		Record zeSchemaRecord = zeSchemaRecordWithCopiedMeta("a");
		recordServices.add(zeSchemaRecord);

		Record anotherSchemaRecord = anotherSchemaRecordLinkedTo(zeSchemaRecord);
		recordServices.add(anotherSchemaRecord);

		Record thirdSchemaRecord = thirdSchemaRecordLinkedTo(anotherSchemaRecord);
		recordServices.add(thirdSchemaRecord);

		reset(recordServices);
		zeSchemaRecord.set(zeSchema.getCopiedMeta(), "b");
		anotherSchemaRecord.set(anotherSchema.manualMeta(), "z");
		thirdSchemaRecord.set(thirdSchema.manualMeta(), "z");

		Record anotherThirdSchemaRecord = thirdSchemaRecordLinkedTo(anotherSchemaRecord);

		Transaction transaction = new Transaction();
		transaction.addUpdate(anotherThirdSchemaRecord);
		transaction.update(thirdSchemaRecord);
		transaction.update(anotherSchemaRecord);
		transaction.update(zeSchemaRecord);
		recordServices.execute(transaction);

		assertThat(anotherSchemaRecord.get(anotherSchema.metadataWithCopiedEntry())).isEqualTo("b");
		assertThat(thirdSchemaRecord.get(thirdSchema.metadataWithCopiedEntry())).isEqualTo("b");
		assertThat(anotherThirdSchemaRecord.get(thirdSchema.metadataWithCopiedEntry())).isEqualTo("b");

		verify(recordServices, times(1)).saveContentsAndRecords(savedTransaction.capture(),
				(RecordModificationImpactHandler) isNull(), anyInt());
	}

	@Test
	public void given2RecordsIn2DifferentSchemaWithTheSecondCalculatedFromTheFirstWhenExecutingTransactionThenAddedInCorrectOrder()
			throws Exception {
		defineSchemasManager().using(schemas.withAMetadataCopiedInAnotherSchema());

		Record zeSchemaRecord1 = recordServices.newRecordWithSchema(zeSchema.instance());
		zeSchemaRecord1.set(zeSchema.getCopiedMeta(), "1.1");
		recordServices.add(zeSchemaRecord1);

		Record zeSchemaRecord2 = recordServices.newRecordWithSchema(zeSchema.instance());
		zeSchemaRecord2.set(zeSchema.getCopiedMeta(), "2.1");
		recordServices.add(zeSchemaRecord2);

		Record anotherSchemaRecord = new TestRecord(anotherSchema);
		anotherSchemaRecord.set(anotherSchema.referenceToZeSchema(), zeSchemaRecord1.getId());
		recordServices.add(anotherSchemaRecord);

		assertThat(anotherSchemaRecord.get(anotherSchema.metadataWithCopiedEntry())).isEqualTo("1.1");

		Transaction initialTransaction = new Transaction();
		initialTransaction.addUpdate(anotherSchemaRecord.set(anotherSchema.referenceToZeSchema(), zeSchemaRecord2.getId()));
		initialTransaction.addUpdate(zeSchemaRecord1.set(zeSchema.getCopiedMeta(), "1.2"));
		initialTransaction.addUpdate(zeSchemaRecord2.set(zeSchema.getCopiedMeta(), "2.2"));
		recordServices.execute(initialTransaction);

		assertThat(anotherSchemaRecord.get(anotherSchema.metadataWithCopiedEntry())).isEqualTo("2.2");
	}

	@Test
	public void given2RecordsIn2DifferentSchemaWithTheSecondCalculatedFromTheFirstAddedInCorrectOrderWhenExecutingTransactionThenKeptInCorrectOrder()
			throws Exception {
		defineSchemasManager().using(schemas.withAMetadataCopiedInAnotherSchema());

		Record zeSchemaRecord1 = recordServices.newRecordWithSchema(zeSchema.instance());
		zeSchemaRecord1.set(zeSchema.getCopiedMeta(), "1.1");
		recordServices.add(zeSchemaRecord1);

		Record zeSchemaRecord2 = recordServices.newRecordWithSchema(zeSchema.instance());
		zeSchemaRecord2.set(zeSchema.getCopiedMeta(), "2.1");
		recordServices.add(zeSchemaRecord2);

		Record anotherSchemaRecord = recordServices.newRecordWithSchema(anotherSchema.instance());
		anotherSchemaRecord.set(anotherSchema.referenceToZeSchema(), zeSchemaRecord1.getId());
		recordServices.add(anotherSchemaRecord);

		assertThat(anotherSchemaRecord.get(anotherSchema.metadataWithCopiedEntry())).isEqualTo("1.1");

		Transaction initialTransaction = new Transaction();
		initialTransaction.addUpdate(zeSchemaRecord1.set(zeSchema.getCopiedMeta(), "1.2"));
		initialTransaction.addUpdate(zeSchemaRecord2.set(zeSchema.getCopiedMeta(), "2.2"));
		initialTransaction.addUpdate(anotherSchemaRecord.set(anotherSchema.referenceToZeSchema(), zeSchemaRecord2.getId()));
		recordServices.execute(initialTransaction);

		assertThat(anotherSchemaRecord.get(anotherSchema.metadataWithCopiedEntry())).isEqualTo("2.2");
	}

	@Test
	public void given2RecordsIn2DifferentSchemaWithTheSecondCalculatedFromTheFirstWhenExecutingAsyncTransactionThenAddedInCorrectOrder()
			throws Exception {
		defineSchemasManager().using(schemas.withAMetadataCopiedInAnotherSchema());

		Record zeSchemaRecord1 = recordServices.newRecordWithSchema(zeSchema.instance());
		zeSchemaRecord1.set(zeSchema.getCopiedMeta(), "1.1");
		recordServices.add(zeSchemaRecord1);

		Record zeSchemaRecord2 = recordServices.newRecordWithSchema(zeSchema.instance());
		zeSchemaRecord2.set(zeSchema.getCopiedMeta(), "2.1");
		recordServices.add(zeSchemaRecord2);

		Record anotherSchemaRecord = new TestRecord(anotherSchema);
		anotherSchemaRecord.set(anotherSchema.referenceToZeSchema(), zeSchemaRecord1.getId());
		recordServices.add(anotherSchemaRecord);

		assertThat(anotherSchemaRecord.get(anotherSchema.metadataWithCopiedEntry())).isEqualTo("1.1");

		Transaction initialTransaction = new Transaction();
		initialTransaction.addUpdate(anotherSchemaRecord.set(anotherSchema.referenceToZeSchema(), zeSchemaRecord2.getId()));
		initialTransaction.addUpdate(zeSchemaRecord1.set(zeSchema.getCopiedMeta(), "1.2"));
		initialTransaction.addUpdate(zeSchemaRecord2.set(zeSchema.getCopiedMeta(), "2.2"));
		recordServices.executeHandlingImpactsAsync(initialTransaction);

		assertThat(anotherSchemaRecord.get(anotherSchema.metadataWithCopiedEntry())).isEqualTo("2.2");
	}

	@Test
	public void givenUnmodifiableMetadataThenCanSetValueButCannotUpdateIt()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(whichIsUnmodifiable));
		RecordImpl record = saveZeSchemaRecordAndReload();
		assertThat(record.get(zeSchema.stringMetadata())).isNull();

		record = updateAndReload(record.set(zeSchema.stringMetadata(), "ze value"));
		assertThat(record.get(zeSchema.stringMetadata())).isEqualTo("ze value");

		record = updateAndReload(record.set(zeSchema.stringMetadata(), "ze value"));
		assertThat(record.get(zeSchema.stringMetadata())).isEqualTo("ze value");

		try {
			recordServices.update(record.set(zeSchema.stringMetadata(), "another value"));
			fail("ValidationException expected");
		} catch (ValidationException e) {
			Map<String, Object> parameters = asMap(METADATA_CODE, "zeSchemaType_default_stringMetadata");
			parameters.put(Validator.METADATA_LABEL, asMap("fr", "A toAString metadata"));
			assertThat(e.getErrors().getValidationErrors()).containsOnly(new ValidationError(
					MetadataUnmodifiableValidator.class, UNMODIFIABLE_METADATA,
					parameters)
			);
		}

		try {
			recordServices.update(record.set(zeSchema.stringMetadata(), null));
			fail("ValidationException expected");
		} catch (ValidationException e) {
			Map<String, Object> parameters = asMap(METADATA_CODE, "zeSchemaType_default_stringMetadata");
			parameters.put(Validator.METADATA_LABEL, asMap("fr", "A toAString metadata"));
			assertThat(e.getErrors().getValidationErrors()).containsOnly(new ValidationError(
					MetadataUnmodifiableValidator.class, UNMODIFIABLE_METADATA,
					parameters)
			);
		}

	}

	@SlowTest
	@Test
	public void whenExecutingWithMoreThan1000RecordsAndMergeOptimisticLockingResolutionThenOk()
			throws Exception {
		defineSchemasManager().using(schemas.withATitle().withAStringMetadata());
		doNothing().when(recordServices)
				.saveContentsAndRecords(any(Transaction.class), any(RecordModificationImpactHandler.class), anyInt());

		recordServices.execute(
				newTransactionWithNRecords(1000).setOptimisticLockingResolution(OptimisticLockingResolution.TRY_MERGE));
	}

	@Test(expected = RecordServicesRuntimeException_TransactionWithMoreThan1000RecordsCannotHaveTryMergeOptimisticLockingResolution.class)
	public void whenExecutingWithMoreThan1001RecordsAndMergeOptimisticLockingResolutionThenException()
			throws Exception {
		defineSchemasManager().using(schemas.withATitle().withAStringMetadata());
		recordServices.execute(
				newTransactionWithNRecords(1001).setOptimisticLockingResolution(OptimisticLockingResolution.TRY_MERGE));
	}

	@SlowTest
	@Test
	public void whenExecutingWithMoreThan1001RecordsAndThrowExceptionOptimisticLockingResolutionThenOk()
			throws Exception {
		defineSchemasManager().using(schemas.withATitle().withAStringMetadata());
		recordServices.execute(
				newTransactionWithNRecords(1001).setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION));
	}

	@SlowTest
	@Test
	public void whenExecutingWithMoreThan10000RecordsAndThrowExceptionOptimisticLockingResolutionThenOk()
			throws Exception {
		defineSchemasManager().using(schemas.withATitle().withAStringMetadata());
		recordServices.execute(
				newTransactionWithNRecords(10000).setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION));
	}

	@Test(expected = RecordServicesRuntimeException_TransactionHasMoreThan100000Records.class)
	public void whenExecutingWithMoreThan10001RecordsAndThrowExceptionOptimisticLockingResolutionThenException()
			throws Exception {
		defineSchemasManager().using(schemas.withATitle().withAStringMetadata());
		recordServices.execute(
				newTransactionWithNRecords(10001).setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION));
	}

	@SlowTest
	@Test
	public void whenExecutingASyncWithMoreThan1000RecordsAndMergeOptimisticLockingResolutionThenOk()
			throws Exception {
		defineSchemasManager().using(schemas.withATitle().withAStringMetadata());
		doNothing().when(recordServices)
				.executeWithImpactHandler(any(Transaction.class), any(RecordModificationImpactHandler.class));

		recordServices.executeHandlingImpactsAsync(
				newTransactionWithNRecords(1000).setOptimisticLockingResolution(
						OptimisticLockingResolution.TRY_MERGE));
	}

	@Test(expected = RecordServicesRuntimeException_TransactionWithMoreThan1000RecordsCannotHaveTryMergeOptimisticLockingResolution.class)
	public void whenExecutingASyncWithMoreThan1001RecordsAndMergeOptimisticLockingResolutionThenException()
			throws Exception {
		defineSchemasManager().using(schemas.withATitle().withAStringMetadata());
		recordServices.executeHandlingImpactsAsync(
				newTransactionWithNRecords(1001).setOptimisticLockingResolution(OptimisticLockingResolution.TRY_MERGE));
	}

	@SlowTest
	@Test
	public void whenExecutingASyncWithMoreThan1001RecordsAndThrowExceptionOptimisticLockingResolutionThenOk()
			throws Exception {
		defineSchemasManager().using(schemas.withATitle().withAStringMetadata());
		recordServices.executeHandlingImpactsAsync(
				newTransactionWithNRecords(1001).setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION));
	}

	@SlowTest
	@Test
	public void whenExecutingASyncWithMoreThan10000RecordsAndThrowExceptionOptimisticLockingResolutionThenOk()
			throws Exception {

		defineSchemasManager().using(schemas.withATitle().withAStringMetadata());
		recordServices.executeHandlingImpactsAsync(
				newTransactionWithNRecords(10000).setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION));
	}

	@Test(expected = RecordServicesRuntimeException_TransactionHasMoreThan100000Records.class)
	public void whenExecutingASyncWithMoreThan10001RecordsAndThrowExceptionOptimisticLockingResolutionThenException()
			throws Exception {
		defineSchemasManager().using(schemas.withATitle().withAStringMetadata());
		recordServices.executeHandlingImpactsAsync(
				newTransactionWithNRecords(10001).setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION));
	}

	@Test
	public void whenUpdateARecordAReferenceToANewRecord()
			throws Exception {
		defineSchemasManager().using(schemas.withAReferenceFromAnotherSchemaToZeSchema());

		Record anotherSchemaRecord = new TestRecord(anotherSchema).set(Schemas.TITLE, "New record saved in transaction 1");
		recordServices.add(anotherSchemaRecord);

		Record zeSchemaRecord = new TestRecord(zeSchema).set(Schemas.TITLE, "New record saved in transaction 2");
		anotherSchemaRecord.set(anotherSchema.metadata("referenceFromAnotherSchemaToZeSchema"), zeSchemaRecord.getId());

		recordServices.execute(new Transaction(anotherSchemaRecord, zeSchemaRecord));

	}

	@Test
	public void whenUpdateARecordAReferenceListToANewRecord()
			throws Exception {
		defineSchemasManager().using(schemas.with(metadataFromAnotherSchemaToZeSchema()));

		Record anotherSchemaRecord = new TestRecord(anotherSchema).set(Schemas.TITLE, "New record saved in transaction 1");
		recordServices.add(anotherSchemaRecord);

		Record zeSchemaRecord = new TestRecord(zeSchema).set(Schemas.TITLE, "New record saved in transaction 2");
		anotherSchemaRecord.set(anotherSchema.metadata("referenceFromAnotherSchemaToZeSchema"), asList(zeSchemaRecord.getId()));

		recordServices.execute(new Transaction(anotherSchemaRecord, zeSchemaRecord));

	}

	private MetadataSchemaTypesConfigurator metadataFromAnotherSchemaToZeSchema() {
		return new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaTypeBuilder zeSchemaTypeBuilder = schemaTypes.getSchemaType(zeSchema.typeCode());
				schemaTypes.getSchema(anotherSchema.code()).create("referenceFromAnotherSchemaToZeSchema")
						.defineReferencesTo(zeSchemaTypeBuilder).setMultivalue(true);
			}
		};
	}

	@Test
	public void whenUpdateARecordWithAnAutomaticReferenceListToANewRecord()
			throws Exception {
		defineSchemasManager().using(schemas.with(
				aMetadataInAnotherSchemaContainingAReferenceToZeSchemaAndACalculatorRetreivingIt()));

		Record anotherSchemaRecord = new TestRecord(anotherSchema).set(Schemas.TITLE, "New record saved in transaction 1");
		recordServices.add(anotherSchemaRecord);

		Record zeSchemaRecord = new TestRecord(zeSchema).set(Schemas.TITLE, "New record saved in transaction 2");
		anotherSchemaRecord.set(anotherSchema.metadata("aStringMetadata"), zeSchemaRecord.getId());

		recordServices.execute(new Transaction(anotherSchemaRecord, zeSchemaRecord));

	}

	private MetadataSchemaTypesConfigurator aMetadataInAnotherSchemaContainingAReferenceToZeSchemaAndACalculatorRetreivingIt() {
		return new MetadataSchemaTypesConfigurator() {

			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaBuilder anotherSchemaBuilder = schemaTypes.getSchema(anotherSchema.code());
				MetadataSchemaTypeBuilder zeSchemaTypeBuilder = schemaTypes.getSchemaType(zeSchema.typeCode());

				anotherSchemaBuilder.create("aStringMetadata").setType(MetadataValueType.STRING);
				anotherSchemaBuilder.create("aCalculatedMetadataToAReference").setType(MetadataValueType.REFERENCE)
						.setMultivalue(true)
						.defineReferencesTo(zeSchemaTypeBuilder)
						.defineDataEntry().asCalculated(RecordServicesAcceptanceTestCalculator.class);
			}
		};
	}

	public static class RecordServicesAcceptanceTestCalculator implements MetadataValueCalculator<List<String>> {

		LocalDependency<String> aStringMetadataParam = LocalDependency.toAString("aStringMetadata");

		@Override
		public List<String> calculate(CalculatorParameters parameters) {
			String aStringMetadata = parameters.get(aStringMetadataParam);
			return aStringMetadata == null ? null : Arrays.asList(aStringMetadata);
		}

		@Override
		public List<String> getDefaultValue() {
			return null;
		}

		@Override
		public MetadataValueType getReturnType() {
			return MetadataValueType.REFERENCE;
		}

		@Override
		public boolean isMultiValue() {
			return true;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return asList(aStringMetadataParam);
		}
	}

	private Record anotherSchemaRecordLinkedTo(Record record) {
		Record anotherSchemaRecord = recordServices.newRecordWithSchema(schemas.anotherDefaultSchema());
		anotherSchemaRecord.set(anotherSchema.referenceToZeSchema(), record.getId());
		return anotherSchemaRecord;
	}

	private Record thirdSchemaRecordLinkedTo(Record record) {
		Record anotherSchemaRecord = recordServices.newRecordWithSchema(schemas.aThirdDefaultSchema());
		anotherSchemaRecord.set(thirdSchema.referenceToAnotherSchema(), record.getId());
		return anotherSchemaRecord;
	}

	private Record zeSchemaRecordWithCopiedMeta(String value) {
		Record record = recordServices.newRecordWithSchema(schemas.zeDefaultSchema());
		record.set(zeSchema.getCopiedMeta(), value);
		return record;
	}

	private String addRecordInAnotherSchemaWithStringMetadataValue(boolean multivalue)
			throws RecordServicesException {
		Record recordReference = new TestRecord(anotherSchema);
		if (multivalue) {
			recordReference.set(anotherSchema.stringMetadata(), Arrays.asList("Banana", "Apple"));
		} else {
			recordReference.set(anotherSchema.stringMetadata(), "Banana");
		}
		recordServices.add(recordReference);
		return recordReference.getId();
	}

	private RecordImpl updateAndReload(Record record)
			throws RecordServicesException {
		recordServices.update(record);
		return (RecordImpl) recordServices.getDocumentById(record.getId());
	}

	private RecordImpl saveZeSchemaRecordAndReload()
			throws RecordServicesException {
		Record record = new TestRecord(zeSchema);
		recordServices.add(record);
		return (RecordImpl) recordServices.getDocumentById(record.getId());
	}

	private Record saveAnotherSchemaRecordWithDateMetadataToJanuary1()
			throws RecordServicesException {
		Record record = new TestRecord(anotherSchema);
		record.set(anotherSchema.dateMetadata(), january1);
		recordServices.add(record);
		return record;
	}

	private Record saveAnotherSchemaRecord()
			throws RecordServicesException {
		Record record = new TestRecord(anotherSchema);
		recordServices.add(record);
		return record;
	}

	private Record saveThirdSchemaRecord()
			throws RecordServicesException {
		Record record = new TestRecord(thirdSchema);
		recordServices.add(record);
		return record;
	}

	private Transaction newTransactionWithNRecords(int numberOfRecords) {
		Transaction transaction = new Transaction();

		for (int i = 0; i < numberOfRecords; i++) {
			transaction.addUpdate(new TestRecord(zeSchema));
		}

		return transaction;
	}

	private Map<String, Object> asMap(String key1, String value1) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(key1, value1);
		return parameters;
	}
}
