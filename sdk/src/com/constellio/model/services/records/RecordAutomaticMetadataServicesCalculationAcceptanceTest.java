package com.constellio.model.services.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.DaysBetweenSingleLocalDateAndAnotherSchemaRequiredDateCalculator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RecordAutomaticMetadataServicesCalculationAcceptanceTest extends ConstellioTest {

	RecordUpdateOptions options = new RecordUpdateOptions();
	RecordServicesImpl recordServices;

	RecordAutomaticMetadataServices services;

	RecordProvider recordProvider;

	String idReferencedRecordWithJan1DateValue, idReferencedRecordWithJan2DateValue, idReferencedRecordWithoutDateValue;
	RecordImpl record;

	LocalDateTime jan1 = new LocalDateTime(2014, 1, 1, 0, 0);
	LocalDateTime jan2 = new LocalDateTime(2014, 1, 2, 0, 0);
	LocalDateTime jan3 = new LocalDateTime(2014, 1, 3, 0, 0);

	TestsSchemasSetup schemas;
	ZeSchemaMetadatas zeSchema;
	AnotherSchemaMetadatas anotherSchema;

	@Mock Metadata firstReindexedMetadata, secondReindexedMetadata;
	TransactionRecordsReindexation reindexedMetadata;

	@Before
	public void setUp() {

		schemas = new TestsSchemasSetup();
		zeSchema = schemas.new ZeSchemaMetadatas();
		anotherSchema = schemas.new AnotherSchemaMetadatas();

		services = new RecordAutomaticMetadataServices(getModelLayerFactory());

		recordServices = spy((RecordServicesImpl) getModelLayerFactory().newCachelessRecordServices());
		recordProvider = recordServices.newRecordProvider(null, new Transaction());

		DaysBetweenSingleLocalDateAndAnotherSchemaRequiredDateCalculator.invokationCounter.set(0);

		reindexedMetadata = new TransactionRecordsReindexation(
				new MetadataList(firstReindexedMetadata, secondReindexedMetadata));
	}

	@Test
	public void givenCalculatedMetadataOfNewRecordWhenUpdatingThenCalculateValue()
			throws Exception {
		givenCalculatedNumberBasedOnLocalDateAndAnotherSchemaReferenceDate(false);
		record = new TestRecord(zeSchema);
		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithJan1DateValue);
		record.set(zeSchema.dateTimeMetadata(), jan3);

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, new Transaction(options));

		assertThat(record.<Double>get(zeSchema.calculatedDaysBetween())).isEqualTo(2.0);
	}

	@Test
	public void givenCalculatedMetadataOfNewRecordWithMultivaluesWhenUpdatingThenCalculateValue()
			throws Exception {
		givenCalculatedNumberBasedOnLocalDateAndAnotherSchemaReferenceDate(true);
		record = new TestRecord(zeSchema);
		record.set(zeSchema.secondReferenceToAnotherSchema(), asList(idReferencedRecordWithJan1DateValue));
		record.set(zeSchema.dateTimeMetadata(), asList(jan1, jan2));

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, new Transaction(options));

		assertThat(record.<Double>get(zeSchema.calculatedDaysBetween())).isEqualTo(1.0);
	}

	@Test
	public void givenCalculatedMetadataOfNewRecordMissingRequiredReferenceDependencyWhenUpdatingThenCalculatedValueIsDefaultValue()
			throws Exception {
		givenCalculatedNumberBasedOnLocalDateAndAnotherSchemaReferenceDate(false);
		record = new TestRecord(zeSchema);
		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithoutDateValue);
		record.set(zeSchema.dateTimeMetadata(), jan3);

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, new Transaction(options));

		assertThat(record.<Double>get(zeSchema.calculatedDaysBetween())).isEqualTo(-1.0);
	}

	@Test
	public void givenCalculatedMetadataOfNewRecordMissingRequiredLocalDependencyWhenUpdatingThenCalculatedValueIsDefaultValue()
			throws Exception {
		givenCalculatedNumberBasedOnLocalDateAndAnotherSchemaReferenceDate(false);
		record = new TestRecord(zeSchema);
		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithJan1DateValue);
		record.set(zeSchema.dateTimeMetadata(), null);

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, new Transaction(options));

		assertThat(record.<Double>get(zeSchema.calculatedDaysBetween())).isEqualTo(-1.0);
	}

	@Test
	public void givenExistingRecordDependenciesOfCalculatedMetadataNotModifiedWhenUpdatingThenNotRecalculate()
			throws Exception {
		givenCalculatedNumberBasedOnLocalDateAndAnotherSchemaReferenceDate(false);
		record = new TestRecord(zeSchema);
		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithJan1DateValue);
		record.set(zeSchema.dateTimeMetadata(), jan3);
		add();

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, new Transaction(options));

		assertThat(DaysBetweenSingleLocalDateAndAnotherSchemaRequiredDateCalculator.invokationCounter.get()).isEqualTo(1);
		assertThat(record.<Double>get(zeSchema.calculatedDaysBetween())).isEqualTo(2.0);
		verifyZeroInteractions(recordServices);
	}

	@Test
	public void givenExistingRecordReferenceDependencyOfCalculatedMetadataModifiedWhenUpdatingThenRecalculate()
			throws Exception {
		givenCalculatedNumberBasedOnLocalDateAndAnotherSchemaReferenceDate(false);
		record = new TestRecord(zeSchema);
		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithJan1DateValue);
		record.set(zeSchema.dateTimeMetadata(), jan3);
		add();

		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithJan2DateValue);
		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, new Transaction(options));

		assertThat(DaysBetweenSingleLocalDateAndAnotherSchemaRequiredDateCalculator.invokationCounter.get()).isEqualTo(2);
		assertThat(record.<Double>get(zeSchema.calculatedDaysBetween())).isEqualTo(1.0);
	}

	@Test
	public void givenExistingRecordLocalDependencyOfCalculatedMetadataModifiedWhenUpdatingThenRecalculate()
			throws Exception {
		givenCalculatedNumberBasedOnLocalDateAndAnotherSchemaReferenceDate(false);
		record = new TestRecord(zeSchema);
		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithJan1DateValue);
		record.set(zeSchema.dateTimeMetadata(), jan3);
		add();

		record.set(zeSchema.dateTimeMetadata(), jan2);
		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, new Transaction(options));

		assertThat(DaysBetweenSingleLocalDateAndAnotherSchemaRequiredDateCalculator.invokationCounter.get()).isEqualTo(2);
		assertThat(record.<Double>get(zeSchema.calculatedDaysBetween())).isEqualTo(1.0);
	}

	@Test
	public void givenExistingRecordReferenceDependencyOfCalculatedMetadataRemovedWhenUpdatingThenReplaceWithDefaultValue()
			throws Exception {
		givenCalculatedNumberBasedOnLocalDateAndAnotherSchemaReferenceDate(false);
		record = new TestRecord(zeSchema);
		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithJan1DateValue);
		record.set(zeSchema.dateTimeMetadata(), jan3);
		add();

		record.set(zeSchema.secondReferenceToAnotherSchema(), null);
		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, new Transaction(options));

		assertThat(DaysBetweenSingleLocalDateAndAnotherSchemaRequiredDateCalculator.invokationCounter.get()).isEqualTo(1);
		assertThat(record.<Double>get(zeSchema.calculatedDaysBetween())).isEqualTo(-1.0);
		verifyZeroInteractions(recordServices);

	}

	@Test
	public void givenExistingRecordLocalDependencyOfCalculatedMetadataRemovedWhenUpdatingThenReplaceWithDefaultValue()
			throws Exception {
		givenCalculatedNumberBasedOnLocalDateAndAnotherSchemaReferenceDate(false);
		record = new TestRecord(zeSchema);
		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithJan1DateValue);
		record.set(zeSchema.dateTimeMetadata(), jan3);
		add();

		record.set(zeSchema.dateTimeMetadata(), null);
		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, new Transaction(options));

		assertThat(DaysBetweenSingleLocalDateAndAnotherSchemaRequiredDateCalculator.invokationCounter.get()).isEqualTo(1);
		assertThat(record.<Double>get(zeSchema.calculatedDaysBetween())).isEqualTo(-1.0);

	}

	private void add()
			throws RecordServicesException {
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		recordServices.add(record);
	}

	protected void givenCalculatedNumberBasedOnLocalDateAndAnotherSchemaReferenceDate(boolean multivalue)
			throws Exception {
		defineSchemasManager().using(schemas.withCalculatedDaysBetweenLocalDateAndAnotherSchemaRequiredDate(multivalue));
		addRecords();
	}

	private void addRecords()
			throws RecordServicesException {
		Record record = new TestRecord(anotherSchema);
		record.set(anotherSchema.dateMetadata(), jan1);
		recordServices.add(record);
		idReferencedRecordWithJan1DateValue = record.getId();

		record = new TestRecord(anotherSchema);
		record.set(anotherSchema.dateMetadata(), jan2);
		recordServices.add(record);
		idReferencedRecordWithJan2DateValue = record.getId();

		record = new TestRecord(anotherSchema);
		recordServices.add(record);
		idReferencedRecordWithoutDateValue = record.getId();

		reset(recordServices);
	}
}
