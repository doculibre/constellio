package com.constellio.model.services.records;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.records.cache.CachedRecordServices;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RecordAutomaticMetadataServicesCopyAcceptanceTest extends ConstellioTest {

	Transaction zeTransaction = new Transaction();
	RecordUpdateOptions options = zeTransaction.getRecordUpdateOptions();
	CachedRecordServices recordServices;
	RecordProvider recordProvider;

	RecordAutomaticMetadataServices services;

	String idReferencedRecordWithAStringAndADateValue, idReferencedRecordWithAnotherDateValue, idReferencedRecordWithoutValue;
	RecordImpl record;

	String aString = aString();
	LocalDateTime aDate = aDateTime();
	LocalDateTime anotherDate = aDateTime();

	List<String> aStringList = asList(aString(), aString());
	List<LocalDateTime> aDateList = asList(aDateTime(), aDateTime());
	List<LocalDateTime> anotherDateList = asList(aDateTime(), aDateTime());

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

		recordServices = spy((CachedRecordServices) getModelLayerFactory().newRecordServices());
		recordProvider = new RecordProvider(recordServices, null, new ArrayList<Record>(), new Transaction());

		reindexedMetadata = new TransactionRecordsReindexation(new MetadataList(firstReindexedMetadata, secondReindexedMetadata));
	}

	@Test
	public void givenCopiedMetadataOfNewRecordUsingSingleValueReferenceWhenUpdatingThenCopyValue()
			throws Exception {
		givenSchemaWithTextAndDateCopiedEntryUsingSingleValueReferenceAndSomeRecordsInOtherSchema();
		record.set(zeSchema.firstReferenceToAnotherSchema(), idReferencedRecordWithAStringAndADateValue);
		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithAStringAndADateValue);

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, zeTransaction);

		assertThat((Object) record.get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo(aString);
		assertThat((Object) record.get(zeSchema.dateCopiedFromSecondReferenceDateMeta())).isEqualTo(aDate);
	}

	@Test
	public void givenCopiedMetadataOfNewRecordUsingMultivalueReferenceWhenUpdatingThenCopyValue()
			throws Exception {
		givenSchemaWithTextAndDateCopiedEntryUsingMultivalueReferencesAndSomeRecordsInOtherSchema();
		record.set(zeSchema.firstReferenceToAnotherSchema(), asList(idReferencedRecordWithAStringAndADateValue));
		record.set(zeSchema.secondReferenceToAnotherSchema(),
				asList(idReferencedRecordWithAStringAndADateValue, idReferencedRecordWithAnotherDateValue));

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, zeTransaction);

		assertThat((Object) record.get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo(asList(aString));
		assertThat((Object) record.get(zeSchema.dateCopiedFromSecondReferenceDateMeta())).isEqualTo(asList(aDate, anotherDate));
	}

	@Test
	public void givenCopiedMultivalueMetadataOfNewRecordWhenUpdatingThenCopyValue()
			throws Exception {
		givenSchemaWithMultivalueTextAndDateCopiedEntryAndSomeRecordsInOtherSchema();
		record.set(zeSchema.firstReferenceToAnotherSchema(), idReferencedRecordWithAStringAndADateValue);
		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithAStringAndADateValue);

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, zeTransaction);

		assertThat((Object) record.get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo(aStringList);
		assertThat((Object) record.get(zeSchema.dateCopiedFromSecondReferenceDateMeta())).isEqualTo(aDateList);
	}

	@Test
	public void givenCopiedMultivalueMetadataOfNewRecordWithMultivalueReferencesWhenUpdatingThenCopyValue()
			throws Exception {
		givenSchemaWithMultivalueTextAndDateCopiedEntryUsingMultivalueReferencesAndSomeRecordsInOtherSchema();
		record.set(zeSchema.firstReferenceToAnotherSchema(),
				asList(idReferencedRecordWithAStringAndADateValue, idReferencedRecordWithAStringAndADateValue));
		record.set(zeSchema.secondReferenceToAnotherSchema(),
				asList(idReferencedRecordWithAStringAndADateValue, idReferencedRecordWithAnotherDateValue));

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, zeTransaction);

		List<String> expectedStrings = new ArrayList<>();
		expectedStrings.addAll(aStringList);
		expectedStrings.addAll(aStringList);
		assertThat((Object) record.get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo(expectedStrings);
		List<LocalDateTime> expectedDates = new ArrayList<>();
		expectedDates.addAll(aDateList);
		expectedDates.addAll(anotherDateList);
		assertThat((Object) record.get(zeSchema.dateCopiedFromSecondReferenceDateMeta())).isEqualTo(expectedDates);
	}

	@Test
	public void givenCopiedMetadataOfNewRecordReferencingARecordWithoutCopiedValueWhenUpdatingThenCopiedValueIsNull()
			throws Exception {
		givenSchemaWithTextAndDateCopiedEntryUsingSingleValueReferenceAndSomeRecordsInOtherSchema();

		record.set(zeSchema.firstReferenceToAnotherSchema(), idReferencedRecordWithoutValue);
		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithAStringAndADateValue);

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, zeTransaction);

		assertThat((Object) record.get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isNull();
		assertThat((Object) record.get(zeSchema.dateCopiedFromSecondReferenceDateMeta())).isEqualTo(aDate);
	}

	@Test
	public void givenCopiedMetadataOfNewRecordReferencingMutlipleRecordsWithoutCopiedValueWhenUpdatingThenCopiedValueIsNull()
			throws Exception {
		givenSchemaWithTextAndDateCopiedEntryUsingMultivalueReferencesAndSomeRecordsInOtherSchema();

		record.set(zeSchema.firstReferenceToAnotherSchema(), asList(idReferencedRecordWithoutValue));
		record.set(zeSchema.secondReferenceToAnotherSchema(),
				asList(idReferencedRecordWithAStringAndADateValue, idReferencedRecordWithoutValue));

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, zeTransaction);

		assertThat((Object) record.get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo(new ArrayList<>());
		assertThat((Object) record.get(zeSchema.dateCopiedFromSecondReferenceDateMeta())).isEqualTo(asList(aDate));
	}

	@Test
	public void givenCopiedMultivalueMetadataOfNewRecordReferencingARecordWithoutCopiedValueWhenUpdatingThenCopiedValueIsEmptyList()
			throws Exception {
		givenSchemaWithMultivalueTextAndDateCopiedEntryAndSomeRecordsInOtherSchema();

		record.set(zeSchema.firstReferenceToAnotherSchema(), idReferencedRecordWithoutValue);
		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithAStringAndADateValue);

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, zeTransaction);

		assertThat((Object) record.get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo(new ArrayList<>());
		assertThat((Object) record.get(zeSchema.dateCopiedFromSecondReferenceDateMeta())).isEqualTo(aDateList);
	}

	@Test
	public void givenCopiedMultivalueMetadataOfNewRecordWithMultivalueReferencesToRecordsWithoutValueWhenUpdatingThenCopiedValueIsEmptyList()
			throws Exception {
		givenSchemaWithMultivalueTextAndDateCopiedEntryUsingMultivalueReferencesAndSomeRecordsInOtherSchema();

		record.set(zeSchema.firstReferenceToAnotherSchema(),
				asList(idReferencedRecordWithoutValue, idReferencedRecordWithoutValue));
		record.set(zeSchema.secondReferenceToAnotherSchema(),
				asList(idReferencedRecordWithoutValue, idReferencedRecordWithAStringAndADateValue));

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, zeTransaction);

		assertThat((Object) record.get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo(new ArrayList<>());
		assertThat((Object) record.get(zeSchema.dateCopiedFromSecondReferenceDateMeta())).isEqualTo(aDateList);
	}

	@Test
	public void givenCopiedMetadataOfNewRecordWithoutReferencedValueWhenUpdatingThenCopiedValueIsNull()
			throws Exception {
		givenSchemaWithTextAndDateCopiedEntryUsingSingleValueReferenceAndSomeRecordsInOtherSchema();
		record.set(zeSchema.firstReferenceToAnotherSchema(), idReferencedRecordWithAStringAndADateValue);

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, zeTransaction);

		assertThat((Object) record.get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo(aString);
		assertThat((Object) record.get(zeSchema.dateCopiedFromSecondReferenceDateMeta())).isNull();
	}

	@Test
	public void givenCopiedMetadataOfNewRecordWithUndefinedReferenceListWhenUpdatingThenCopiedValueIsEmptyLust()
			throws Exception {
		givenSchemaWithTextAndDateCopiedEntryUsingMultivalueReferencesAndSomeRecordsInOtherSchema();
		record.set(zeSchema.firstReferenceToAnotherSchema(), asList(idReferencedRecordWithAStringAndADateValue));

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, zeTransaction);

		assertThat((Object) record.get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo(asList(aString));
		assertThat((Object) record.get(zeSchema.dateCopiedFromSecondReferenceDateMeta())).isEqualTo(new ArrayList<>());
	}

	@Test
	public void givenCopiedMetadataOfNewRecordWithEmptyReferenceListWhenUpdatingThenCopiedValueIsEmptyLust()
			throws Exception {
		givenSchemaWithTextAndDateCopiedEntryUsingMultivalueReferencesAndSomeRecordsInOtherSchema();
		record.set(zeSchema.firstReferenceToAnotherSchema(), asList(idReferencedRecordWithAStringAndADateValue));
		record.set(zeSchema.secondReferenceToAnotherSchema(), new ArrayList<>());

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, zeTransaction);

		assertThat((Object) record.get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo(asList(aString));
		assertThat((Object) record.get(zeSchema.dateCopiedFromSecondReferenceDateMeta())).isEqualTo(new ArrayList<>());
	}

	@Test
	public void givenCopiedMultivalueMetadataOfNewRecordWithoutReferencedValueWhenUpdatingThenCopiedValueIsEmptyList()
			throws Exception {
		givenSchemaWithMultivalueTextAndDateCopiedEntryAndSomeRecordsInOtherSchema();
		record.set(zeSchema.firstReferenceToAnotherSchema(), idReferencedRecordWithAStringAndADateValue);

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, zeTransaction);

		assertThat((Object) record.get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo(aStringList);
		assertThat((Object) record.get(zeSchema.dateCopiedFromSecondReferenceDateMeta())).isEqualTo(new ArrayList<>());
	}

	@Test
	public void givenCopiedMultivalueMetadataOfNewRecordWithUndefinedReferencesListValueWhenUpdatingThenCopiedValueIsEmptyList()
			throws Exception {
		givenSchemaWithMultivalueTextAndDateCopiedEntryUsingMultivalueReferencesAndSomeRecordsInOtherSchema();
		record.set(zeSchema.firstReferenceToAnotherSchema(), asList(idReferencedRecordWithAStringAndADateValue));

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, zeTransaction);

		assertThat(record.getList(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo(aStringList);
		assertThat(record.getList(zeSchema.dateCopiedFromSecondReferenceDateMeta())).isEqualTo(new ArrayList<>());
	}

	@Test
	public void givenCopiedMultivalueMetadataOfNewRecordWithEmptyReferencesListValueWhenUpdatingThenCopiedValueIsEmptyList()
			throws Exception {
		givenSchemaWithMultivalueTextAndDateCopiedEntryUsingMultivalueReferencesAndSomeRecordsInOtherSchema();
		record.set(zeSchema.firstReferenceToAnotherSchema(), new ArrayList<>());
		record.set(zeSchema.firstReferenceToAnotherSchema(), asList(idReferencedRecordWithAStringAndADateValue));

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, zeTransaction);

		assertThat((Object) record.get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo(aStringList);
		assertThat((Object) record.get(zeSchema.dateCopiedFromSecondReferenceDateMeta())).isEqualTo(new ArrayList<>());
	}

	@Test
	public void givenCopiedMetadataOfExistingRecordWhenUpdatingThenDoNotCopyIfReferenceIsNotModified()
			throws Exception {
		givenSchemaWithTextAndDateCopiedEntryUsingSingleValueReferenceAndSomeRecordsInOtherSchema();
		reset(recordServices);
		record.set(zeSchema.firstReferenceToAnotherSchema(), idReferencedRecordWithAStringAndADateValue);
		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithAStringAndADateValue);
		add();

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, zeTransaction);
		assertThat(record.getModifiedValues().isEmpty());

		assertThat((Object) record.get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo(aString);
		assertThat((Object) record.get(zeSchema.dateCopiedFromSecondReferenceDateMeta())).isEqualTo(aDate);
		verifyZeroInteractions(recordServices);
	}

	@Test
	public void givenCopiedMetadataOfExistingRecordWhenUpdatingThenDoNotCopyIfReferenceListIsNotModified()
			throws Exception {
		givenSchemaWithTextAndDateCopiedEntryUsingMultivalueReferencesAndSomeRecordsInOtherSchema();
		reset(recordServices);
		record.set(zeSchema.firstReferenceToAnotherSchema(), asList(idReferencedRecordWithAStringAndADateValue));
		record.set(zeSchema.secondReferenceToAnotherSchema(), asList(idReferencedRecordWithAStringAndADateValue));
		add();

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, zeTransaction);
		assertThat(record.getModifiedValues().isEmpty());

		assertThat((Object) record.get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo(asList(aString));
		assertThat((Object) record.get(zeSchema.dateCopiedFromSecondReferenceDateMeta())).isEqualTo(asList(aDate));
		verifyZeroInteractions(recordServices);
	}

	@Test
	public void givenCopiedMultivalueMetadataOfExistingRecordWhenUpdatingThenDoNotCopyIfReferenceIsNotModified()
			throws Exception {
		givenSchemaWithMultivalueTextAndDateCopiedEntryAndSomeRecordsInOtherSchema();
		reset(recordServices);
		record.set(zeSchema.firstReferenceToAnotherSchema(), idReferencedRecordWithAStringAndADateValue);
		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithAStringAndADateValue);
		add();

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, zeTransaction);
		assertThat(record.getModifiedValues().isEmpty());

		assertThat((Object) record.get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo(aStringList);
		assertThat((Object) record.get(zeSchema.dateCopiedFromSecondReferenceDateMeta())).isEqualTo(aDateList);
		verifyZeroInteractions(recordServices);
	}

	@Test
	public void givenCopiedMultivalueMetadataOfExistingRecordWithMultivalueReferencesWhenUpdatingThenDoNotCopyIfReferenceIsNotModified()
			throws Exception {
		givenSchemaWithMultivalueTextAndDateCopiedEntryUsingMultivalueReferencesAndSomeRecordsInOtherSchema();
		reset(recordServices);
		record.set(zeSchema.firstReferenceToAnotherSchema(), asList(idReferencedRecordWithAStringAndADateValue));
		record.set(zeSchema.secondReferenceToAnotherSchema(), asList(idReferencedRecordWithAStringAndADateValue));
		add();

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, zeTransaction);
		assertThat(record.getModifiedValues().isEmpty());

		assertThat((Object) record.get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo(aStringList);
		assertThat((Object) record.get(zeSchema.dateCopiedFromSecondReferenceDateMeta())).isEqualTo(aDateList);
		verifyZeroInteractions(recordServices);
	}

	@Test
	public void givenCopiedMetadataOfExistingRecordWhenUpdatingThenOnlyCopyValuesWithDifferentReference()
			throws Exception {
		givenSchemaWithTextAndDateCopiedEntryUsingSingleValueReferenceAndSomeRecordsInOtherSchema();
		record.set(zeSchema.firstReferenceToAnotherSchema(), idReferencedRecordWithAStringAndADateValue);
		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithoutValue);
		add();
		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithAnotherDateValue);
		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, zeTransaction);

		assertThat((Object) record.get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo(aString);
		assertThat((Object) record.get(zeSchema.dateCopiedFromSecondReferenceDateMeta())).isEqualTo(anotherDate);
		verify(recordServices).realtimeGetRecordById(idReferencedRecordWithAnotherDateValue);
		verify(recordServices, never()).realtimeGetRecordById(idReferencedRecordWithoutValue);
		verify(recordServices, never()).realtimeGetRecordById(idReferencedRecordWithAStringAndADateValue);

	}

	@Test
	public void givenCopiedMetadataOfExistingRecordWhenUpdatingThenOnlyCopyValuesWithDifferentReferenceList()
			throws Exception {

		givenSchemaWithTextAndDateCopiedEntryUsingMultivalueReferencesAndSomeRecordsInOtherSchema();
		record.set(zeSchema.firstReferenceToAnotherSchema(),
				asList(idReferencedRecordWithAStringAndADateValue, idReferencedRecordWithAStringAndADateValue));
		record.set(zeSchema.secondReferenceToAnotherSchema(), asList(idReferencedRecordWithoutValue));
		add();
		record.set(zeSchema.secondReferenceToAnotherSchema(), asList(idReferencedRecordWithAnotherDateValue));
		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, zeTransaction);

		assertThat((Object) record.get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo(asList(aString, aString));
		assertThat((Object) record.get(zeSchema.dateCopiedFromSecondReferenceDateMeta())).isEqualTo(asList(anotherDate));
		verify(recordServices).realtimeGetRecordById(idReferencedRecordWithAnotherDateValue);
		verify(recordServices, never()).realtimeGetRecordById(idReferencedRecordWithoutValue);
		verify(recordServices, never()).realtimeGetRecordById(idReferencedRecordWithAStringAndADateValue);

	}

	@Test
	public void givenCopiedMultivalueMetadataOfExistingRecordWhenUpdatingThenOnlyCopyValuesWithDifferentReferences()
			throws Exception {
		givenSchemaWithMultivalueTextAndDateCopiedEntryAndSomeRecordsInOtherSchema();
		record.set(zeSchema.firstReferenceToAnotherSchema(), idReferencedRecordWithAStringAndADateValue);
		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithoutValue);
		add();
		record.set(zeSchema.secondReferenceToAnotherSchema(), idReferencedRecordWithAnotherDateValue);
		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, zeTransaction);

		assertThat((Object) record.get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo(aStringList);
		assertThat((Object) record.get(zeSchema.dateCopiedFromSecondReferenceDateMeta())).isEqualTo(anotherDateList);
		verify(recordServices).realtimeGetRecordById(idReferencedRecordWithAnotherDateValue);
		verify(recordServices, never()).realtimeGetRecordById(idReferencedRecordWithoutValue);
		verify(recordServices, never()).realtimeGetRecordById(idReferencedRecordWithAStringAndADateValue);

	}

	public void givenCopiedMultivalueMetadataOfExistingRecordWithMultivalueReferencesWhenUpdatingThenOnlyCopyValuesWithDifferentReferences()
			throws Exception {
		givenSchemaWithMultivalueTextAndDateCopiedEntryUsingMultivalueReferencesAndSomeRecordsInOtherSchema();
		record.set(zeSchema.firstReferenceToAnotherSchema(), asList(idReferencedRecordWithAStringAndADateValue));
		record.set(zeSchema.secondReferenceToAnotherSchema(), asList(idReferencedRecordWithoutValue));
		add();
		record.set(zeSchema.firstReferenceToAnotherSchema(), asList(idReferencedRecordWithAStringAndADateValue));
		record.set(zeSchema.secondReferenceToAnotherSchema(), asList(idReferencedRecordWithAnotherDateValue));
		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, zeTransaction);

		assertThat((Object) record.get(zeSchema.stringCopiedFromFirstReferenceStringMeta())).isEqualTo(aStringList);
		assertThat((Object) record.get(zeSchema.dateCopiedFromSecondReferenceDateMeta())).isEqualTo(anotherDateList);
		verify(recordServices).getDocumentById(idReferencedRecordWithAnotherDateValue);
		verify(recordServices, never()).getDocumentById(idReferencedRecordWithoutValue);
		verify(recordServices, never()).getDocumentById(idReferencedRecordWithAStringAndADateValue);

	}

	private List<String> toCodeList(List<Metadata> metadatas) {
		List<String> codes = new ArrayList<>();
		for (Metadata metadata : metadatas) {
			codes.add(metadata.getCode());
		}
		return codes;
	}

	private <T> MetadataValueCalculator<T> newCalculatorWithLocalDependencies(final String... dependenciesCode) {
		return new MetadataValueCalculator<T>() {

			@Override
			public T calculate(CalculatorParameters parameters) {
				throw new UnsupportedOperationException("TODO");
			}

			@Override
			public T getDefaultValue() {
				throw new UnsupportedOperationException("TODO");
			}

			@Override
			public MetadataValueType getReturnType() {
				throw new UnsupportedOperationException("TODO");
			}

			@Override
			public List<? extends Dependency> getDependencies() {
				List<Dependency> dependencies = new ArrayList<>();
				for (String dependencyCode : dependenciesCode) {
					dependencies.add(LocalDependency.toAString(dependencyCode).whichIsRequired());
				}
				return dependencies;
			}

			@Override
			public boolean isMultiValue() {
				return false;
			}
		};
	}

	private <T> MetadataValueCalculator<T> newCalculatorWithReferenceDependencies(final String... dependenciesCode) {
		return new MetadataValueCalculator<T>() {

			@Override
			public T calculate(CalculatorParameters parameters) {
				throw new UnsupportedOperationException("TODO");
			}

			@Override
			public T getDefaultValue() {
				throw new UnsupportedOperationException("TODO");
			}

			@Override
			public MetadataValueType getReturnType() {
				throw new UnsupportedOperationException("TODO");
			}

			@Override
			public List<? extends Dependency> getDependencies() {
				List<Dependency> dependencies = new ArrayList<>();
				for (String dependencyCode : dependenciesCode) {
					dependencies.add(ReferenceDependency.toAString(dependencyCode, "notImportant").whichIsRequired());
				}
				return dependencies;
			}

			@Override
			public boolean isMultiValue() {
				return false;
			}
		};
	}

	private void add()
			throws RecordServicesException {
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		recordServices.add(record);
	}

	protected void givenSchemaWithTextAndDateCopiedEntryUsingSingleValueReferenceAndSomeRecordsInOtherSchema()
			throws Exception {
		defineSchemasManager().using(
				schemas.withTwoMetadatasCopyingAnotherSchemaValuesUsingTwoDifferentReferenceMetadata(false, false, false));
		defineAnotherSchemaRecordsWithSinglevalueMetadata();
		record = new TestRecord(zeSchema);
	}

	protected void givenSchemaWithTextAndDateCopiedEntryUsingMultivalueReferencesAndSomeRecordsInOtherSchema()
			throws Exception {
		defineSchemasManager().using(
				schemas.withTwoMetadatasCopyingAnotherSchemaValuesUsingTwoDifferentReferenceMetadata(false, true, false));
		defineAnotherSchemaRecordsWithSinglevalueMetadata();
		record = new TestRecord(zeSchema);
	}

	protected void givenSchemaWithMultivalueTextAndDateCopiedEntryAndSomeRecordsInOtherSchema()
			throws Exception {
		defineSchemasManager().using(
				schemas.withTwoMetadatasCopyingAnotherSchemaValuesUsingTwoDifferentReferenceMetadata(true, false, false));
		defineAnotherSchemaRecordsWithMultivaluesMetadata();
		record = new TestRecord(zeSchema);
	}

	protected void givenSchemaWithMultivalueTextAndDateCopiedEntryUsingMultivalueReferencesAndSomeRecordsInOtherSchema()
			throws Exception {
		defineSchemasManager().using(
				schemas.withTwoMetadatasCopyingAnotherSchemaValuesUsingTwoDifferentReferenceMetadata(true, true, false));
		defineAnotherSchemaRecordsWithMultivaluesMetadata();
		record = new TestRecord(zeSchema);
	}

	private void defineAnotherSchemaRecordsWithSinglevalueMetadata()
			throws RecordServicesException {
		Record record = new TestRecord(anotherSchema);
		record.set(anotherSchema.stringMetadata(), aString);
		record.set(anotherSchema.dateMetadata(), aDate);
		recordServices.add(record);
		idReferencedRecordWithAStringAndADateValue = record.getId();

		record = new TestRecord(anotherSchema);
		record.set(anotherSchema.dateMetadata(), anotherDate);
		recordServices.add(record);
		idReferencedRecordWithAnotherDateValue = record.getId();

		record = new TestRecord(anotherSchema);
		recordServices.add(record);
		idReferencedRecordWithoutValue = record.getId();
	}

	private void defineAnotherSchemaRecordsWithMultivaluesMetadata()
			throws RecordServicesException {
		Record record = new TestRecord(anotherSchema);
		record.set(anotherSchema.stringMetadata(), aStringList);
		record.set(anotherSchema.dateMetadata(), aDateList);
		recordServices.add(record);
		idReferencedRecordWithAStringAndADateValue = record.getId();

		record = new TestRecord(anotherSchema);
		record.set(anotherSchema.dateMetadata(), anotherDateList);
		recordServices.add(record);
		idReferencedRecordWithAnotherDateValue = record.getId();

		record = new TestRecord(anotherSchema);
		recordServices.add(record);
		idReferencedRecordWithoutValue = record.getId();
	}

}
