package com.constellio.model.services.records;

import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.services.bigVault.RecordDaoException;
import com.constellio.data.dao.services.records.DataStore;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.dao.services.sequence.SequencesManager;
import com.constellio.data.utils.Factory;
import com.constellio.model.conf.PropertiesModelLayerConfiguration.InMemoryModelLayerConfiguration;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordCreationEvent;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent;
import com.constellio.model.extensions.events.records.RecordModificationEvent;
import com.constellio.model.extensions.events.records.TransactionExecutionBeforeSaveEvent;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.Validator;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.encrypt.EncryptionKeyFactory;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import com.constellio.model.services.records.RecordServicesRuntimeException.CannotSetIdsToReindexInEmptyTransaction;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_TransactionHasMoreThan100000Records;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_TransactionWithMoreThan1000RecordsCannotHaveTryMergeOptimisticLockingResolution;
import com.constellio.model.services.records.RecordServicesRuntimeException.SchemaTypeOfARecordHasReadOnlyLock;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilder_EnumClassTest.AValidEnum;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.validators.MetadataUnmodifiableValidator;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.FakeEncryptionServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.GetByIdCounter;
import com.constellio.sdk.tests.ModelLayerConfigurationAlteration;
import com.constellio.sdk.tests.QueryCounter;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.TestUtils;
import com.constellio.sdk.tests.annotations.IntermittentFailureTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.entities.schemas.MetadataTransiency.TRANSIENT_EAGER;
import static com.constellio.model.entities.schemas.MetadataTransiency.TRANSIENT_LAZY;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.RecordCacheType.SUMMARY_CACHED_WITHOUT_VOLATILE;
import static com.constellio.model.entities.schemas.Schemas.ESTIMATED_SIZE;
import static com.constellio.model.entities.schemas.Schemas.MARKED_FOR_REINDEXING;
import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.model.frameworks.validation.Validator.METADATA_CODE;
import static com.constellio.model.services.records.RecordServicesAcceptanceTestUtils.calculatedReferenceFromDummyCalculatorUsingOtherMetadata;
import static com.constellio.model.services.records.RecordServicesAcceptanceTestUtils.calculatedTextFromDummyCalculator;
import static com.constellio.model.services.records.RecordServicesAcceptanceTestUtils.calculatedTextListFromDummyCalculator;
import static com.constellio.model.services.records.RecordServicesAcceptanceTestUtils.calculatedTextListFromDummyCalculatorReturningInvalidType;
import static com.constellio.model.services.schemas.validators.MetadataUnmodifiableValidator.UNMODIFIABLE_METADATA;
import static com.constellio.model.services.search.query.logical.LogicalSearchQuery.query;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.limitedTo50Characters;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichAllowsAnotherDefaultSchema;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasDefaultRequirement;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasInputMask;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasTransiency;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsCalculatedUsing;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEncrypted;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsNotSearchable;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsScripted;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSortable;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsUnmodifiable;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RecordServicesAcceptanceTest extends ConstellioTest {
	LocalDate shishDay = new LocalDate().minusDays(42);
	LocalDate tockDay = new LocalDate().minusDays(666);

	private final String valueTooLong = "this title is too lonnnnnnnnnnnnnnnnnnnnnnnnnnnnnng";
	RecordServicesTestSchemaSetup schemas;
	RecordServicesTestSchemaSetup.ZeSchemaMetadatas zeSchema;
	RecordServicesTestSchemaSetup.AnotherSchemaMetadatas anotherSchema;
	RecordServicesTestSchemaSetup.ThirdSchemaMetadatas thirdSchema;
	LocalDateTime january1 = new LocalDateTime(2014, 1, 1, 0, 0, 0);
	LocalDateTime january2 = new LocalDateTime(2014, 1, 2, 0, 0, 0);
	BatchProcessesManager batchProcessesManager;
	private RecordServicesImpl recordServices;
	RecordDao recordDao;

	@Before
	public void setup()
			throws Exception {
		System.out.println("\n\n--RecordServicesAcceptanceTest.setup--\n\n");
		givenDisabledAfterTestValidations();
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

		recordServices = getModelLayerFactory().newCachelessRecordServices();
		batchProcessesManager = getModelLayerFactory().getBatchProcessesManager();
		schemas = new RecordServicesTestSchemaSetup();

		getAppLayerFactory().getCollectionsManager().createCollectionInCurrentVersion(zeCollection, Arrays.asList("fr"));
		MetadataSchemasManager manager = getModelLayerFactory().getMetadataSchemasManager();

		MetadataSchemaTypes types = manager.getSchemaTypes(zeCollection);

		schemas.onSchemaBuilt(types);
		zeSchema = schemas.new ZeSchemaMetadatas();
		anotherSchema = schemas.new AnotherSchemaMetadatas();
		thirdSchema = schemas.new ThirdSchemaMetadatas();

		recordDao = getDataLayerFactory().newRecordDao();
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void givenRequiredAutomaticMetadataWhenSavingWithNullValueThenValidationException()
			throws Exception {
		System.out.println("\n\n--ze test--\n\n");
		defineSchemasManager().using(
				schemas.withAStringMetadata(whichHasDefaultRequirement, calculatedTextFromDummyCalculator()));

		Record record = new TestRecord(zeSchema, "zeUltimateRecord");
		record.set(zeSchema.metadata("other"), null);
		recordServices.add(record);
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void givenAutomaticMetadataWhenSavingWithValueWhichIsInvalidValueThenValidationException()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(limitedTo50Characters, calculatedTextFromDummyCalculator()));

		Record record = new TestRecord(zeSchema, "zeUltimateRecord");

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

		Record record = new TestRecord(zeSchema, "zeUltimateRecord");

		record.set(zeSchema.metadata("other"), recordUnallowed.getId());
		recordServices.add(record);
	}

	@Test
	public void whenMarkingOrUnmarkingRecordWithoutRecalculateThenNoRecalculate() throws Exception {

		defineSchemasManager().using(schemas.with((types) -> {

			types.getSchemaType(zeSchema.typeCode()).setRecordCacheType(RecordCacheType.NOT_CACHED);
			types.getSchemaType(anotherSchema.typeCode()).setRecordCacheType(RecordCacheType.NOT_CACHED);

			types.getSchemaType(zeSchema.typeCode()).createMetadata("copied").setType(STRING);
			types.getSchemaType(anotherSchema.typeCode()).createMetadata("ref")
					.defineReferencesTo(types.getSchemaType(zeSchema.typeCode()));
			types.getSchemaType(anotherSchema.typeCode()).createMetadata("calculated").setType(STRING)
					.defineDataEntry().asJexlScript("ref.copied");
		}));


		Transaction tx = new Transaction();
		String r1 = tx.add(newRecord(zeSchema).set(zeSchema.metadata("copied"), "toto")).getId();
		String r2 = tx.add(newRecord(anotherSchema).set(anotherSchema.metadata("ref"), r1)).getId();
		recordServices.execute(tx);

		assertThat(record(r2).<String>get(anotherSchema.metadata("calculated"))).isEqualTo("toto");

		tx = new Transaction();
		tx.add(record(r1).set(zeSchema.metadata("copied"), "banana"));
		recordServices.executeHandlingImpactsAsync(tx);
		assertThat(record(r2).<String>get(anotherSchema.metadata("calculated"))).isEqualTo("toto");

		GetByIdCounter counter = new GetByIdCounter(getClass()).listening(getDataLayerFactory());

		tx = new Transaction();
		tx.add(record(r2).set(Schemas.MARKED_FOR_PREVIEW_CONVERSION, true));
		tx.getRecordUpdateOptions().setUpdateCalculatedMetadatas(false);
		tx.getRecordUpdateOptions().setForcedReindexationOfMetadatas(TransactionRecordsReindexation.ALL());
		recordServices.execute(tx);
		assertThat(record(r2).<String>get(anotherSchema.metadata("calculated"))).isEqualTo("toto");

		assertThat(counter.newIdCalled()).doesNotContain(r1);

		tx = new Transaction();
		tx.add(record(r2).set(Schemas.MARKED_FOR_PREVIEW_CONVERSION, null));
		tx.getRecordUpdateOptions().setForcedReindexationOfMetadatas(TransactionRecordsReindexation.ALL());
		recordServices.execute(tx);

		assertThat(record(r2).<String>get(anotherSchema.metadata("calculated"))).isEqualTo("banana");

		assertThat(counter.newIdCalled()).contains(r1);
	}

	@Test
	public void givenAutomaticReferenceMetadataWhenSavingWithValueOfAllowedSchemaThenOK()
			throws Exception {
		defineSchemasManager().using(
				schemas.withAReferenceMetadata(whichAllowsAnotherDefaultSchema,
						calculatedReferenceFromDummyCalculatorUsingOtherMetadata()));
		Record recordUnallowed = saveAnotherSchemaRecord();

		Record record = new TestRecord(zeSchema, "zeUltimateRecord");

		record.set(zeSchema.metadata("other"), recordUnallowed.getId());
		recordServices.add(record);
	}

	@Test
	public void givenRequiredAutomaticMetadataWhenSavingWithValueThenOK()
			throws Exception {
		defineSchemasManager().using(
				schemas.withAStringMetadata(whichHasDefaultRequirement, calculatedTextFromDummyCalculator()));
		Record record = new TestRecord(zeSchema, "zeUltimateRecord");
		record.set(zeSchema.metadata("other"), "aValue");
		recordServices.add(record);
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void givenRequiredAutomaticMultivalueMetadataWhenSavingWithNullListThenValidationException()
			throws Exception {
		defineSchemasManager().using(
				schemas.withAStringMetadata(whichHasDefaultRequirement, calculatedTextListFromDummyCalculator()));
		Record record = new TestRecord(zeSchema, "zeUltimateRecord");
		record.set(zeSchema.metadata("other"), null);
		recordServices.add(record);
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void givenRequiredAutomaticMultivalueMetadataWhenSavingWithEmptyListThenValidationException()
			throws Exception {
		defineSchemasManager().using(
				schemas.withAStringMetadata(whichHasDefaultRequirement, calculatedTextListFromDummyCalculator()));
		Record record = new TestRecord(zeSchema, "zeUltimateRecord");
		record.set(zeSchema.metadata("other"), new ArrayList<>());
		recordServices.add(record);
	}

	@Test
	public void givenRequiredAutomaticMultivalueMetadataWhenSavingWithValueThenOK()
			throws Exception {
		defineSchemasManager().using(
				schemas.withAStringMetadata(whichHasDefaultRequirement, calculatedTextListFromDummyCalculator()));
		Record record = new TestRecord(zeSchema, "zeUltimateRecord");
		record.set(zeSchema.metadata("other"), asList("aValue"));
		recordServices.add(record);
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void givenRequiredAutomaticMetadataWhenSavingWithInvalidTypeThenValidationException()
			throws Exception {
		defineSchemasManager().using(
				schemas.withAStringMetadata(whichHasDefaultRequirement,
						calculatedTextListFromDummyCalculatorReturningInvalidType()));
		Record record = new TestRecord(zeSchema, "zeUltimateRecord");
		record.set(zeSchema.metadata("other"), 1);
		recordServices.add(record);
	}

	@Test
	public void whenAddingSomeRecordsThenDocumentsCountIsCorrect()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata().withAnotherStringMetadata());
		long initialDocumentsCount = recordServices.documentsCount();
		Record record;
		for (int i = 0; i < 11; i++) {
			record = new TestRecord(zeSchema);
			recordServices.add(record.set(zeSchema.stringMetadata(), "value " + i));
		}

		assertThat(recordServices.documentsCount() - initialDocumentsCount).isEqualTo(11);
	}

	@Test
	public void givenSchemaWithEnumListWhenAddingRecordThenValuesPersisted()
			throws Exception {
		defineSchemasManager().using(schemas.withAnEnumMetadata(AValidEnum.class, whichIsMultivalue));
		Record record = new TestRecord(zeSchema, "zeUltimateRecord");
		record.set(zeSchema.enumMetadata(), asList(AValidEnum.SECOND_VALUE, AValidEnum.FIRST_VALUE));

		recordServices.add(record);
		record = recordServices.getDocumentById(record.getId());

		assertThat(record.<List<Object>>get(zeSchema.enumMetadata())).isEqualTo(asList(AValidEnum.SECOND_VALUE, AValidEnum.FIRST_VALUE));
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void givenSchemaWithValidatorsWhenAddingRecordFailingValidationThenThrowValidationException()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(limitedTo50Characters));

		Record record = new TestRecord(zeSchema, "zeUltimateRecord");
		record.set(zeSchema.stringMetadata(), valueTooLong);

		recordServices.add(record);
	}

	@Test(expected = RecordServicesException.ValidationException.class)
	public void givenSchemaWithValidatorsWhenUpdatingRecordFailingValidationThenThrowValidationException()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(limitedTo50Characters));
		Record record = new TestRecord(zeSchema, "zeUltimateRecord");
		recordServices.add(record.set(zeSchema.stringMetadata(), "Banana"));

		record = new TestRecord(zeSchema, "zeUltimateRecord");
		record.set(zeSchema.stringMetadata(), valueTooLong);

		recordServices.update(record);
	}

	@Test(expected = SchemaTypeOfARecordHasReadOnlyLock.class)
	public void whenAddUpdatingRecordWithReadOnlyLockThenException()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata());
		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType("zeSchemaType").setReadOnlyLocked(true);
			}
		});

		Record record = new TestRecord(zeSchema, "zeUltimateRecord");

		recordServices.add(record.set(zeSchema.stringMetadata(), "Banana"));
	}

	@Test
	public void whenAddUpdatingRecordWithReadOnlyLockWithFlagAllowingItThenNoException()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata());
		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType("zeSchemaType").setReadOnlyLocked(true);
			}
		});

		Record record = new TestRecord(zeSchema, "zeUltimateRecord");
		Transaction transaction = new Transaction();
		transaction.getRecordUpdateOptions().setAllowSchemaTypeLockedRecordsModification(true);
		transaction.add(record.set(zeSchema.stringMetadata(), "Banana"));
		recordServices.execute(transaction);
	}

	@Test
	public void givenAnOptimisticLockingOccursDuringATransactionThenRecordsMergedAndMarkedForReindexingAreSentAgainWithoutBeingMerged()
			throws Exception {

		defineSchemasManager().using(schemas.withAStringMetadata().withAnotherStringMetadata());

		Transaction tx = new Transaction();
		Record record1 = tx.add(new TestRecord(zeSchema, "record1").set(zeSchema.stringMetadata(), "value1"));
		Record record2 = tx.add(new TestRecord(zeSchema, "record2").set(zeSchema.stringMetadata(), "value1"));
		Record record3 = tx.add(new TestRecord(zeSchema, "record3").set(zeSchema.stringMetadata(), "value1"));
		recordServices.execute(tx);

		recordServices.update(record("record1").set(zeSchema.anotherStringMetadata(), "mouahahaha"));

		tx = new Transaction();
		tx.add(record1.set(zeSchema.stringMetadata(), "value2"));
		tx.add(record2.set(zeSchema.stringMetadata(), "value2"));
		tx.addRecordToReindex("record3");
		recordServices.execute(tx);

		assertThatRecord(record1).extracting(zeSchema.stringMetadata(), zeSchema.anotherStringMetadata())
				.isEqualTo(asList("value2", "mouahahaha"));

		assertThatRecord(record2).extracting(zeSchema.stringMetadata())
				.isEqualTo(asList("value2"));

		assertThatRecord(record("record3")).extracting(zeSchema.stringMetadata(), Schemas.MARKED_FOR_REINDEXING)
				.isEqualTo(asList("value1", true));

	}

	@Test
	public void givenAnOptimisticLockingOccursDuringATransactionThenRecordsMergedAndANewRecordIsMarkedForReindexingThenOK()
			throws Exception {

		defineSchemasManager().using(schemas.withAStringMetadata().withAnotherStringMetadata());

		Transaction tx = new Transaction();
		Record record1 = tx.add(new TestRecord(zeSchema, "record1").set(zeSchema.stringMetadata(), "value1"));
		Record record2 = tx.add(new TestRecord(zeSchema, "record2").set(zeSchema.stringMetadata(), "value1"));

		recordServices.execute(tx);

		recordServices.update(record("record1").set(zeSchema.anotherStringMetadata(), "mouahahaha"));

		tx = new Transaction();
		tx.add(record1.set(zeSchema.stringMetadata(), "value2"));
		tx.add(record2.set(zeSchema.stringMetadata(), "value2"));
		tx.add(new TestRecord(zeSchema, "record3").set(zeSchema.stringMetadata(), "value1"));
		tx.addRecordToReindex("record3");
		recordServices.execute(tx);

		assertThatRecord(record1).extracting(zeSchema.stringMetadata(), zeSchema.anotherStringMetadata())
				.isEqualTo(asList("value2", "mouahahaha"));

		assertThatRecord(record2).extracting(zeSchema.stringMetadata())
				.isEqualTo(asList("value2"));

		assertThatRecord(record("record3")).extracting(zeSchema.stringMetadata(), Schemas.MARKED_FOR_REINDEXING)
				.isEqualTo(asList("value1", true));

	}

	@Test
	public void givenRecordInCacheMarkedForReindexingThenVersionInCacheIsUpdated()
			throws Exception {

		defineSchemasManager().using(schemas.withAStringMetadata().withAnotherStringMetadata());

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeSchema.typeCode()).setRecordCacheType(RecordCacheType.FULLY_CACHED);
			}
		});


		Transaction tx = new Transaction();
		Record record1 = tx.add(new TestRecord(zeSchema, "record1").set(zeSchema.stringMetadata(), "value1"));
		Record record2 = tx.add(new TestRecord(zeSchema, "record2").set(zeSchema.stringMetadata(), "value1"));

		recordServices.execute(tx);

		tx = new Transaction();
		tx.add(record2.set(zeSchema.anotherStringMetadata(), "mouahahaha"));
		tx.addRecordToReindex("record1");
		recordServices.execute(tx);

		assertThat(getModelLayerFactory().newRecordServices().getDocumentById("record1").getVersion())
				.isNotEqualTo(record1.getVersion())
				.isEqualTo(getModelLayerFactory().newCachelessRecordServices().getDocumentById("record1").getVersion());


	}

	@Test()
	public void givenSchemaWithFixedSequenceMetadataWhenAddingValidRecordThenSetNewSequenceValue()
			throws Exception {
		//TODO AFTER-TEST-VALIDATION-SEQ
		givenDisabledAfterTestValidations();

		defineSchemasManager().using(schemas.withAFixedSequence());
		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).get("title").setDefaultRequirement(true);
				types.getSchema(zeSchema.code()).create("calculatedOnFixedSequence").setType(STRING)
						.defineDataEntry().asJexlScript("'F'+ fixedSequenceMetadata + '.00'");

			}
		});

		Record record;
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		try {
			recordServices.add(record);
		} catch (RecordServicesException.ValidationException e) {
			//OK
		}

		assertThat(record.<String>get(zeSchema.fixedSequenceMetadata())).isNull();
		assertThat(record.<String>get(zeSchema.metadata("calculatedOnFixedSequence"))).isNull();

		record.set(TITLE, "Ze title");

		try {
			recordServices.add(record);
		} catch (RecordServicesException.ValidationException e) {
			//OK
		}

		assertThat(record.<String>get(zeSchema.fixedSequenceMetadata())).isEqualTo("1");
		assertThat(record.<String>get(zeSchema.metadata("calculatedOnFixedSequence"))).isEqualTo("F1.00");

		record = recordServices.newRecordWithSchema(zeSchema.instance());
		record.set(TITLE, "Ze title");
		try {
			recordServices.add(record);
		} catch (RecordServicesException.ValidationException e) {
			//OK
		}

		assertThat(record.<String>get(zeSchema.fixedSequenceMetadata())).isEqualTo("2");
		assertThat(record.<String>get(zeSchema.metadata("calculatedOnFixedSequence"))).isEqualTo("F2.00");

	}

	@Test()
	public void givenSchemaWithDynamicSequenceMetadataReferencingAnotherSchemaThenSetValidSequenceValue()
			throws Exception {

		//TODO AFTER-TEST-VALIDATION-SEQ

		// prepare test
		givenDisabledAfterTestValidations();
		defineSchemasManager().using(schemas);
		SequencesManager sequencesManager = getDataLayerFactory().getSequencesManager();
		// defines metadatas
		final String DYNAMIC_SEQUENCE_METADATA = "dynamicSequenceMetadata"; // seq : 1
		final String REFERENCED_CODE_METADATA = "referencedCodeMetadata"; // schemaType
		final String REFERENCED_CODE_METADATA_CODE = REFERENCED_CODE_METADATA + ".code"; // schemaType => specificSchema.metadata

		final String UNIT_CODE_METADATA = "code";

		// creates metadatas in schemas
		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder schemaBuilderAccessor) {

				final MetadataSchemaBuilder DEFAULT_FOLDER_SCHEMA = schemaBuilderAccessor.getSchema(zeSchema.code());
				final MetadataSchemaBuilder DEFAULT_UNIT_SCHEMA = schemaBuilderAccessor.getSchema(anotherSchema.code());

				DEFAULT_UNIT_SCHEMA.create(UNIT_CODE_METADATA).setType(STRING);
				DEFAULT_FOLDER_SCHEMA.create(REFERENCED_CODE_METADATA).setType(MetadataValueType.REFERENCE).defineReferencesTo(schemaBuilderAccessor.getSchemaType(anotherSchema.typeCode())); // .typeCode to be more generally available (not specific to schema_default but to all schema with same type
				DEFAULT_FOLDER_SCHEMA.create(DYNAMIC_SEQUENCE_METADATA).setType(STRING).defineDataEntry().asSequenceDefinedByMetadata(REFERENCED_CODE_METADATA_CODE);
			}
		});

		// whenNoReferenceThenNoSequentialNumber

		Record anotherSchemaRecord = recordServices.newRecordWithSchema(anotherSchema.instance());
		Record zeSchemaRecord = recordServices.newRecordWithSchema(zeSchema.instance());

		try {
			recordServices.add(anotherSchemaRecord);
			recordServices.add(zeSchemaRecord);
		} catch (RecordServicesException.ValidationException e) {
			//OK
		}

		assertThat(zeSchemaRecord.<String>get(zeSchema.metadata(REFERENCED_CODE_METADATA))).isNull();
		assertThat(zeSchemaRecord.<String>get(zeSchema.metadata(DYNAMIC_SEQUENCE_METADATA))).isNull();

		// whenFirstReferenceThenSequentialNumber

		String DEFAULT_REFEREE_CODE = "defaultRefereeCode";

		anotherSchemaRecord.set(anotherSchema.metadata(UNIT_CODE_METADATA), DEFAULT_REFEREE_CODE);
		zeSchemaRecord.set(zeSchema.metadata(REFERENCED_CODE_METADATA), anotherSchemaRecord.getId());

		recordServices.update(anotherSchemaRecord);
		recordServices.update(zeSchemaRecord);

		assertThat(anotherSchemaRecord.<String>get(anotherSchema.metadata(UNIT_CODE_METADATA))).isEqualTo(DEFAULT_REFEREE_CODE);
		assertThat(zeSchemaRecord.<String>get(zeSchema.metadata(REFERENCED_CODE_METADATA))).isEqualTo(anotherSchemaRecord.getId());
		assertThat(zeSchemaRecord.<String>get(zeSchema.metadata(DYNAMIC_SEQUENCE_METADATA))).isEqualTo("1");

		// whenSecondReferenceThenIncrementedSequentialNumber

		Record zeSchemaRecord2 = recordServices.newRecordWithSchema(zeSchema.instance());
		zeSchemaRecord2.set(zeSchema.metadata(REFERENCED_CODE_METADATA), anotherSchemaRecord.getId());

		try {
			recordServices.add(zeSchemaRecord2);
		} catch (RecordServicesException.ValidationException e) {
			//OK
		}

		assertThat(zeSchemaRecord2.<String>get(zeSchema.metadata(REFERENCED_CODE_METADATA))).isEqualTo(anotherSchemaRecord.getId());
		assertThat(zeSchemaRecord2.<String>get(zeSchema.metadata(DYNAMIC_SEQUENCE_METADATA))).isEqualTo("2");

		// whenNewReferralThenResettedSequentialNumber

		String DEFAULT_REFEREE_CODE_2 = "defaultRefereeCode2";

		Record anotherSchemaRecord2 = recordServices.newRecordWithSchema(anotherSchema.instance());
		anotherSchemaRecord2.set(anotherSchema.metadata(UNIT_CODE_METADATA), DEFAULT_REFEREE_CODE_2);

		try {
			recordServices.add(anotherSchemaRecord2);
		} catch (RecordServicesException.ValidationException e) {
			//OK
		}

		zeSchemaRecord2.set(zeSchema.metadata(REFERENCED_CODE_METADATA), anotherSchemaRecord2.getId());

		recordServices.update(zeSchemaRecord2);

		assertThat(anotherSchemaRecord2.<String>get(anotherSchema.metadata(UNIT_CODE_METADATA))).isEqualTo(DEFAULT_REFEREE_CODE_2);
		assertThat(zeSchemaRecord2.<String>get(zeSchema.metadata(REFERENCED_CODE_METADATA))).isEqualTo(anotherSchemaRecord2.getId());
		assertThat(zeSchemaRecord2.<String>get(zeSchema.metadata(DYNAMIC_SEQUENCE_METADATA))).isEqualTo("1");

		// whenOldReferralThenIncrementedSequentialNumber

		zeSchemaRecord2.set(zeSchema.metadata(REFERENCED_CODE_METADATA), anotherSchemaRecord.getId());
		recordServices.update(zeSchemaRecord2);

		assertThat(zeSchemaRecord2.<String>get(zeSchema.metadata(REFERENCED_CODE_METADATA))).isEqualTo(anotherSchemaRecord.getId());
		assertThat(zeSchemaRecord2.<String>get(zeSchema.metadata(DYNAMIC_SEQUENCE_METADATA))).isEqualTo("3");

		// whenOperationsDoneOnSequentialTablesThenGoodIncrementations

		assertThat(sequencesManager.getLastSequenceValue(DEFAULT_REFEREE_CODE)).isEqualTo(3);
		assertThat(sequencesManager.getLastSequenceValue(DEFAULT_REFEREE_CODE_2)).isEqualTo(1);

	}

	@Test()
	public void givenSchemaWithFixedSequenceMetadataWithPatternWhenAddingValidRecordThenSetNewSequenceValue()
			throws Exception {

		//TODO AFTER-TEST-VALIDATION-SEQ
		givenDisabledAfterTestValidations();

		defineSchemasManager().using(schemas.withAFixedSequence(whichHasInputMask("99999")));
		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).get("title").setDefaultRequirement(true);
				types.getSchema(zeSchema.code()).create("calculatedOnFixedSequence").setType(STRING)
						.defineDataEntry().asJexlScript("'F'+ fixedSequenceMetadata + '.00'");

			}
		});

		Record record = new TestRecord(zeSchema, "zeUltimateRecord");
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		try {
			recordServices.add(record);
		} catch (RecordServicesException.ValidationException e) {
			//OK
		}

		assertThat(record.<String>get(zeSchema.fixedSequenceMetadata())).isNull();
		assertThat(record.<String>get(zeSchema.metadata("calculatedOnFixedSequence"))).isNull();

		record.set(TITLE, "Ze title");

		try {
			recordServices.add(record);
		} catch (RecordServicesException.ValidationException e) {
			//OK
		}

		assertThat(record.<String>get(zeSchema.fixedSequenceMetadata())).isEqualTo("00001");
		assertThat(record.<String>get(zeSchema.metadata("calculatedOnFixedSequence"))).isEqualTo("F00001.00");

		record = recordServices.newRecordWithSchema(zeSchema.instance());
		record.set(TITLE, "Ze title");
		try {
			recordServices.add(record);
		} catch (RecordServicesException.ValidationException e) {
			//OK
		}

		assertThat(record.<String>get(zeSchema.fixedSequenceMetadata())).isEqualTo("00002");
		assertThat(record.<String>get(zeSchema.metadata("calculatedOnFixedSequence"))).isEqualTo("F00002.00");

	}

	@Test()
	public void givenSchemaWithDynamicSequenceMetadataWhenChangeSequenceSourceThenGetNewSequenceUsingNewSource()
			throws Exception {

		//TODO AFTER-TEST-VALIDATION-SEQ
		givenDisabledAfterTestValidations();

		SequencesManager sequencesManager = getDataLayerFactory().getSequencesManager();
		sequencesManager.set("sequence1", 42);
		sequencesManager.set("sequence2", 666);

		defineSchemasManager().using(schemas.withADynamicSequence());

		Record record = new TestRecord(zeSchema, "zeUltimateRecord");
		record = recordServices.newRecordWithSchema(zeSchema.instance());

		recordServices.add(record);

		assertThat(record.<String>get(zeSchema.dynamicSequenceMetadata())).isNull();

		recordServices.update(record.set(zeSchema.metadataDefiningSequenceNumber(), "sequence1"));
		assertThat(record.<String>get(zeSchema.dynamicSequenceMetadata())).isEqualTo("43");

		recordServices.update(record.set(zeSchema.metadataDefiningSequenceNumber(), "sequence2"));
		assertThat(record.<String>get(zeSchema.dynamicSequenceMetadata())).isEqualTo("667");

		recordServices.update(record.set(zeSchema.metadataDefiningSequenceNumber(), "sequence1"));
		assertThat(record.<String>get(zeSchema.dynamicSequenceMetadata())).isEqualTo("44");

		recordServices.update(record.set(TITLE, "zeTitle"));
		assertThat(record.<String>get(zeSchema.dynamicSequenceMetadata())).isEqualTo("44");

	}

	@Test()
	public void givenSchemaWithCopiedMetadataWhenAddingRecordThenCopyValues()
			throws Exception {
		defineSchemasManager().using(
				schemas.withTwoMetadatasCopyingAnotherSchemaValuesUsingTwoDifferentReferenceMetadata(false, false, false));
		String referencedRecordId = addRecordInAnotherSchemaWithStringMetadataValue(false);

		Record record = new TestRecord(zeSchema, "zeUltimateRecord");
		record = recordServices.newRecordWithSchema(zeSchema.instance());
		record.set(zeSchema.firstReferenceToAnotherSchema(), referencedRecordId);

		recordServices.add(record);

		assertThat(record.<String>get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo("Banana");
	}

	@Test()
	public void givenSchemaWithCopiedMultivalueMetadataWhenAddingRecordThenCopyValues()
			throws Exception {
		defineSchemasManager().using(
				schemas.withTwoMetadatasCopyingAnotherSchemaValuesUsingTwoDifferentReferenceMetadata(true, false, false));
		String referencedRecordId = addRecordInAnotherSchemaWithStringMetadataValue(true);

		Record record = new TestRecord(zeSchema, "zeUltimateRecord");
		record = recordServices.newRecordWithSchema(zeSchema.instance());
		record.set(zeSchema.firstReferenceToAnotherSchema(), referencedRecordId);

		recordServices.add(record);

		assertThat(record.<List<String>>get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo(Arrays.asList("Banana", "Apple"));
	}

	@Test()
	public void givenSchemaWithCopiedMetadataWhenUpdatingRecordThenCopyValues()
			throws Exception {
		defineSchemasManager().using(
				schemas.withTwoMetadatasCopyingAnotherSchemaValuesUsingTwoDifferentReferenceMetadata(false, false, false));
		String referencedRecordId = addRecordInAnotherSchemaWithStringMetadataValue(false);

		Record record = new TestRecord(zeSchema, "zeUltimateRecord");
		record = recordServices.newRecordWithSchema(zeSchema.instance());
		record.set(zeSchema.firstReferenceToAnotherSchema(), referencedRecordId);
		recordServices.add(record);

		record.set(zeSchema.firstReferenceToAnotherSchema(), null);
		recordServices.update(record);

		assertThat(record.<String>get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isNull();
	}

	@Test()
	public void givenSchemaWithCopiedMultivalueMetadataWhenUpdatingRecordThenCopyValues()
			throws Exception {
		defineSchemasManager().using(
				schemas.withTwoMetadatasCopyingAnotherSchemaValuesUsingTwoDifferentReferenceMetadata(true, false, false));
		String referencedRecordId = addRecordInAnotherSchemaWithStringMetadataValue(true);

		Record record = new TestRecord(zeSchema, "zeUltimateRecord");
		record = recordServices.newRecordWithSchema(zeSchema.instance());
		record.set(zeSchema.firstReferenceToAnotherSchema(), referencedRecordId);
		recordServices.add(record);

		record.set(zeSchema.firstReferenceToAnotherSchema(), null);
		recordServices.update(record);

		assertThat(record.<List<String>>get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo(new ArrayList<>());
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

		assertThat(record.<Double>get(zeSchema.calculatedDaysBetween())).isEqualTo(1.0);
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
		assertThat(record.<Double>get(zeSchema.calculatedDaysBetween())).isEqualTo(1.0);

		record.set(zeSchema.dateTimeMetadata(), january1);
		recordServices.update(record);

		assertThat(record.<Double>get(zeSchema.calculatedDaysBetween())).isEqualTo(0.0);
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
		assertThat(record.<Double>get(zeSchema.calculatedDaysBetween())).isEqualTo(1.0);

		record.set(zeSchema.dateTimeMetadata(), null);
		recordServices.update(record);

		assertThat(record.<Double>get(zeSchema.calculatedDaysBetween())).isEqualTo(-1.0);
	}

	private Record reloadRecord(String id) {
		return recordServices.getDocumentById(id);
	}

	@Test
	public void whenAddingOrUpdatingLargeTextMetadataThenValueIsSet()
			throws Exception {
		defineSchemasManager().using(schemas.withATitle().withALargeTextMetadata());

		Record record = new TestRecord(zeSchema, "zeUltimateRecord");
		recordServices.add(record.set(zeSchema.title(), "title").set(zeSchema.largeTextMetadata(), "firstValue"));
		record = reloadRecord(record.getId());
		assertThat(reloadRecord(record.getId()).<String>get(zeSchema.largeTextMetadata())).isEqualTo("firstValue");

		recordServices.update(reloadRecord(record.getId()).set(zeSchema.largeTextMetadata(), "secondValue"));
		assertThat(reloadRecord(record.getId()).<String>get(zeSchema.largeTextMetadata())).isEqualTo("secondValue");

		recordServices.update(reloadRecord(record.getId()).set(zeSchema.largeTextMetadata(), null));
		assertThat(reloadRecord(record.getId()).<String>get(zeSchema.largeTextMetadata())).isEqualTo(null);
	}

	@Test
	public void whenAddingOrUpdatingMultivalueLargeTextMetadataThenValueIsSet()
			throws Exception {
		defineSchemasManager().using(schemas.withATitle().withALargeTextMetadata(whichIsMultivalue));

		Record record = new TestRecord(zeSchema, "zeUltimateRecord");
		recordServices.add(record.set(zeSchema.title(), "title")
				.set(zeSchema.largeTextMetadata(), asList("firstValue", "secondValue")));
		assertThat(reloadRecord(record.getId()).<List<String>>get(zeSchema.largeTextMetadata())).isEqualTo(asList("firstValue", "secondValue"));

		recordServices.update(reloadRecord(record.getId()).set(zeSchema.largeTextMetadata(), asList("secondValue", "thirdValue")));
		assertThat(reloadRecord(record.getId()).<List<String>>get(zeSchema.largeTextMetadata())).isEqualTo(asList("secondValue", "thirdValue"));

		recordServices.update(reloadRecord(record.getId()).set(zeSchema.largeTextMetadata(), null));
		assertThat(reloadRecord(record.getId()).getList(zeSchema.largeTextMetadata())).isEqualTo(new ArrayList<>());

		recordServices.update(reloadRecord(record.getId()).set(zeSchema.largeTextMetadata(), new ArrayList<>()));
		assertThat(reloadRecord(record.getId()).getList(zeSchema.largeTextMetadata())).isEqualTo(new ArrayList<>());

		recordServices.update(reloadRecord(record.getId()).set(zeSchema.largeTextMetadata(), asList("zeValue")));
		assertThat(reloadRecord(record.getId()).<List<String>>get(zeSchema.largeTextMetadata())).isEqualTo(asList("zeValue"));
	}

	@Test
	public void givenAddedRecordWhenModifyingSingleValueAndUpdatingThenModificationsSavedAndVersionChanged()
			throws Exception {
		defineSchemasManager().using(schemas.withATitle().withABooleanMetadata(whichIsMultivalue));
		Record record = new TestRecord(zeSchema, "zeUltimateRecord");
		record.set(zeSchema.title(), "aValue");
		record.set(zeSchema.booleanMetadata(), Arrays.asList(true, false, true));
		recordServices.add(record);
		long initialVersion = record.getVersion();

		record.set(zeSchema.title(), "anOtherValue");
		recordServices.update(record);

		assertThat(record.<String>get(zeSchema.title())).isEqualTo("anOtherValue");
		assertThat(record.<List<Boolean>>get(zeSchema.booleanMetadata())).isEqualTo(Arrays.asList(true, false, true));
		assertThat(record.getVersion()).isNotEqualTo(initialVersion);
	}

	@Test
	public void givenRecordWithDateFieldWhenAddUpdateThenOk()
			throws Exception {

		defineSchemasManager().using(schemas.withADateMetadata());

		Record record = new TestRecord(zeSchema, "zeUltimateRecord");
		record.set(zeSchema.dateMetadata(), shishDay);
		recordServices.add(record);
		assertThat(recordServices.getDocumentById(record.getId()).<LocalDate>get(zeSchema.dateMetadata())).isEqualTo(shishDay);

		record.set(zeSchema.dateMetadata(), tockDay);
		recordServices.update(record);
		assertThat(recordServices.getDocumentById(record.getId()).<LocalDate>get(zeSchema.dateMetadata())).isEqualTo(tockDay);
	}

	@Test
	public void givenAddedRecordWhenModifyingMultivalueAndUpdatingThenModificationsSavedAndVersionChanged()
			throws Exception {
		defineSchemasManager().using(schemas.withATitle().withABooleanMetadata(whichIsMultivalue));

		Record record = new TestRecord(zeSchema, "zeUltimateRecord");
		record.set(zeSchema.title(), "aValue");
		record.set(zeSchema.booleanMetadata(), Arrays.asList(true, false, true));
		recordServices.add(record);
		long initialVersion = record.getVersion();

		record.set(zeSchema.booleanMetadata(), Arrays.asList(false, true, false));
		recordServices.update(record);

		assertThat(record.<String>get(zeSchema.title())).isEqualTo("aValue");
		assertThat(record.<List<Boolean>>get(zeSchema.booleanMetadata())).isEqualTo(Arrays.asList(false, true, false));
		assertThat(record.getVersion()).isNotEqualTo(initialVersion);
	}

	@Test
	public void givenAddedRecordWhenUpdatingNonModifiedRecordThenVersionUnchanged()
			throws Exception {
		defineSchemasManager().using(schemas.withATitle().withABooleanMetadata(whichIsMultivalue));

		Record record = new TestRecord(zeSchema, "zeUltimateRecord");
		record.set(zeSchema.title(), "aValue");
		record.set(zeSchema.booleanMetadata(), Arrays.asList(true, false, true));
		recordServices.add(record);
		long initialVersion = record.getVersion();

		recordServices.update(record);

		assertThat(record.<String>get(zeSchema.title())).isEqualTo("aValue");
		assertThat(record.<List<Boolean>>get(zeSchema.booleanMetadata())).isEqualTo(Arrays.asList(true, false, true));
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

		Record record = new TestRecord(zeSchema, "zeUltimateRecord");
		recordServices.add(record
				.set(zeSchema.title(), "neverEncryptedValue")
				.set(zeSchema.stringMetadata(), "decryptedValue1")
				.set(zeSchema.anotherStringMetadata(), asList("decryptedValue2", "decryptedValue3")));

		assertThat(record.<String>get(zeSchema.title())).isEqualTo("neverEncryptedValue");
		assertThat(record.<String>get(zeSchema.stringMetadata())).isEqualTo("decryptedValue1");
		assertThat(record.<List<String>>get(zeSchema.anotherStringMetadata())).isEqualTo(asList("decryptedValue2", "decryptedValue3"));

		record = recordServices.getDocumentById(record.getId());
		assertThat(record.<String>get(zeSchema.title())).isEqualTo("neverEncryptedValue");
		assertThat(record.<String>get(zeSchema.stringMetadata())).isEqualTo("decryptedValue1");
		assertThat(record.<List<String>>get(zeSchema.anotherStringMetadata())).isEqualTo(asList("decryptedValue2", "decryptedValue3"));

		RecordDTO recordDTO = recordDao.get(record.getId());
		assertThat(recordDTO.getFields().get(zeSchema.title().getDataStoreCode())).isEqualTo("neverEncryptedValue");
		assertThat(recordDTO.getFields().get(zeSchema.stringMetadata().getDataStoreCode())).isEqualTo("AN1Qletvk4b6cysfpDjWUg==");
		assertThat(recordDTO.getFields().get(zeSchema.anotherStringMetadata().getDataStoreCode()))
				.isEqualTo(asList("2xz/K3dNfajma8DJQVMBnQ==", "0d6Amw6w/rOUYwTrjNK4LQ=="));

		record.set(zeSchema.stringMetadata(), "decryptedValue2")
				.set(zeSchema.anotherStringMetadata(), asList("decryptedValue3", "decryptedValue4"));
		assertThat(record.<String>get(zeSchema.title())).isEqualTo("neverEncryptedValue");
		assertThat(record.<String>get(zeSchema.stringMetadata())).isEqualTo("decryptedValue2");
		assertThat(record.<List<String>>get(zeSchema.anotherStringMetadata())).isEqualTo(asList("decryptedValue3", "decryptedValue4"));

		recordServices.update(record);

		assertThat(record.<String>get(zeSchema.title())).isEqualTo("neverEncryptedValue");
		assertThat(record.<String>get(zeSchema.stringMetadata())).isEqualTo("decryptedValue2");
		assertThat(record.<List<String>>get(zeSchema.anotherStringMetadata())).isEqualTo(asList("decryptedValue3", "decryptedValue4"));
		record = recordServices.getDocumentById(record.getId());
		assertThat(record.<String>get(zeSchema.title())).isEqualTo("neverEncryptedValue");
		assertThat(record.<String>get(zeSchema.stringMetadata())).isEqualTo("decryptedValue2");
		assertThat(record.<List<String>>get(zeSchema.anotherStringMetadata())).isEqualTo(asList("decryptedValue3", "decryptedValue4"));

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
		recordServices = spy(recordServices);

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

		assertThat(anotherSchemaRecord.<String>get(anotherSchema.metadataWithCopiedEntry())).isEqualTo("a");
		assertThat(thirdSchemaRecord.<String>get(thirdSchema.metadataWithCopiedEntry())).isEqualTo("a");

		recordServices.refresh(asList(anotherSchemaRecord, thirdSchemaRecord));

		assertThat(anotherSchemaRecord.<String>get(anotherSchema.metadataWithCopiedEntry())).isEqualTo("b");
		assertThat(thirdSchemaRecord.<String>get(thirdSchema.metadataWithCopiedEntry())).isEqualTo("b");
	}

	@Test
	public void givenModificationImpactWhenExecutingTransactionThenHandledInSameTransaction()
			throws Exception {
		defineSchemasManager().using(schemas.withAMetadataCopiedInAnotherSchema());
		recordServices = spy(recordServices);
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

		verify(recordServices, times(1)).execute(transaction);
		verify(recordServices, times(3)).execute(savedTransaction.capture(), anyInt());
		Transaction firstNestedTransaction = savedTransaction.getAllValues().get(1);
		Transaction secondNestedTransaction = savedTransaction.getAllValues().get(2);

		assertThat(firstNestedTransaction).isNotSameAs(transaction);
		assertThat(firstNestedTransaction.getRecords()).hasSize(2);
		assertThat(firstNestedTransaction.getRecords().get(0)).isEqualTo(zeSchemaRecord);
		assertThat(firstNestedTransaction.getRecords().get(1).getId()).isEqualTo(anotherSchemaRecord.getId());
		assertThat(firstNestedTransaction.getRecords().get(1).<String>get(anotherSchema.metadataWithCopiedEntry())).isEqualTo("b");

		assertThat(secondNestedTransaction).isNotSameAs(transaction);
		assertThat(secondNestedTransaction.getRecords()).hasSize(3);
		assertThat(secondNestedTransaction.getRecords().get(0)).isEqualTo(zeSchemaRecord);
		assertThat(secondNestedTransaction.getRecords().get(1).getId()).isEqualTo(anotherSchemaRecord.getId());
		assertThat(secondNestedTransaction.getRecords().get(1).<String>get(anotherSchema.metadataWithCopiedEntry())).isEqualTo("b");
		assertThat(secondNestedTransaction.getRecords().get(2).getId()).isEqualTo(thirdSchemaRecord.getId());
		assertThat(secondNestedTransaction.getRecords().get(2).<String>get(anotherSchema.metadataWithCopiedEntry())).isEqualTo("b");

		recordServices.refresh(asList(anotherSchemaRecord, thirdSchemaRecord));
		assertThat(anotherSchemaRecord.<String>get(anotherSchema.metadataWithCopiedEntry())).isEqualTo("b");
		assertThat(thirdSchemaRecord.<String>get(thirdSchema.metadataWithCopiedEntry())).isEqualTo("b");
	}

	@Test
	public void givenUpdatingMultipleRecordsInTransactionThenHandleThemInCorrectOrderReducingChancesOfModificationImpact()
			throws Exception {
		defineSchemasManager().using(schemas.withAMetadataCopiedInAnotherSchema());
		recordServices = spy(recordServices);

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

		assertThat(anotherSchemaRecord.<String>get(anotherSchema.metadataWithCopiedEntry())).isEqualTo("b");
		assertThat(thirdSchemaRecord.<String>get(thirdSchema.metadataWithCopiedEntry())).isEqualTo("b");
		assertThat(anotherThirdSchemaRecord.<String>get(thirdSchema.metadataWithCopiedEntry())).isEqualTo("b");

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

		assertThat(anotherSchemaRecord.<String>get(anotherSchema.metadataWithCopiedEntry())).isEqualTo("1.1");

		Transaction initialTransaction = new Transaction();
		initialTransaction.addUpdate(anotherSchemaRecord.set(anotherSchema.referenceToZeSchema(), zeSchemaRecord2.getId()));
		initialTransaction.addUpdate(zeSchemaRecord1.set(zeSchema.getCopiedMeta(), "1.2"));
		initialTransaction.addUpdate(zeSchemaRecord2.set(zeSchema.getCopiedMeta(), "2.2"));
		recordServices.execute(initialTransaction);

		assertThat(anotherSchemaRecord.<String>get(anotherSchema.metadataWithCopiedEntry())).isEqualTo("2.2");
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

		assertThat(anotherSchemaRecord.<String>get(anotherSchema.metadataWithCopiedEntry())).isEqualTo("1.1");

		Transaction initialTransaction = new Transaction();
		initialTransaction.addUpdate(zeSchemaRecord1.set(zeSchema.getCopiedMeta(), "1.2"));
		initialTransaction.addUpdate(zeSchemaRecord2.set(zeSchema.getCopiedMeta(), "2.2"));
		initialTransaction.addUpdate(anotherSchemaRecord.set(anotherSchema.referenceToZeSchema(), zeSchemaRecord2.getId()));
		recordServices.execute(initialTransaction);

		assertThat(anotherSchemaRecord.<String>get(anotherSchema.metadataWithCopiedEntry())).isEqualTo("2.2");
	}

	@Test
	public void given2RecordsIn2DifferentSchemaWithTheSecondCalculatedFromTheFirstWhenExecutingAsyncTransactionThenAddedInCorrectOrder()
			throws Exception {
		defineSchemasManager().using(schemas.withAMetadataCopiedInAnotherSchema());
		cacheIntegrityCheckedAfterTest = false;


		Record zeSchemaRecord1 = recordServices.newRecordWithSchema(zeSchema.instance());
		zeSchemaRecord1.set(zeSchema.getCopiedMeta(), "1.1");
		recordServices.add(zeSchemaRecord1);

		Record zeSchemaRecord2 = recordServices.newRecordWithSchema(zeSchema.instance());
		zeSchemaRecord2.set(zeSchema.getCopiedMeta(), "2.1");
		recordServices.add(zeSchemaRecord2);

		Record anotherSchemaRecord = new TestRecord(anotherSchema);
		anotherSchemaRecord.set(anotherSchema.referenceToZeSchema(), zeSchemaRecord1.getId());
		recordServices.add(anotherSchemaRecord);

		assertThat(anotherSchemaRecord.<String>get(anotherSchema.metadataWithCopiedEntry())).isEqualTo("1.1");

		Transaction initialTransaction = new Transaction();
		initialTransaction.addUpdate(anotherSchemaRecord.set(anotherSchema.referenceToZeSchema(), zeSchemaRecord2.getId()));
		initialTransaction.addUpdate(zeSchemaRecord1.set(zeSchema.getCopiedMeta(), "1.2"));
		initialTransaction.addUpdate(zeSchemaRecord2.set(zeSchema.getCopiedMeta(), "2.2"));
		recordServices.executeHandlingImpactsAsync(initialTransaction);

		assertThat(anotherSchemaRecord.<String>get(anotherSchema.metadataWithCopiedEntry())).isEqualTo("2.2");
	}

	@Test
	public void givenUnmodifiableMetadataThenCanSetValueButCannotUpdateIt()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(whichIsUnmodifiable));
		RecordImpl record = saveZeSchemaRecordAndReload();
		assertThat(record.<String>get(zeSchema.stringMetadata())).isNull();

		record = updateAndReload(record.set(zeSchema.stringMetadata(), "ze value"));
		assertThat(record.<String>get(zeSchema.stringMetadata())).isEqualTo("ze value");

		record = updateAndReload(record.set(zeSchema.stringMetadata(), "ze value"));
		assertThat(record.<String>get(zeSchema.stringMetadata())).isEqualTo("ze value");

		try {
			recordServices.update(record.set(zeSchema.stringMetadata(), "another value"));
			fail("ValidationException expected");
		} catch (ValidationException e) {
			Map<String, Object> parameters = asMap(METADATA_CODE, "zeSchemaType_default_stringMetadata");
			parameters.put(Validator.METADATA_LABEL, TestUtils.asMap("fr", "A toAString metadata"));
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
			parameters.put(Validator.METADATA_LABEL, TestUtils.asMap("fr", "A toAString metadata"));
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
		recordServices = spy(recordServices);
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
				newTransactionWithNRecords(100000).setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION));
	}

	@Test(expected = RecordServicesRuntimeException_TransactionHasMoreThan100000Records.class)
	public void whenExecutingWithMoreThan10001RecordsAndThrowExceptionOptimisticLockingResolutionThenException()
			throws Exception {
		defineSchemasManager().using(schemas.withATitle().withAStringMetadata());
		recordServices.execute(
				newTransactionWithNRecords(100001).setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION));
	}

	@SlowTest
	@Test
	public void whenExecutingASyncWithMoreThan1000RecordsAndMergeOptimisticLockingResolutionThenOk()
			throws Exception {
		defineSchemasManager().using(schemas.withATitle().withAStringMetadata());
		recordServices = spy(recordServices);
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
				newTransactionWithNRecords(100000).setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION));
	}

	@Test(expected = RecordServicesRuntimeException_TransactionHasMoreThan100000Records.class)
	public void whenExecutingASyncWithMoreThan10001RecordsAndThrowExceptionOptimisticLockingResolutionThenException()
			throws Exception {
		defineSchemasManager().using(schemas.withATitle().withAStringMetadata());
		recordServices.executeHandlingImpactsAsync(
				newTransactionWithNRecords(100001).setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION));
	}

	@Test
	public void whenUpdateARecordAReferenceToANewRecord()
			throws Exception {
		defineSchemasManager().using(schemas.withAParentReferenceFromAnotherSchemaToZeSchema());

		Record anotherSchemaRecord = new TestRecord(anotherSchema).set(TITLE, "New record saved in transaction 1");
		recordServices.add(anotherSchemaRecord);

		Record zeSchemaRecord = new TestRecord(zeSchema).set(TITLE, "New record saved in transaction 2");
		anotherSchemaRecord.set(anotherSchema.metadata("referenceFromAnotherSchemaToZeSchema"), zeSchemaRecord.getId());

		recordServices.execute(new Transaction(anotherSchemaRecord, zeSchemaRecord));

	}

	@Test
	public void whenUpdateARecordAReferenceListToANewRecord()
			throws Exception {
		defineSchemasManager().using(schemas.with(metadataFromAnotherSchemaToZeSchema()));

		Record anotherSchemaRecord = new TestRecord(anotherSchema).set(TITLE, "New record saved in transaction 1");
		recordServices.add(anotherSchemaRecord);

		Record zeSchemaRecord = new TestRecord(zeSchema).set(TITLE, "New record saved in transaction 2");
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

		Record anotherSchemaRecord = new TestRecord(anotherSchema).set(TITLE, "New record saved in transaction 1");
		recordServices.add(anotherSchemaRecord);

		Record zeSchemaRecord = new TestRecord(zeSchema).set(TITLE, "New record saved in transaction 2");
		anotherSchemaRecord.set(anotherSchema.metadata("aStringMetadata"), zeSchemaRecord.getId());

		recordServices.execute(new Transaction(anotherSchemaRecord, zeSchemaRecord));

	}

	@Test
	public void whenExecutingTransactionWithRecordsMarkedForReindexingThenMarkedForReindexing()
			throws Exception {

		defineSchemasManager().using(schemas);

		Record record1 = new TestRecord(zeSchema).set(TITLE, "record1");
		Record record2 = new TestRecord(zeSchema).set(TITLE, "record2");
		Record record3 = new TestRecord(zeSchema).set(TITLE, "record3");
		Record record4 = new TestRecord(zeSchema).set(TITLE, "record4");
		Record record5 = new TestRecord(zeSchema).set(TITLE, "record5");
		recordServices.execute(new Transaction(record1, record2, record3, record4, record5));

		Record record6 = new TestRecord(zeSchema).set(TITLE, "record6");
		Transaction transaction = new Transaction();
		transaction.add(record1.set(TITLE, "newTitleOfRecord1"));
		transaction.add(record2.set(TITLE, "newTitleOfRecord2"));
		transaction.add(record6);
		transaction.addRecordToReindex(record2);
		transaction.addRecordToReindex(record5);
		transaction.addRecordToReindex(record4);
		transaction.addRecordToReindex(record6);

		recordServices.execute(transaction);

		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		assertThatRecords(searchServices.search(query(from(zeSchema.instance()).returnAll())))
				.extractingMetadatas(TITLE, MARKED_FOR_REINDEXING).containsOnly(
				tuple("newTitleOfRecord1", null),
				tuple("newTitleOfRecord2", true),
				tuple("record3", null),
				tuple("record4", true),
				tuple("record5", true),
				tuple("record6", true)
		);

	}

	@Test
	public void givenRecordsMarkedForReindexingWhenUpdateThemInTransactionThenUnflaggedEvenIfNoChange()
			throws Exception {

		defineSchemasManager().using(schemas);
		SearchServices searchServices = getModelLayerFactory().newSearchServices();

		Record record1 = new TestRecord(zeSchema, "r1").set(TITLE, "record1");
		Record record2 = new TestRecord(zeSchema, "r2").set(TITLE, "record2");
		Record record3 = new TestRecord(zeSchema, "r3").set(TITLE, "record3");
		Record record4 = new TestRecord(zeSchema, "r4").set(TITLE, "record4");
		Record record5 = new TestRecord(zeSchema, "r5").set(TITLE, "record5");
		recordServices.execute(new Transaction(record1, record2, record3, record4, record5));

		assertThatRecords(searchServices.search(query(from(zeSchema.instance()).returnAll())))
				.extractingMetadatas(TITLE, MARKED_FOR_REINDEXING).containsOnly(
				tuple("record1", null),
				tuple("record2", null),
				tuple("record3", null),
				tuple("record4", null),
				tuple("record5", null)
		);

		Transaction transaction = new Transaction();
		transaction.add(record1.set(TITLE, "newTitleOfRecord1"));
		transaction.addRecordToReindex(record2);
		transaction.addRecordToReindex(record3);
		transaction.addRecordToReindex(record4);
		transaction.addRecordToReindex(record5);
		recordServices.execute(transaction);

		assertThatRecords(searchServices.search(query(from(zeSchema.instance()).returnAll())))
				.extractingMetadatas(TITLE, MARKED_FOR_REINDEXING).containsOnly(
				tuple("newTitleOfRecord1", null),
				tuple("record2", true),
				tuple("record3", true),
				tuple("record4", true),
				tuple("record5", true)
		);

		recordServices.refresh(record2, record3, record4);

		transaction = new Transaction();
		transaction.add(record2);
		transaction.add(record3);
		transaction.update(record4);
		recordServices.execute(transaction);

		assertThatRecords(searchServices.search(query(from(zeSchema.instance()).returnAll())))
				.extractingMetadatas(TITLE, MARKED_FOR_REINDEXING).containsOnly(
				tuple("newTitleOfRecord1", null),
				tuple("record2", true),
				tuple("record3", true),
				tuple("record4", true),
				tuple("record5", true)
		);

		transaction = new Transaction();
		transaction.getRecordUpdateOptions().setForcedReindexationOfMetadatas(TransactionRecordsReindexation.ALL());
		transaction.add(record2);
		transaction.add(record3);
		transaction.update(record4);
		recordServices.execute(transaction);

		assertThatRecords(searchServices.search(query(from(zeSchema.instance()).returnAll())))
				.extractingMetadatas(TITLE, MARKED_FOR_REINDEXING).containsOnly(
				tuple("newTitleOfRecord1", null),
				tuple("record2", true),
				tuple("record3", true),
				tuple("record4", true),
				tuple("record5", true)
		);
	}

	@Test
	@IntermittentFailureTest
	public void whenMarkAnInexistentRecordForReindexingThenException()
			throws Exception {

		defineSchemasManager().using(schemas);

		Record record1 = new TestRecord(zeSchema).set(TITLE, "record1");
		Record record2 = new TestRecord(zeSchema).set(TITLE, "record2");
		Record record3 = new TestRecord(zeSchema).set(TITLE, "record3");
		Record record4 = new TestRecord(zeSchema).set(TITLE, "record4");
		Record record5 = new TestRecord(zeSchema).set(TITLE, "record5");
		recordServices.execute(new Transaction(record1, record2, record3, record4, record5));

		Record record6 = new TestRecord(zeSchema).set(TITLE, "record6");
		Transaction transaction = new Transaction();
		transaction.addRecordToReindex(record2);
		transaction.addRecordToReindex(record3);
		transaction.addRecordToReindex(record4);
		transaction.add(record6);
		transaction.addRecordToReindex("record7");

		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		assertThatRecords(searchServices.search(query(from(zeSchema.instance()).returnAll())))
				.extractingMetadatas(TITLE, MARKED_FOR_REINDEXING).containsOnly(
				tuple("record1", null),
				tuple("record2", null),
				tuple("record3", null),
				tuple("record4", null),
				tuple("record5", null)
		);

		try {
			recordServices.execute(transaction);
			fail("Exception expected");
		} catch (RecordServicesException.UnresolvableOptimisticLockingConflict e) {
			//OK
		}

		recordServices.flush();
		assertThatRecords(searchServices.search(query(from(zeSchema.instance()).returnAll())))
				.extractingMetadatas(TITLE, MARKED_FOR_REINDEXING).containsOnly(
				tuple("record1", null),
				tuple("record2", null),
				tuple("record3", null),
				tuple("record4", null),
				tuple("record5", null)
		);

	}

	@Test(expected = CannotSetIdsToReindexInEmptyTransaction.class)
	public void givenEmptyTransactionWithOnlyMarkedToReindexThenException()
			throws Exception {

		defineSchemasManager().using(schemas);

		Record record1 = new TestRecord(zeSchema).set(TITLE, "record1");
		Record record2 = new TestRecord(zeSchema).set(TITLE, "record2");
		Record record3 = new TestRecord(zeSchema).set(TITLE, "record3");
		Record record4 = new TestRecord(zeSchema).set(TITLE, "record4");
		Record record5 = new TestRecord(zeSchema).set(TITLE, "record5");
		recordServices.execute(new Transaction(record1, record2, record3, record4, record5));

		Record record6 = new TestRecord(zeSchema).set(TITLE, "record6");
		Transaction transaction = new Transaction();
		transaction.addRecordToReindex(record2);
		transaction.addRecordToReindex(record3);
		transaction.addRecordToReindex(record4);
		recordServices.execute(transaction);

	}

	@Test
	public void givenTransientLazyMetadataThenNotSavedAndRetrievedOnRecordRecalculate()
			throws Exception {

		defineSchemasManager().using(schemas.withANumberMetadata(
				whichIsCalculatedUsing(TitleLengthCalculator.class),
				whichHasTransiency(TRANSIENT_LAZY)));

		//TODO records in cache should lost transient metadatas

		//Save a record, it keeps the transient metadatas
		Record record = new TestRecord(zeSchema).set(TITLE, "Vodka Framboise");
		recordServices.add(record);
		assertThat(record.<Double>get(zeSchema.numberMetadata())).isEqualTo(15.0);

		//The record is obtained from the datastore, there is no value
		record = recordServices.getDocumentById(record.getId());
		assertThat(record.<Double>get(zeSchema.numberMetadata())).isNull();

		//The record is recalculated, the value is loaded
		recordServices.recalculate(record);
		assertThat(record.<Double>get(zeSchema.numberMetadata())).isEqualTo(15.0);

		record = new TestRecord(zeSchema).set(TITLE, "Vodka Canneberge");
		recordServices.add(record);
		assertThat(record.<Double>get(zeSchema.numberMetadata())).isEqualTo(16.0);

		record = recordServices.getDocumentById(record.getId());
		assertThat(record.<Double>get(zeSchema.numberMetadata())).isNull();

		//The record is recalculated, the value is loaded
		recordServices.recalculate(record);
		assertThat(record.<Double>get(zeSchema.numberMetadata())).isEqualTo(16.0);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeSchema.typeCode()).setRecordCacheType(RecordCacheType.FULLY_CACHED);
			}
		});

		Record recordInCache = getModelLayerFactory().getRecordsCaches().getCache(zeCollection).get(record.getId());
		assertThat(recordInCache.<Double>get(zeSchema.numberMetadata())).isNull();

	}

	@Test
	public void givenTransientEagerMetadataThenNotSavedAndRetrievedOnRecordRetrieval()
			throws Exception {

		defineSchemasManager().using(schemas.withANumberMetadata(
				whichIsCalculatedUsing(TitleLengthCalculator.class),
				whichHasTransiency(TRANSIENT_EAGER)));

		//TODO records in cache should lost transient metadatas

		//Save a record, it keeps the transient metadatas
		Record record = new TestRecord(zeSchema).set(TITLE, "Vodka Framboise");
		recordServices.add(record);
		assertThat(record.<Double>get(zeSchema.numberMetadata())).isEqualTo(15.0);

		//The record is obtained from the datastore, there is no value
		record = recordServices.getDocumentById(record.getId());
		assertThat(record.<Double>get(zeSchema.numberMetadata())).isEqualTo(15.0);

		record = new TestRecord(zeSchema).set(TITLE, "Vodka Canneberge");
		recordServices.add(record);
		assertThat(record.<Double>get(zeSchema.numberMetadata())).isEqualTo(16.0);

		record = recordServices.getDocumentById(record.getId());
		assertThat(record.<Double>get(zeSchema.numberMetadata())).isEqualTo(16.0);

		Record recordInCache = getModelLayerFactory().getRecordsCaches().getCache(zeCollection).get(record.getId());
		assertThat(recordInCache.<Double>get(zeSchema.numberMetadata())).isEqualTo(16.0);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeSchema.typeCode()).setRecordCacheType(RecordCacheType.FULLY_CACHED);
			}
		});

		recordInCache = getModelLayerFactory().getRecordsCaches().getCache(zeCollection).get(record.getId());
		assertThat(recordInCache.<Double>get(zeSchema.numberMetadata())).isEqualTo(16.0);

	}

	@Test
	public void givenExceptionThrownByRecordInCreationBeforeValidationAndAutomaticValuesCalculationExtensionThenCatchedDependingOnTransactionOption()
			throws Exception {

		defineSchemasManager().using(schemas);
		getModelLayerFactory().getExtensions().forCollection(zeCollection).recordExtensions.add(new RecordExtension() {
			@Override
			public void recordInCreationBeforeValidationAndAutomaticValuesCalculation(
					RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent event) {
				throw new RuntimeException("oh bobo!");
			}
		});

		Record record = new TestRecord(zeSchema).set(TITLE, "Vodka Framboise");
		Transaction transaction = new Transaction(record);
		try {
			recordServices.execute(transaction);
			fail("Exception expected");
		} catch (Exception e) {
			//OK
		}

		transaction.getRecordUpdateOptions().setCatchExtensionsValidationsErrors(true);
		try {
			recordServices.execute(transaction);
			fail("Exception expected");
		} catch (Exception e) {
			//OK
		}

		transaction.getRecordUpdateOptions().setCatchExtensionsValidationsErrors(false).setCatchExtensionsExceptions(true);

		transaction.getRecordUpdateOptions().setCatchExtensionsExceptions(true);
		recordServices.execute(transaction);

		assertThatRecord(record).exists();

	}

	@Test
	public void givenExceptionThrownByRecordInCreationBeforeSaveExtensionThenCatchedDependingOnTransactionOption()
			throws Exception {

		defineSchemasManager().using(schemas);
		getModelLayerFactory().getExtensions().forCollection(zeCollection).recordExtensions.add(new RecordExtension() {

			@Override
			public void recordInCreationBeforeSave(RecordInCreationBeforeSaveEvent event) {
				throw new RuntimeException("oh bobo!");
			}
		});

		Record record = new TestRecord(zeSchema).set(TITLE, "Vodka Framboise");
		Transaction transaction = new Transaction(record);
		try {
			recordServices.execute(transaction);
			fail("Exception expected");
		} catch (Exception e) {
			//OK
		}

		transaction.getRecordUpdateOptions().setCatchExtensionsValidationsErrors(true);
		try {
			recordServices.execute(transaction);
			fail("Exception expected");
		} catch (Exception e) {
			//OK
		}

		transaction.getRecordUpdateOptions().setCatchExtensionsValidationsErrors(false).setCatchExtensionsExceptions(true);

		transaction.getRecordUpdateOptions().setCatchExtensionsExceptions(true);
		recordServices.execute(transaction);

		assertThatRecord(record).exists();

	}

	@Test
	public void givenExceptionThrownByRecordCreatedExtensionThenCatchedDependingOnTransactionOption()
			throws Exception {

		defineSchemasManager().using(schemas);
		getModelLayerFactory().getExtensions().forCollection(zeCollection).recordExtensions.add(new RecordExtension() {

			@Override
			public void recordCreated(RecordCreationEvent event) {
				throw new RuntimeException("oh bobo!");
			}
		});

		Record record = new TestRecord(zeSchema).set(TITLE, "Vodka Framboise");
		Transaction transaction = new Transaction(record);
		try {
			recordServices.execute(transaction);
			fail("Exception expected");
		} catch (Exception e) {
			//OK
		}

		record = new TestRecord(zeSchema).set(TITLE, "Édouard Lechat");
		transaction = new Transaction(record);
		transaction.getRecordUpdateOptions().setCatchExtensionsValidationsErrors(true);
		try {
			recordServices.execute(transaction);
			fail("Exception expected");
		} catch (Exception e) {
			//OK
		}

		record = new TestRecord(zeSchema).set(TITLE, "Félix");
		transaction = new Transaction(record);
		transaction.getRecordUpdateOptions().setCatchExtensionsValidationsErrors(false).setCatchExtensionsExceptions(true);

		transaction.getRecordUpdateOptions().setCatchExtensionsExceptions(true);
		recordServices.execute(transaction);

		assertThatRecord(record).exists();

	}

	@Test
	public void givenValidationExceptionThrownByRecordInCreationBeforeSaveExtensionThenCatchedDependingOnTransactionOption()
			throws Exception {

		defineSchemasManager().using(schemas);
		getModelLayerFactory().getExtensions().forCollection(zeCollection).recordExtensions.add(new RecordExtension() {

			@Override
			public void recordInCreationBeforeSave(RecordInCreationBeforeSaveEvent event) {
				event.getValidationErrors().add(RecordServicesAcceptanceTest.class, "Ze validation error");
			}
		});

		Record record = new TestRecord(zeSchema).set(TITLE, "Vodka Framboise");
		Transaction transaction = new Transaction(record);
		try {
			recordServices.execute(transaction);
			fail("Exception expected");
		} catch (Exception e) {
			//OK
		}

		transaction.getRecordUpdateOptions().setCatchExtensionsExceptions(true);
		try {
			recordServices.execute(transaction);
			fail("Exception expected");
		} catch (Exception e) {
			//OK
		}

		transaction.getRecordUpdateOptions().setCatchExtensionsValidationsErrors(true).setCatchExtensionsExceptions(false);
		recordServices.execute(transaction);

		assertThatRecord(record).exists();

	}

	@Test
	public void givenExceptionThrownByRecordInModificationBeforeValidationAndAutomaticValuesCalculationExtensionThenCatchedDependingOnTransactionOption()
			throws Exception {

		defineSchemasManager().using(schemas);
		getModelLayerFactory().getExtensions().forCollection(zeCollection).recordExtensions.add(new RecordExtension() {
			@Override
			public void recordInModificationBeforeValidationAndAutomaticValuesCalculation(
					RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent event) {
				throw new RuntimeException("oh bobo!");
			}
		});

		Record record = new TestRecord(zeSchema).set(TITLE, "Vodka Framboise");
		recordServices.add(record);
		record.set(Schemas.TITLE, "Édouard Lechat");

		Transaction transaction = new Transaction(record);
		try {
			recordServices.execute(transaction);
			fail("Exception expected");
		} catch (Exception e) {
			//OK
		}

		transaction.getRecordUpdateOptions().setCatchExtensionsValidationsErrors(true);
		try {
			recordServices.execute(transaction);
			fail("Exception expected");
		} catch (Exception e) {
			//OK
		}

		transaction.getRecordUpdateOptions().setCatchExtensionsValidationsErrors(false).setCatchExtensionsExceptions(true);

		transaction.getRecordUpdateOptions().setCatchExtensionsExceptions(true);
		recordServices.execute(transaction);

		assertThatRecord(record).exists();

	}

	@Test
	public void givenExceptionThrownByRecordInModificationBeforeSaveExtensionThenCatchedDependingOnTransactionOption()
			throws Exception {

		defineSchemasManager().using(schemas);
		getModelLayerFactory().getExtensions().forCollection(zeCollection).recordExtensions.add(new RecordExtension() {

			@Override
			public void recordInModificationBeforeSave(RecordInModificationBeforeSaveEvent event) {
				throw new RuntimeException("oh bobo!");
			}
		});

		Record record = new TestRecord(zeSchema).set(TITLE, "Vodka Framboise");
		recordServices.add(record);
		record.set(Schemas.TITLE, "Édouard Lechat");
		Transaction transaction = new Transaction(record);
		try {
			recordServices.execute(transaction);
			fail("Exception expected");
		} catch (Exception e) {
			//OK
		}

		transaction.getRecordUpdateOptions().setCatchExtensionsValidationsErrors(true);
		try {
			recordServices.execute(transaction);
			fail("Exception expected");
		} catch (Exception e) {
			//OK
		}

		transaction.getRecordUpdateOptions().setCatchExtensionsValidationsErrors(false).setCatchExtensionsExceptions(true);

		transaction.getRecordUpdateOptions().setCatchExtensionsExceptions(true);
		recordServices.execute(transaction);

		assertThatRecord(record).exists();

	}

	@Test
	public void givenExceptionThrownByRecordModifiedExtensionThenCatchedDependingOnTransactionOption()
			throws Exception {

		defineSchemasManager().using(schemas);
		getModelLayerFactory().getExtensions().forCollection(zeCollection).recordExtensions.add(new RecordExtension() {

			@Override
			public void recordModified(RecordModificationEvent event) {
				throw new RuntimeException("oh bobo!");
			}
		});

		Record record = new TestRecord(zeSchema).set(TITLE, "Un chat");
		recordServices.add(record);
		record.set(Schemas.TITLE, "Vodka Framboise");

		Transaction transaction = new Transaction(record);
		try {
			recordServices.execute(transaction);
			fail("Exception expected");
		} catch (Exception e) {
			//OK
		}

		record = new TestRecord(zeSchema).set(TITLE, "Un chat");
		recordServices.add(record);
		record.set(Schemas.TITLE, "Édouard Lechat");
		transaction = new Transaction(record);
		transaction.getRecordUpdateOptions().setCatchExtensionsValidationsErrors(true);
		try {
			recordServices.execute(transaction);
			fail("Exception expected");
		} catch (Exception e) {
			//OK
		}

		record = new TestRecord(zeSchema).set(TITLE, "Un chat");
		recordServices.add(record);
		record.set(Schemas.TITLE, "Félix");
		transaction = new Transaction(record);
		transaction.getRecordUpdateOptions().setCatchExtensionsValidationsErrors(false).setCatchExtensionsExceptions(true);

		transaction.getRecordUpdateOptions().setCatchExtensionsExceptions(true);
		recordServices.execute(transaction);

		assertThatRecord(record).exists();

	}

	@Test
	public void givenExceptionThrownByTransactionExecutionBeforeSaveExtensionThenCatchedDependingOnTransactionOption()
			throws Exception {

		defineSchemasManager().using(schemas);
		getModelLayerFactory().getExtensions().forCollection(zeCollection).recordExtensions.add(new RecordExtension() {

			@Override
			public void transactionExecutionBeforeSave(TransactionExecutionBeforeSaveEvent event) {
				throw new RuntimeException("oh bobo!");
			}
		});

		Record record = new TestRecord(zeSchema).set(TITLE, "Vodka Framboise");
		Transaction transaction = new Transaction(record);
		try {
			recordServices.execute(transaction);
			fail("Exception expected");
		} catch (Exception e) {
			//OK
		}

		transaction.getRecordUpdateOptions().setCatchExtensionsValidationsErrors(true);
		try {
			recordServices.execute(transaction);
			fail("Exception expected");
		} catch (Exception e) {
			//OK
		}

		transaction.getRecordUpdateOptions().setCatchExtensionsValidationsErrors(false).setCatchExtensionsExceptions(true);
		recordServices.execute(transaction);

		assertThatRecord(record).exists();

	}

	@Test
	public void givenValidationExceptionThrownByRecordInModificationBeforeSaveExtensionThenCatchedDependingOnTransactionOption()
			throws Exception {

		defineSchemasManager().using(schemas);
		getModelLayerFactory().getExtensions().forCollection(zeCollection).recordExtensions.add(new RecordExtension() {

			@Override
			public void recordInModificationBeforeSave(RecordInModificationBeforeSaveEvent event) {
				event.getValidationErrors().add(RecordServicesAcceptanceTest.class, "Ze validation error");
			}
		});

		Record record = new TestRecord(zeSchema).set(TITLE, "Vodka Framboise");
		recordServices.add(record);
		record.set(Schemas.TITLE, "Édouard Lechat");
		Transaction transaction = new Transaction(record);
		try {
			recordServices.execute(transaction);
			fail("Exception expected");
		} catch (Exception e) {
			//OK
		}

		transaction.getRecordUpdateOptions().setCatchExtensionsExceptions(true);
		try {
			recordServices.execute(transaction);
			fail("Exception expected");
		} catch (Exception e) {
			//OK
		}

		transaction.getRecordUpdateOptions().setCatchExtensionsValidationsErrors(true).setCatchExtensionsExceptions(false);
		recordServices.execute(transaction);

		assertThatRecord(record).exists();

	}

	@Test
	public void givenValidationExceptionThrownByTransactionExecutionBeforeSafeExtensionThenCatchedDependingOnTransactionOption()
			throws Exception {

		defineSchemasManager().using(schemas);
		getModelLayerFactory().getExtensions().forCollection(zeCollection).recordExtensions.add(new RecordExtension() {

			@Override
			public void transactionExecutionBeforeSave(TransactionExecutionBeforeSaveEvent event) {
				event.getValidationErrors().add(RecordServicesAcceptanceTest.class, "Ze validation error");
			}
		});

		Record record = new TestRecord(zeSchema).set(TITLE, "Vodka Framboise");
		Transaction transaction = new Transaction(record);
		try {
			recordServices.execute(transaction);
			fail("Exception expected");
		} catch (Exception e) {
			//OK
		}

		transaction.getRecordUpdateOptions().setCatchExtensionsExceptions(true);
		try {
			recordServices.execute(transaction);
			fail("Exception expected");
		} catch (Exception e) {
			//OK
		}

		transaction.getRecordUpdateOptions().setCatchExtensionsValidationsErrors(true).setCatchExtensionsExceptions(false);
		recordServices.execute(transaction);

		assertThatRecord(record).exists();

	}

	@Test
	public void givenBrokenReferencesThenCatchedDependingOnTransactionOption()
			throws Exception {

		defineSchemasManager().using(schemas.withAReferenceFromAnotherSchemaToZeSchema()
				.withAnotherSchemaStringMetadata(whichIsScripted("#STRICT:title + referenceFromAnotherSchemaToZeSchema.title")));
		cacheIntegrityCheckedAfterTest = false;

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "ours").set(TITLE, "L'ours"));
		Record pointeur = transaction.add(new TestRecord(anotherSchema, "pointeur").set(TITLE, "Pointeur d'")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), "ours"));
		recordServices.execute(transaction);

		assertThatRecord(pointeur).extracting("title", "stringMetadata").isEqualTo(asList(
				"Pointeur d'", "Pointeur d'L'ours"));

		getDataLayerFactory().newRecordDao().getBigVaultServer().getNestedSolrServer().deleteById("ours");
		getDataLayerFactory().newRecordDao().getBigVaultServer().getNestedSolrServer().commit();
		recordServices.getRecordsCaches().getCache(zeCollection).invalidateVolatileReloadPermanent(asList(zeSchema.typeCode()));

		pointeur.set(Schemas.TITLE, "Pointeur d'ours brisé");
		transaction = new Transaction(pointeur);
		try {
			recordServices.execute(transaction);
			fail("Exception expected");
		} catch (RecordServicesRuntimeException.RecordServicesRuntimeException_ExceptionWhileCalculating e) {
			//OK
		}

		transaction.getRecordUpdateOptions().setCatchBrokenReferenceErrors(true);
		recordServices.execute(transaction);

		assertThatRecord(pointeur).exists();
		assertThatRecord(pointeur).extracting("title", "stringMetadata").isEqualTo(asList(
				"Pointeur d'ours brisé", null));

	}

	@Test
	public void givenBrokenReferencesInCopiedValueThenCatchedDependingOnTransactionOption()
			throws Exception {

		defineSchemasManager().using(schemas.withAReferenceFromAnotherSchemaToZeSchema()
				.withAnotherSchemaStringMetadata().with(new MetadataSchemaTypesConfigurator() {
					@Override
					public void configure(MetadataSchemaTypesBuilder schemaTypes) {
						MetadataSchemaBuilder anotherSchemaType = schemaTypes.getSchema("anotherSchemaType_default");
						MetadataBuilder zeSchemaTypeTitle = schemaTypes.getMetadata("zeSchemaType_default_title");
						anotherSchemaType.getMetadata("stringMetadata").defineDataEntry()
								.asCopied(anotherSchemaType.get("referenceFromAnotherSchemaToZeSchema"), zeSchemaTypeTitle);
					}
				}));
		cacheIntegrityCheckedAfterTest = false;

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "ours").set(TITLE, "L'ours"));
		Record pointeur = transaction.add(new TestRecord(anotherSchema, "pointeur").set(TITLE, "Pointeur d'")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), "ours"));
		recordServices.execute(transaction);
		assertThatRecord(pointeur).extracting("title", "stringMetadata").isEqualTo(asList(
				"Pointeur d'", "L'ours"));

		getDataLayerFactory().newRecordDao().getBigVaultServer().getNestedSolrServer().deleteById("ours");
		getDataLayerFactory().newRecordDao().getBigVaultServer().getNestedSolrServer().commit();
		recordServices.getRecordsCaches().getCache(zeCollection).invalidateVolatileReloadPermanent(asList(zeSchema.typeCode()));

		pointeur.set(Schemas.TITLE, "Pointeur d'ours brisé");
		transaction = new Transaction(pointeur);
		transaction.getRecordUpdateOptions().setForcedReindexationOfMetadatas(TransactionRecordsReindexation.ALL());
		try {
			recordServices.execute(transaction);
			fail("Exception expected");
		} catch (RecordServicesRuntimeException.RecordServicesRuntimeException_ExceptionWhileCalculating e) {
			//OK
		}

		transaction.getRecordUpdateOptions().setCatchBrokenReferenceErrors(true);
		recordServices.execute(transaction);

		assertThatRecord(pointeur).exists();
		assertThatRecord(pointeur).extracting("title", "stringMetadata").isEqualTo(asList(
				"Pointeur d'ours brisé", null));

	}

	@Test
	public void givenBrokenMultivalueReferencesInCopiedValueThenCatchedDependingOnTransactionOption()
			throws Exception {

		defineSchemasManager().using(schemas.withAReferenceFromAnotherSchemaToZeSchema(whichIsMultivalue)
				.withAnotherSchemaStringMetadata().with(new MetadataSchemaTypesConfigurator() {
					@Override
					public void configure(MetadataSchemaTypesBuilder schemaTypes) {
						MetadataSchemaBuilder anotherSchemaType = schemaTypes.getSchema("anotherSchemaType_default");
						MetadataBuilder zeSchemaTypeTitle = schemaTypes.getMetadata("zeSchemaType_default_title");
						anotherSchemaType.getMetadata("stringMetadata").setMultivalue(true).defineDataEntry()
								.asCopied(anotherSchemaType.get("referenceFromAnotherSchemaToZeSchema"), zeSchemaTypeTitle);
					}
				}));
		cacheIntegrityCheckedAfterTest = false;

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "ours").set(TITLE, "L'ours"));
		Record pointeur = transaction.add(new TestRecord(anotherSchema, "pointeur").set(TITLE, "Pointeur d'")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList("ours")));
		recordServices.execute(transaction);

		getDataLayerFactory().newRecordDao().getBigVaultServer().getNestedSolrServer().deleteById("ours");
		getDataLayerFactory().newRecordDao().getBigVaultServer().getNestedSolrServer().commit();
		recordServices.getRecordsCaches().getCache(zeCollection).invalidateVolatileReloadPermanent(asList(zeSchema.typeCode()));

		assertThatRecord(pointeur).extracting("title", "stringMetadata").isEqualTo(asList(
				"Pointeur d'", asList("L'ours")));

		pointeur.set(Schemas.TITLE, "Pointeur d'ours brisé");
		transaction = new Transaction(pointeur);
		transaction.getRecordUpdateOptions().setForcedReindexationOfMetadatas(TransactionRecordsReindexation.ALL());
		try {
			recordServices.execute(transaction);
			fail("Exception expected");
		} catch (RecordServicesRuntimeException.RecordServicesRuntimeException_ExceptionWhileCalculating e) {
			//OK
		}

		transaction.getRecordUpdateOptions().setCatchBrokenReferenceErrors(true);
		recordServices.execute(transaction);

		assertThatRecord(pointeur).exists();
		assertThatRecord(pointeur).extracting("title", "stringMetadata").isEqualTo(asList(
				"Pointeur d'ours brisé", new ArrayList<>()));

	}

	@Test
	public void givenBrokenMultivalueReferencesThenCatchedDependingOnTransactionOption()
			throws Exception {
		givenDisabledAfterTestValidations();
		defineSchemasManager().using(schemas.withAReferenceFromAnotherSchemaToZeSchema(whichIsMultivalue)
				.withAnotherSchemaStringMetadata(whichIsScripted("title + referenceFromAnotherSchemaToZeSchema.title")));
		cacheIntegrityCheckedAfterTest = false;

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "ours").set(TITLE, "d'ours"));
		Record pointeur = transaction.add(new TestRecord(anotherSchema, "pointeur").set(TITLE, "Pointeur ")
				.set(anotherSchema.referenceFromAnotherSchemaToZeSchema(), asList("ours")));
		recordServices.execute(transaction);
		assertThatRecord(pointeur).extracting("title", "stringMetadata").isEqualTo(asList(
				"Pointeur ", "Pointeur [d'ours]"));

		getDataLayerFactory().newRecordDao().getBigVaultServer().getNestedSolrServer().deleteById("ours");
		getDataLayerFactory().newRecordDao().getBigVaultServer().getNestedSolrServer().commit();
		recordServices.getRecordsCaches().getCache(zeCollection).invalidateVolatileReloadPermanent(asList(zeSchema.typeCode()));

		pointeur.set(Schemas.TITLE, "Pointeur d'ours brisé");
		transaction = new Transaction(pointeur);
		try {
			recordServices.execute(transaction);
			fail("Exception expected");
		} catch (RecordServicesRuntimeException.RecordServicesRuntimeException_ExceptionWhileCalculating e) {
			//OK
		}

		transaction.getRecordUpdateOptions().setCatchBrokenReferenceErrors(true);
		recordServices.execute(transaction);

		assertThatRecord(pointeur).exists();
		assertThatRecord(pointeur).extracting("title", "stringMetadata").isEqualTo(asList(
				"Pointeur d'ours brisé", "Pointeur d'ours brisé[]"));

	}

	@Test()
	public void whenExecutingATransactionWithRecordsOfMultipleDataStoresThenRecordsInsertedIn()
			throws Exception {
		defineSchemasManager().using(schemas.whichIsIsStoredInDataStore("events"));

		Transaction tx = new Transaction();
		Record record1InEventsDataStore = tx.add(new TestRecord(zeSchema.instance(), "record1"));
		Record record2InEventsDataStore = tx.add(new TestRecord(zeSchema.instance(), "record2"));
		Record record1InRecordsDataStore = tx.add(new TestRecord(anotherSchema.instance(), "record3"));
		Record record2InRecordsDataStore = tx.add(new TestRecord(anotherSchema.instance(), "record4"));

		recordServices.execute(tx);

		RecordServices recordServices = getModelLayerFactory().newCachelessRecordServices();

		assertThat(recordServices.getById(DataStore.EVENTS, "record1")).isNotNull();
		assertThat(recordServices.getById(DataStore.EVENTS, "record2")).isNotNull();
		assertThat(recordServices.getById(DataStore.RECORDS, "record3")).isNotNull();
		assertThat(recordServices.getById(DataStore.RECORDS, "record4")).isNotNull();

		try {
			assertThat(recordServices.getDocumentById("record1")).isNotNull();
			fail("Exception expected");
		} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
			//OK
		}

		try {
			assertThat(recordServices.getDocumentById("record2")).isNotNull();
			fail("Exception expected");
		} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
			//OK
		}
		assertThat(recordServices.getDocumentById("record3")).isNotNull();
		assertThat(recordServices.getDocumentById("record4")).isNotNull();

		RecordDao recordsDataStore = getDataLayerFactory().newRecordDao();
		RecordDao eventsDataStore = getDataLayerFactory().newEventsDao();

		assertThat(eventsDataStore.get("record1")).isNotNull();
		assertThat(eventsDataStore.get("record2")).isNotNull();
		try {
			eventsDataStore.get("record3");
			fail("Exception expected");
		} catch (RecordDaoException.NoSuchRecordWithId e) {
			//OK
		}
		try {
			eventsDataStore.get("record4");
			fail("Exception expected");
		} catch (RecordDaoException.NoSuchRecordWithId e) {
			//OK
		}

		try {
			recordsDataStore.get("record1");
			fail("Exception expected");
		} catch (RecordDaoException.NoSuchRecordWithId e) {
			//OK
		}
		try {
			recordsDataStore.get("record2");
			fail("Exception expected");
		} catch (RecordDaoException.NoSuchRecordWithId e) {
			//OK
		}

		assertThat(recordsDataStore.get("record3")).isNotNull();
		assertThat(recordsDataStore.get("record4")).isNotNull();
	}

	@Test
	public void whenCreateRecordsThenEstimatedRecordSizeBasedOnStringAndTextFields()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(whichIsNotSearchable)
				.withALargeTextMetadata(whichIsNotSearchable)
				.withAMultivaluedLargeTextMetadata(whichIsNotSearchable));

		Record emptyRecord1 = recordServices.newRecordWithSchema(zeSchema.instance());
		Record emptyRecord2 = recordServices.newRecordWithSchema(zeSchema.instance());
		recordServices.add(emptyRecord1);
		recordServices.add(emptyRecord2);

		int emptyRecordSize = emptyRecord1.<Integer>get(ESTIMATED_SIZE);
		assertThat(emptyRecordSize)
				.isGreaterThan(0)
				.isEqualTo(emptyRecord2.<Integer>get(ESTIMATED_SIZE));

		Record record = recordServices.newRecordWithSchema(zeSchema.instance());
		recordServices.add(record.set(zeSchema.stringMetadata(), "This is a simple value"));
		assertThat(record.<Integer>get(ESTIMATED_SIZE) - emptyRecordSize).isEqualTo(60);

		record = recordServices.newRecordWithSchema(zeSchema.instance());
		recordServices.add(record.set(zeSchema.stringMetadata(), "This is a little bit longer value"));
		assertThat(record.<Integer>get(ESTIMATED_SIZE) - emptyRecordSize).isEqualTo(82);

		record = recordServices.newRecordWithSchema(zeSchema.instance());
		recordServices.add(record.set(zeSchema.largeTextMetadata(), "This is a simple value"));
		assertThat(record.<Integer>get(ESTIMATED_SIZE) - emptyRecordSize).isEqualTo(60);

		record = recordServices.newRecordWithSchema(zeSchema.instance());
		recordServices.add(record.set(zeSchema.multivaluedLargeTextMetadata(),
				asList("This is a simple value", "This is an other simple value")));
		assertThat(record.<Integer>get(ESTIMATED_SIZE) - emptyRecordSize).isEqualTo(138);
	}

	@Test
	public void whenUpdateRecordsThenEstimatedRecordSizeUpdated()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(whichIsNotSearchable)
				.withALargeTextMetadata(whichIsNotSearchable)
				.withAMultivaluedLargeTextMetadata(whichIsNotSearchable));

		Record record = recordServices.newRecordWithSchema(zeSchema.instance());
		recordServices.add(record);

		int emptyRecordSize = record.<Integer>get(ESTIMATED_SIZE);
		assertThat(emptyRecordSize).isGreaterThan(0);

		recordServices.update(record.set(zeSchema.stringMetadata(), "This is a simple value"));
		assertThat(record.<Integer>get(ESTIMATED_SIZE) - emptyRecordSize).isEqualTo(60);

		recordServices.update(record.set(zeSchema.stringMetadata(), "This is a little bit longer value"));
		assertThat(record.<Integer>get(ESTIMATED_SIZE) - emptyRecordSize).isEqualTo(82);

		recordServices.update(record.set(zeSchema.largeTextMetadata(), "This is a simple value"));
		assertThat(record.<Integer>get(ESTIMATED_SIZE) - emptyRecordSize).isEqualTo(142);

		recordServices.update(record.set(zeSchema.stringMetadata(), null));
		assertThat(record.<Integer>get(ESTIMATED_SIZE) - emptyRecordSize).isEqualTo(60);

		recordServices.update(record.set(zeSchema.multivaluedLargeTextMetadata(),
				asList("This is a simple value", "This is an other simple value")));
		assertThat(record.<Integer>get(ESTIMATED_SIZE) - emptyRecordSize).isEqualTo(198);
	}

	@Test
	public void whenCreatingRecordSearchableFieldsThenBigger()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(whichIsSearchable)
				.withALargeTextMetadata(whichIsSearchable)
				.withAMultivaluedLargeTextMetadata(whichIsSearchable));

		Record emptyRecord1 = recordServices.newRecordWithSchema(zeSchema.instance());
		Record emptyRecord2 = recordServices.newRecordWithSchema(zeSchema.instance());
		recordServices.add(emptyRecord1);
		recordServices.add(emptyRecord2);

		int emptyRecordSize = emptyRecord1.<Integer>get(ESTIMATED_SIZE);
		assertThat(emptyRecordSize)
				.isGreaterThan(0)
				.isEqualTo(emptyRecord2.<Integer>get(ESTIMATED_SIZE));

		Record record = recordServices.newRecordWithSchema(zeSchema.instance());
		recordServices.add(record.set(zeSchema.stringMetadata(), "This is a simple value"));
		assertThat(record.<Integer>get(ESTIMATED_SIZE) - emptyRecordSize).isEqualTo(120);

		record = recordServices.newRecordWithSchema(zeSchema.instance());
		recordServices.add(record.set(zeSchema.stringMetadata(), "This is a little bit longer value"));
		assertThat(record.<Integer>get(ESTIMATED_SIZE) - emptyRecordSize).isEqualTo(164);

		record = recordServices.newRecordWithSchema(zeSchema.instance());
		recordServices.add(record.set(zeSchema.largeTextMetadata(), "This is a simple value"));
		assertThat(record.<Integer>get(ESTIMATED_SIZE) - emptyRecordSize).isEqualTo(120);

		record = recordServices.newRecordWithSchema(zeSchema.instance());
		recordServices.add(record.set(zeSchema.multivaluedLargeTextMetadata(),
				asList("This is a simple value", "This is an other simple value")));
		assertThat(record.<Integer>get(ESTIMATED_SIZE) - emptyRecordSize).isEqualTo(276);
	}


	@Test
	public void whenCreatingRecordSortFieldsThenBigger()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(whichIsSortable)
				.withALargeTextMetadata(whichIsSortable));

		Record emptyRecord1 = recordServices.newRecordWithSchema(zeSchema.instance());
		Record emptyRecord2 = recordServices.newRecordWithSchema(zeSchema.instance());
		recordServices.add(emptyRecord1);
		recordServices.add(emptyRecord2);

		int emptyRecordSize = emptyRecord1.<Integer>get(ESTIMATED_SIZE);
		assertThat(emptyRecordSize)
				.isGreaterThan(0)
				.isEqualTo(emptyRecord2.<Integer>get(ESTIMATED_SIZE));

		Record record = recordServices.newRecordWithSchema(zeSchema.instance());
		recordServices.add(record.set(zeSchema.stringMetadata(), "1234 is a simple value"));
		assertThat(record.<Integer>get(ESTIMATED_SIZE) - emptyRecordSize).isEqualTo(130);

		record = recordServices.newRecordWithSchema(zeSchema.instance());
		recordServices.add(record.set(zeSchema.stringMetadata(), "12345 is a little bit longer value"));
		assertThat(record.<Integer>get(ESTIMATED_SIZE) - emptyRecordSize).isEqualTo(176);
	}

	@Test
	public void givenCopiedMetadataCopyingSummaryMetadatasAndRefIndexedInCacheThenLoadedOnceUsingGetById()
			throws Exception {
		defineSchemasManager().using(schemas.with((MetadataSchemaTypesBuilder b) -> {
			b.getSchemaType("zeSchemaType").setRecordCacheType(SUMMARY_CACHED_WITHOUT_VOLATILE);
			b.getSchemaType("zeSchemaType").createMetadata("m1").setType(STRING).setEssentialInSummary(true);
			b.getSchemaType("zeSchemaType").createMetadata("m2").setType(STRING).setEssentialInSummary(true);
			b.getSchemaType("anotherSchemaType").setRecordCacheType(SUMMARY_CACHED_WITHOUT_VOLATILE);
			b.getSchemaType("anotherSchemaType").createMetadata("ref").defineReferencesTo(b.getSchemaType("zeSchemaType")).setCacheIndex(true);
			b.getSchemaType("anotherSchemaType").createMetadata("m1Copied").setType(STRING).defineDataEntry().asCopied(
					b.getMetadata("anotherSchemaType_default_ref"), b.getMetadata("zeSchemaType_default_m1"));
			b.getSchemaType("anotherSchemaType").createMetadata("m2Copied").setType(STRING).defineDataEntry().asCopied(
					b.getMetadata("anotherSchemaType_default_ref"), b.getMetadata("zeSchemaType_default_m2"));
			b.getSchemaType("anotherSchemaType").createMetadata("mCalculated")
					.setType(STRING).defineDataEntry().asJexlScript("ref.m1;");
		}));

		getDataLayerFactory().getDataLayerLogger().setPrintAllQueriesLongerThanMS(0).setQueryDebuggingMode(true);
		Transaction tx = new Transaction();
		Record record1 = tx.add(newRecord(zeSchema).set(zeSchema.metadata("m1"), "v1").set(zeSchema.metadata("m2"), "v2"));
		Record record2 = tx.add(newRecord(zeSchema).set(zeSchema.metadata("m1"), "v3").set(zeSchema.metadata("m2"), "v4"));
		execute(tx);

		QueryCounter queryCounter = new QueryCounter(getDataLayerFactory(), getClass());


		Record record3 = newRecord(anotherSchema).set(anotherSchema.metadata("ref"), record1);
		recordServices.add(record3);
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);

		recordServices.update(newRecord(anotherSchema).set(anotherSchema.metadata("ref"), record2));
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		recordServices.refresh(record3);
		assertThat(record3.<String>get(anotherSchema.metadata("m1Copied"))).isEqualTo("v1");
		assertThat(record3.<String>get(anotherSchema.metadata("m2Copied"))).isEqualTo("v2");
		assertThat(record3.<String>get(anotherSchema.metadata("mCalculated"))).isEqualTo("v1");
		queryCounter.reset();

		recordServices.update(record1.set(zeSchema.metadata("m1"), "v5").set(zeSchema.metadata("m2"), "v6"));
		//A getById is made to retrieve record3 and update it, that's ok
		assertThat(queryCounter.newQueryCalls()).isEqualTo(1);
		recordServices.refresh(record3);
		assertThat(record3.<String>get(anotherSchema.metadata("m1Copied"))).isEqualTo("v5");
		assertThat(record3.<String>get(anotherSchema.metadata("m2Copied"))).isEqualTo("v6");
		assertThat(record3.<String>get(anotherSchema.metadata("mCalculated"))).isEqualTo("v5");
		queryCounter.reset();

		recordServices.executeHandlingImpactsAsync(new Transaction(record1.set(zeSchema.metadata("m1"), "v7").set(zeSchema.metadata("m2"), "v8")));
		waitForBatchProcess();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(1);
		recordServices.refresh(record3);
		assertThat(record3.<String>get(anotherSchema.metadata("m1Copied"))).isEqualTo("v7");
		assertThat(record3.<String>get(anotherSchema.metadata("m2Copied"))).isEqualTo("v8");
		assertThat(record3.<String>get(anotherSchema.metadata("mCalculated"))).isEqualTo("v7");
		queryCounter.reset();

		recordServices.execute(new Transaction(record3.set(anotherSchema.metadata("ref"), record2)));
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		recordServices.refresh(record3);
		assertThat(record3.<String>get(anotherSchema.metadata("m1Copied"))).isEqualTo("v3");
		assertThat(record3.<String>get(anotherSchema.metadata("m2Copied"))).isEqualTo("v4");
		assertThat(record3.<String>get(anotherSchema.metadata("mCalculated"))).isEqualTo("v3");
		queryCounter.reset();

		recordServices.executeHandlingImpactsAsync(new Transaction(record3.set(anotherSchema.metadata("ref"), record1)));
		waitForBatchProcess();
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		recordServices.refresh(record3);
		assertThat(record3.<String>get(anotherSchema.metadata("m1Copied"))).isEqualTo("v7");
		assertThat(record3.<String>get(anotherSchema.metadata("m2Copied"))).isEqualTo("v8");
		assertThat(record3.<String>get(anotherSchema.metadata("mCalculated"))).isEqualTo("v7");
		queryCounter.reset();

	}


	@Test
	public void givenCopiedMetadataCopyingNonSummaryMetadatasThenLoadedOnceUsingGetById() throws Exception {
		defineSchemasManager().using(schemas.with((MetadataSchemaTypesBuilder b) -> {
			b.getSchemaType("zeSchemaType").setRecordCacheType(SUMMARY_CACHED_WITHOUT_VOLATILE);
			b.getSchemaType("zeSchemaType").createMetadata("m1").setType(STRING);
			b.getSchemaType("zeSchemaType").createMetadata("m2").setType(STRING);
			b.getSchemaType("anotherSchemaType").setRecordCacheType(SUMMARY_CACHED_WITHOUT_VOLATILE);
			b.getSchemaType("anotherSchemaType").createMetadata("ref").defineReferencesTo(b.getSchemaType("zeSchemaType"));
			b.getSchemaType("anotherSchemaType").createMetadata("m1Copied").setType(STRING).defineDataEntry().asCopied(
					b.getMetadata("anotherSchemaType_default_ref"), b.getMetadata("zeSchemaType_default_m1"));
			b.getSchemaType("anotherSchemaType").createMetadata("m2Copied").setType(STRING).defineDataEntry().asCopied(
					b.getMetadata("anotherSchemaType_default_ref"), b.getMetadata("zeSchemaType_default_m2"));
			b.getSchemaType("anotherSchemaType").createMetadata("mCalculated")
					.setType(STRING).defineDataEntry().asJexlScript("ref.m1");
		}));

		Transaction tx = new Transaction();
		Record record1 = tx.add(newRecord(zeSchema).set(zeSchema.metadata("m1"), "v1").set(zeSchema.metadata("m2"), "v2"));
		Record record2 = tx.add(newRecord(zeSchema).set(zeSchema.metadata("m1"), "v3").set(zeSchema.metadata("m2"), "v4"));
		execute(tx);

		QueryCounter queryCounter = new QueryCounter(getDataLayerFactory(), getClass());


		Record record3 = newRecord(anotherSchema).set(anotherSchema.metadata("ref"), record1);
		recordServices.add(record3);

		assertThat(queryCounter.newQueryCalls()).isEqualTo(1);

		recordServices.update(newRecord(anotherSchema).set(anotherSchema.metadata("ref"), record2));
		assertThat(queryCounter.newQueryCalls()).isEqualTo(1);
		recordServices.refresh(record3);
		assertThat(record3.<String>get(anotherSchema.metadata("m1Copied"))).isEqualTo("v1");
		assertThat(record3.<String>get(anotherSchema.metadata("m2Copied"))).isEqualTo("v2");
		assertThat(record3.<String>get(anotherSchema.metadata("mCalculated"))).isEqualTo("v1");
		queryCounter.reset();

		recordServices.update(record1.set(zeSchema.metadata("m1"), "v5").set(zeSchema.metadata("m2"), "v6"));
		//FIX ME : assertThat(queryCounter.newQueryCalls()).isEqualTo(1);
		recordServices.refresh(record3);
		assertThat(record3.<String>get(anotherSchema.metadata("m1Copied"))).isEqualTo("v5");
		assertThat(record3.<String>get(anotherSchema.metadata("m2Copied"))).isEqualTo("v6");
		assertThat(record3.<String>get(anotherSchema.metadata("mCalculated"))).isEqualTo("v5");
		queryCounter.reset();

		recordServices.executeHandlingImpactsAsync(new Transaction(record1.set(zeSchema.metadata("m1"), "v7").set(zeSchema.metadata("m2"), "v8")));
		waitForBatchProcess();
		//FIX ME : assertThat(queryCounter.newQueryCalls()).isEqualTo(1);
		recordServices.refresh(record3);
		assertThat(record3.<String>get(anotherSchema.metadata("m1Copied"))).isEqualTo("v7");
		assertThat(record3.<String>get(anotherSchema.metadata("m2Copied"))).isEqualTo("v8");
		assertThat(record3.<String>get(anotherSchema.metadata("mCalculated"))).isEqualTo("v7");
		queryCounter.reset();

		recordServices.execute(new Transaction(record3.set(anotherSchema.metadata("ref"), record2)));
		//FIX ME : assertThat(queryCounter.newQueryCalls()).isEqualTo(1);
		recordServices.refresh(record3);
		assertThat(record3.<String>get(anotherSchema.metadata("m1Copied"))).isEqualTo("v3");
		assertThat(record3.<String>get(anotherSchema.metadata("m2Copied"))).isEqualTo("v4");
		assertThat(record3.<String>get(anotherSchema.metadata("mCalculated"))).isEqualTo("v3");
		queryCounter.reset();

		recordServices.executeHandlingImpactsAsync(new Transaction(record3.set(anotherSchema.metadata("ref"), record1)));
		waitForBatchProcess();
		//FIX ME : assertThat(queryCounter.newQueryCalls()).isEqualTo(1);
		recordServices.refresh(record3);
		assertThat(record3.<String>get(anotherSchema.metadata("m1Copied"))).isEqualTo("v7");
		assertThat(record3.<String>get(anotherSchema.metadata("m2Copied"))).isEqualTo("v8");
		assertThat(record3.<String>get(anotherSchema.metadata("mCalculated"))).isEqualTo("v7");
		queryCounter.reset();

	}

	private MetadataSchemaTypesConfigurator aMetadataInAnotherSchemaContainingAReferenceToZeSchemaAndACalculatorRetreivingIt() {
		return new MetadataSchemaTypesConfigurator() {

			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaBuilder anotherSchemaBuilder = schemaTypes.getSchema(anotherSchema.code());
				MetadataSchemaTypeBuilder zeSchemaTypeBuilder = schemaTypes.getSchemaType(zeSchema.typeCode());

				anotherSchemaBuilder.create("aStringMetadata").setType(STRING);
				anotherSchemaBuilder.create("aCalculatedMetadataToAReference").setType(MetadataValueType.REFERENCE)
						.setMultivalue(true)
						.defineReferencesTo(zeSchemaTypeBuilder)
						.defineDataEntry().asCalculated(RecordServicesAcceptanceTestCalculator.class);
			}
		};
	}

	public static class RecordServicesAcceptanceTestCalculator extends AbstractMetadataValueCalculator<List<String>> {

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

	public static final class TitleLengthCalculator extends AbstractMetadataValueCalculator<Double> {

		LocalDependency<String> titleDependency = LocalDependency.toAString(Schemas.TITLE.getLocalCode());

		@Override
		public Double calculate(CalculatorParameters parameters) {
			return (double) parameters.get(titleDependency).length();
		}

		@Override
		public Double getDefaultValue() {
			return 0.0;
		}

		@Override
		public MetadataValueType getReturnType() {
			return MetadataValueType.NUMBER;
		}

		@Override
		public boolean isMultiValue() {
			return false;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return asList(titleDependency);
		}
	}
}
