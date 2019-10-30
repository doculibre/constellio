package com.constellio.model.services.records;

import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.factories.ModelLayerLogger;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.sdk.tests.TestUtils.asSet;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RecordAutomaticMetadataServicesTest extends ConstellioTest {

	Transaction zeTransaction = new Transaction();
	RecordUpdateOptions options = zeTransaction.getRecordUpdateOptions();

	@Mock ModelLayerLogger modelLayerLogger;

	RecordAutomaticMetadataServices services;
	@Mock RecordProvider recordProvider;
	@Mock MetadataSchemasManager schemasManager;
	Set<Metadata> automaticMetadatas;
	List<Metadata> sortedMetadatas;
	TestsSchemasSetup schemas;
	ZeSchemaMetadatas zeSchema;
	AnotherSchemaMetadatas anotherSchema;

	RecordImpl record, recordWithReferenceToRecordWithValue;

	String aString = aString();
	List<String> aStringList = Arrays.asList(aString, aString);
	String anotherString = aString();
	List<String> anotherStringList = Arrays.asList(anotherString, anotherString);
	LocalDateTime aDate = aDateTime();
	LocalDateTime anotherDate = aDateTime();

	String idReferencedRecordWithAStringAndADateValue;
	String idReferencedRecordWithAnotherDateValue;
	String idReferencedRecordWithoutValue;
	Record referencedRecordWithAStringAndADateValue, referencedRecordWithAnotherDateValue, referencedRecordWithoutValue;

	String idReferencedRecordWithAStringListAndADateListValue;
	String idReferencedRecordWithAnotherStringListValue;
	String idReferencedRecordWithEmptyStringListAndEmptyDateListValue;

	Record referencedRecordWithAStringListAndADateListValue, referencedRecordWithAnotherStringListValue,
			referencedRecordWithEmptyStringListAndEmptyDateListValue;

	Metadata metadataWithCopyDataEntry, referenceMetadata, copiedMetadata;

	@Mock Metadata firstReindexedMetadata, secondReindexedMetadata;
	TransactionRecordsReindexation reindexedMetadata;

	@Mock TaxonomiesManager taxonomiesManager;
	@Mock SystemConfigurationsManager systemConfigurationsManager;

	@Mock SearchServices searchServices;

	@Mock ModelLayerFactory modelLayerFactory;

	@Mock RecordsCaches recordsCache;

	@Mock RecordsCache recordCache;

	@Mock RolesManager rolesManager;

	@Before
	public void setUp() {

		schemas = new TestsSchemasSetup();
		zeSchema = schemas.new ZeSchemaMetadatas();
		anotherSchema = schemas.new AnotherSchemaMetadatas();
		define(schemasManager).using(
				schemas.withTwoMetadatasCopyingAnotherSchemaValuesUsingTwoDifferentReferenceMetadata(false, false, false));

		automaticMetadatas = new HashSet<>();
		automaticMetadatas.add(zeSchema.stringCopiedFromFirstReferenceStringMeta());
		automaticMetadatas.add(zeSchema.dateCopiedFromSecondReferenceDateMeta());

		sortedMetadatas = new ArrayList<>();
		sortedMetadatas.add(zeSchema.stringCopiedFromFirstReferenceStringMeta());
		sortedMetadatas.add(zeSchema.dateCopiedFromSecondReferenceDateMeta());

		when(modelLayerFactory.getModelLayerLogger()).thenReturn(modelLayerLogger);
		when(modelLayerFactory.getRolesManager()).thenReturn(rolesManager);
		when(modelLayerFactory.getMetadataSchemasManager()).thenReturn(schemasManager);
		when(modelLayerFactory.newSearchServices()).thenReturn(searchServices);
		when(modelLayerFactory.getSystemConfigurationsManager()).thenReturn(systemConfigurationsManager);
		when(modelLayerFactory.getTaxonomiesManager()).thenReturn(taxonomiesManager);
		when(modelLayerFactory.getRecordsCaches()).thenReturn(recordsCache);
		when(recordsCache.getCache(anyString())).thenReturn(recordCache);

		services = spy(new RecordAutomaticMetadataServices(modelLayerFactory));

		createOtherSchemaRecordsWithSingleValueMetadata();

		record = spy(new TestRecord(zeSchema));

		recordWithReferenceToRecordWithValue = spy(new TestRecord(zeSchema));
		recordWithReferenceToRecordWithValue.set(zeSchema.firstReferenceToAnotherSchema(),
				idReferencedRecordWithAStringAndADateValue);

		metadataWithCopyDataEntry = zeSchema.stringCopiedFromFirstReferenceStringMeta();
		referenceMetadata = zeSchema.firstReferenceToAnotherSchema();
		copiedMetadata = anotherSchema.stringMetadata();

		reset(schemasManager.getSchemaTypes(zeCollection));

		reindexedMetadata = new TransactionRecordsReindexation(new MetadataList(firstReindexedMetadata, secondReindexedMetadata));
	}

	@Test
	public void whenUpdateAutomaticMetadatasThenGetSortedAutomaticMetadataAndUpdateInCorrectOrder()
			throws Exception {

		Metadata firstMetadata = mock(Metadata.class);
		Metadata secondMetadata = mock(Metadata.class);
		List<Metadata> sortedAutomaticMetadatas = asList(firstMetadata, secondMetadata);

		doNothing().when(services)
				.updateAutomaticMetadata(any(TransactionExecutionContext.class), any(RecordImpl.class), any(RecordProvider.class),
						any(Metadata.class),
						eq(reindexedMetadata), any(MetadataSchemaTypes.class), any(Transaction.class));

		MetadataSchemaTypes types = mock(MetadataSchemaTypes.class);
		MetadataSchema schema = mock(MetadataSchema.class);

		when(schemasManager.getSchemaTypes(zeCollection)).thenReturn(types);
		when(types.getSchemaOf(record)).thenReturn(schema);
		when(schema.getAutomaticMetadatas()).thenReturn(sortedAutomaticMetadatas);

		services.updateAutomaticMetadatas(record, recordProvider, reindexedMetadata, zeTransaction);

		InOrder inOrder = Mockito.inOrder(services);
		inOrder.verify(services)
				.updateAutomaticMetadata(any(TransactionExecutionContext.class), eq(record), eq(recordProvider),
						eq(firstMetadata), eq(reindexedMetadata), eq(types), eq(zeTransaction));
		inOrder.verify(services)
				.updateAutomaticMetadata(any(TransactionExecutionContext.class), eq(record), eq(recordProvider),
						eq(secondMetadata), eq(reindexedMetadata), eq(types), eq(zeTransaction));

	}

	@Test
	public void givenCopiedMetadataWhenUpdateAutomaticMetadataThenSetCopiedValuesInRecords()
			throws Exception {
		TransactionExecutionContext context = new TransactionExecutionContext(mock(Transaction.class));
		Metadata metadata = mock(Metadata.class);
		when(metadata.getDataEntry()).thenReturn(new CopiedDataEntry(aString(), aString()));
		doNothing().when(services).setCopiedValuesInRecords(
				eq(record), eq(metadata), eq(recordProvider), eq(reindexedMetadata), any(RecordUpdateOptions.class));

		services.updateAutomaticMetadata(context, record, recordProvider, metadata, reindexedMetadata, schemas.getTypes(),
				zeTransaction);

		verify(services).setCopiedValuesInRecords(record, metadata, recordProvider, reindexedMetadata, options);

	}

	@Test
	public void givenCalculatedMetadataWhenUpdateAutomaticMetadataThenSetCopiedValuesInRecords()
			throws Exception {
		TransactionExecutionContext context = new TransactionExecutionContext(mock(Transaction.class));
		Metadata metadata = mock(Metadata.class);
		when(metadata.getDataEntry()).thenReturn(new CalculatedDataEntry(mock(MetadataValueCalculator.class)));
		doNothing().when(services).setCalculatedValuesInRecords(eq(context), eq(record), eq(metadata), eq(recordProvider), eq(
				reindexedMetadata), any(MetadataSchemaTypes.class), eq(zeTransaction));

		services.updateAutomaticMetadata(context, record, recordProvider, metadata, reindexedMetadata, schemas.getTypes(),
				zeTransaction);

		verify(services)
				.setCalculatedValuesInRecords(context, record, metadata, recordProvider, reindexedMetadata, schemas.getTypes(),
						zeTransaction);

	}

	@Test
	public void givenRecordWithModifiedReferenceIdWhenSetAutomaticValuesThenCopied()
			throws Exception {

		doNothing().when(services).copyValueInRecord(any(RecordImpl.class), any(Metadata.class), eq(recordProvider),
				any(Metadata.class), any(Metadata.class), any(RecordUpdateOptions.class));
		record.set(zeSchema.firstReferenceToAnotherSchema(), "aNewId");

		services.setCopiedValuesInRecords(record, zeSchema.stringCopiedFromFirstReferenceStringMeta(), recordProvider,
				reindexedMetadata, options);

		verify(services).copyValueInRecord(any(RecordImpl.class), any(Metadata.class), eq(recordProvider), any(Metadata.class),
				any(Metadata.class), any(RecordUpdateOptions.class));
	}

	@Test
	public void givenRecordWithUnmodifiedReferenceIdWhenSetAutomaticValuesThenNotCopied()
			throws Exception {

		doNothing().when(services).copyValueInRecord(any(RecordImpl.class), any(Metadata.class), eq(recordProvider),
				any(Metadata.class), any(Metadata.class), any(RecordUpdateOptions.class));

		services.setCopiedValuesInRecords(record, zeSchema.stringCopiedFromFirstReferenceStringMeta(), recordProvider,
				reindexedMetadata, options);

		verify(services, never()).copyValueInRecord(any(RecordImpl.class), any(Metadata.class), eq(recordProvider),
				any(Metadata.class), any(Metadata.class), any(RecordUpdateOptions.class));
	}

	@Test
	public void givenRecordWithUnmodifiedReferenceIdWhenSetAutomaticValuesWithForcedCopyThenCopied()
			throws Exception {

		doNothing().when(services).copyValueInRecord(any(RecordImpl.class), any(Metadata.class), eq(recordProvider),
				any(Metadata.class), any(Metadata.class), any(RecordUpdateOptions.class));

		services.setCopiedValuesInRecords(record, zeSchema.stringCopiedFromFirstReferenceStringMeta(), recordProvider,
				new TransactionRecordsReindexation(new MetadataList(zeSchema.stringCopiedFromFirstReferenceStringMeta())),
				options);

		verify(services).copyValueInRecord(any(RecordImpl.class), any(Metadata.class), eq(recordProvider), any(Metadata.class),
				any(Metadata.class), any(RecordUpdateOptions.class));
	}

	@Test
	public void givenNoModifiedReferenceMetadataWhenSetAutomaticValuesDoNothing()
			throws Exception {
		when(recordWithReferenceToRecordWithValue.getModifiedValues()).thenReturn(new HashMap<String, Object>());

		services.updateAutomaticMetadatas(recordWithReferenceToRecordWithValue, recordProvider, reindexedMetadata, zeTransaction);

		assertThat(recordWithReferenceToRecordWithValue.getModifiedValues().isEmpty()).isTrue();
	}

	@Test
	public void whenSetAutomaticValuesInRecordsThenGetCopiedMetadataAndReferencedMetadataUsedToCopy()
			throws Exception {

		services.setCopiedValuesInRecords(recordWithReferenceToRecordWithValue,
				zeSchema.stringCopiedFromFirstReferenceStringMeta(), recordProvider, reindexedMetadata, options);

		verify(schemasManager.getSchemaTypes(zeCollection)).getMetadata(zeSchema.firstReferenceToAnotherSchemaCompleteCode());
		verify(schemasManager.getSchemaTypes(zeCollection)).getMetadata(anotherSchema.stringMetadataCompleteCode);

	}

	@Test
	public void whenSetAutomaticValuesInRecordsThenObtainReferencedRecordAndSetCopiedValueToRecord()
			throws Exception {

		services.setCopiedValuesInRecords(recordWithReferenceToRecordWithValue,
				zeSchema.stringCopiedFromFirstReferenceStringMeta(), recordProvider, reindexedMetadata, options);

		InOrder inOrder = Mockito.inOrder(recordWithReferenceToRecordWithValue,
				referencedRecordWithAStringAndADateValue);
		inOrder.verify(recordWithReferenceToRecordWithValue).get(zeSchema.firstReferenceToAnotherSchema());
		inOrder.verify(referencedRecordWithAStringAndADateValue).get(anotherSchema.stringMetadata());
		inOrder.verify(recordWithReferenceToRecordWithValue).updateAutomaticValue(
				zeSchema.stringCopiedFromFirstReferenceStringMeta(), aString);
	}

	@Test
	public void givenModifiedReferenceMetadataWhenSetAutomaticValuesThenCopyValueInRecord()
			throws Exception {
		Map<String, Object> mapWithModifiedValue = new HashMap<>();
		mapWithModifiedValue.put(zeSchema.firstReferenceToAnotherSchema().getDataStoreCode(), "value");
		when(recordWithReferenceToRecordWithValue.getModifiedValues()).thenReturn(mapWithModifiedValue);

		services.setCopiedValuesInRecords(recordWithReferenceToRecordWithValue, metadataWithCopyDataEntry, recordProvider,
				reindexedMetadata, options);

		verify(services).copyValueInRecord(recordWithReferenceToRecordWithValue, metadataWithCopyDataEntry, recordProvider,
				referenceMetadata, copiedMetadata, options);
	}

	@Test
	public void givenRecordWithNullReferenceWhenSetAutomaticValuesInRecordsThenSetCopiedValueToNull()
			throws Exception {
		services.copyValueInRecord(record, metadataWithCopyDataEntry, recordProvider, referenceMetadata, copiedMetadata, options);

		verify(record).updateAutomaticValue(metadataWithCopyDataEntry, null);
	}

	@Test
	public void givenRecordWithReferenceWhenSetAutomaticValuesInRecordsThenCallCopyReferenceValueInRecord()
			throws Exception {
		doNothing().when(services).copyReferenceValueInRecord(record, metadataWithCopyDataEntry, recordProvider, copiedMetadata,
				"value", referenceMetadata, options);
		record.set(referenceMetadata, "value");

		services.copyValueInRecord(record, metadataWithCopyDataEntry, recordProvider, referenceMetadata, copiedMetadata, options);

		verify(services).copyReferenceValueInRecord(record, metadataWithCopyDataEntry, recordProvider, copiedMetadata, "value"
				, referenceMetadata, options);
	}

	@Test
	public void givenRecordWithNullReferenceListWhenSetAutomaticValuesInRecordsThenSetCopiedValueToEmptyList()
			throws Exception {
		givenMultivalueMetadataWithCopiedDataEntry();
		givenMultivalueReferences();

		record.set(referenceMetadata, null);

		services.copyValueInRecord(record, metadataWithCopyDataEntry, recordProvider, referenceMetadata, copiedMetadata, options);

		verify(record).updateAutomaticValue(metadataWithCopyDataEntry, Collections.emptyList());
	}

	@Test
	public void givenRecordWithEmptyReferenceListWhenSetAutomaticValuesInRecordsThenSetCopiedValueToEmptyList()
			throws Exception {
		givenMultivalueMetadataWithCopiedDataEntry();
		givenMultivalueReferences();

		record.set(referenceMetadata, new ArrayList<>());

		services.copyValueInRecord(record, metadataWithCopyDataEntry, recordProvider, referenceMetadata, copiedMetadata, options);

		verify(record).updateAutomaticValue(metadataWithCopyDataEntry, Collections.emptyList());
	}

	@Test
	public void givenRecordWithReferenceListWhenSetAutomaticValuesInRecordsThenSetCopiedValuesList()
			throws Exception {
		givenMultivalueMetadataWithCopiedDataEntry();
		givenMultivalueReferences();

		record.set(referenceMetadata, new ArrayList<>());

		services.copyValueInRecord(record, metadataWithCopyDataEntry, recordProvider, referenceMetadata, copiedMetadata, options);

		verify(record).updateAutomaticValue(metadataWithCopyDataEntry, Collections.emptyList());
	}

	@Test
	public void givenMultivalueReferencesWhenSetAutomaticValuesInRecordsThenObtainReferencedRecordsAndSetCopiedValueToRecord()
			throws Exception {
		givenMultivalueMetadataWithCopiedDataEntry();
		givenMultivalueReferences();
		record.set(referenceMetadata,
				Arrays.asList(idReferencedRecordWithAStringAndADateValue, idReferencedRecordWithAStringAndADateValue));

		services.copyValueInRecord(record, metadataWithCopyDataEntry, recordProvider, referenceMetadata, copiedMetadata, options);

		InOrder inOrder = Mockito.inOrder(recordProvider, record, referencedRecordWithAStringAndADateValue);
		inOrder.verify(record).getList(referenceMetadata);
		inOrder.verify(recordProvider).getRecord(idReferencedRecordWithAStringAndADateValue);
		inOrder.verify(referencedRecordWithAStringAndADateValue).get(copiedMetadata);
		inOrder.verify(recordProvider).getRecord(idReferencedRecordWithAStringAndADateValue);
		inOrder.verify(referencedRecordWithAStringAndADateValue).get(copiedMetadata);
		inOrder.verify(record).updateAutomaticValue(metadataWithCopyDataEntry, Arrays.asList(aString, aString));
	}

	@Test
	public void givenMultivalueReferencesAndCopiedValuesWhenSetAutomaticValuesInRecordsThenObtainReferencedRecordsAndSetCopiedValuesToRecord()
			throws Exception {
		givenMultivalueMetadataWithCopiedDataEntry();
		givenMultivalueReferences();
		givenMultivalueCopiedMetadata();
		createOtherSchemaRecordsWithMultivalueMetadata();
		record.set(referenceMetadata,
				Arrays.asList(idReferencedRecordWithAStringListAndADateListValue, idReferencedRecordWithAnotherStringListValue));

		services.copyValueInRecord(record, metadataWithCopyDataEntry, recordProvider, referenceMetadata, copiedMetadata, options);

		InOrder inOrder = Mockito.inOrder(recordProvider, record, referencedRecordWithAStringListAndADateListValue,
				referencedRecordWithAnotherStringListValue);
		inOrder.verify(record).getList(referenceMetadata);

		inOrder.verify(recordProvider).getRecord(idReferencedRecordWithAStringListAndADateListValue);
		inOrder.verify(referencedRecordWithAStringListAndADateListValue).getList(copiedMetadata);

		inOrder.verify(recordProvider).getRecord(idReferencedRecordWithAnotherStringListValue);
		inOrder.verify(referencedRecordWithAnotherStringListValue).getList(copiedMetadata);

		inOrder.verify(record).updateAutomaticValue(metadataWithCopyDataEntry,
				Arrays.asList(aString, aString, anotherString, anotherString));
	}

	@Test
	public void givenCopiedValuesWhenSetAutomaticValuesInRecordsThenObtainReferencedRecordsAndSetCopiedValuesToRecord()
			throws Exception {
		givenMultivalueMetadataWithCopiedDataEntry();
		givenMultivalueCopiedMetadata();
		createOtherSchemaRecordsWithMultivalueMetadata();
		record.set(referenceMetadata, idReferencedRecordWithAStringListAndADateListValue);

		services.copyValueInRecord(record, metadataWithCopyDataEntry, recordProvider, referenceMetadata, copiedMetadata, options);

		InOrder inOrder = Mockito.inOrder(recordProvider, record, referencedRecordWithAStringListAndADateListValue);
		inOrder.verify(record, times(2)).get(referenceMetadata);

		inOrder.verify(recordProvider).getRecord(idReferencedRecordWithAStringListAndADateListValue);
		inOrder.verify(referencedRecordWithAStringListAndADateListValue).get(copiedMetadata);

		inOrder.verify(record).updateAutomaticValue(metadataWithCopyDataEntry, Arrays.asList(aString, aString));
	}

	@Test
	public void whenSortingByDependenciesThenFirstElementHasNoCopyDependencies()
			throws Exception {
		Metadata metadataAWithoutAutomaticDependency = mock(Metadata.class);
		when(metadataAWithoutAutomaticDependency.getLocalCode()).thenReturn("a");

		Metadata metadataBWithAutomaticDependencyToA = mock(Metadata.class);
		when(metadataBWithAutomaticDependencyToA.getLocalCode()).thenReturn("b");

		Metadata metadataCWithAutomaticDependencyToAandB = mock(Metadata.class);
		when(metadataCWithAutomaticDependencyToAandB.getLocalCode()).thenReturn("c");

		Map<Metadata, Set<String>> metadatasWithLocalDependencies = new HashMap<>();
		metadatasWithLocalDependencies.put(metadataAWithoutAutomaticDependency, new HashSet<String>());
		metadatasWithLocalDependencies.put(metadataBWithAutomaticDependencyToA, asSet("a"));
		metadatasWithLocalDependencies.put(metadataCWithAutomaticDependencyToAandB, asSet("a", "b"));

		List<Metadata> sortedMetadata = services.sortMetadatasUsingLocalDependencies(metadatasWithLocalDependencies);

		assertThat(sortedMetadata).containsExactly(metadataAWithoutAutomaticDependency, metadataBWithAutomaticDependencyToA,
				metadataCWithAutomaticDependencyToAandB);

	}

	@Test(expected = ImpossibleRuntimeException.class)
	public void givenAllMetadataWithAutomaticDependencyhenSortingByDependenciesThenImpossibleException()
			throws Exception {
		Metadata metadataAWithAutomaticDependencyToB = mock(Metadata.class);
		when(metadataAWithAutomaticDependencyToB.getCode()).thenReturn("a");

		Metadata metadataBWithAutomaticDependencyToA = mock(Metadata.class);
		when(metadataBWithAutomaticDependencyToA.getCode()).thenReturn("b");

		Map<Metadata, Set<String>> metadatasWithLocalDependencies = new HashMap<>();
		metadatasWithLocalDependencies.put(metadataAWithAutomaticDependencyToB, asSet("b"));
		metadatasWithLocalDependencies.put(metadataBWithAutomaticDependencyToA, asSet("a"));

		services.sortMetadatasUsingLocalDependencies(metadatasWithLocalDependencies);

	}

	private void createOtherSchemaRecordsWithSingleValueMetadata() {
		referencedRecordWithAStringAndADateValue = spy(
				new TestRecord(anotherSchema, "referencedRecordWithAStringAndADateValue"));
		idReferencedRecordWithAStringAndADateValue = referencedRecordWithAStringAndADateValue.getId();
		referencedRecordWithAStringAndADateValue.set(anotherSchema.stringMetadata(), aString);
		referencedRecordWithAStringAndADateValue.set(anotherSchema.dateMetadata(), aDate);

		referencedRecordWithAnotherDateValue = spy(new TestRecord(anotherSchema));
		idReferencedRecordWithAnotherDateValue = referencedRecordWithAnotherDateValue.getId();
		referencedRecordWithAnotherDateValue.set(anotherSchema.dateMetadata(), anotherDate);

		referencedRecordWithoutValue = spy(new TestRecord(anotherSchema));
		idReferencedRecordWithoutValue = referencedRecordWithoutValue.getId();
		when(recordProvider.getRecord(idReferencedRecordWithAStringAndADateValue)).thenReturn(
				referencedRecordWithAStringAndADateValue);
		when(recordProvider.getRecord(idReferencedRecordWithAnotherDateValue)).thenReturn(
				referencedRecordWithAnotherDateValue);
		when(recordProvider.getRecord(idReferencedRecordWithoutValue)).thenReturn(referencedRecordWithoutValue);
	}

	private void createOtherSchemaRecordsWithMultivalueMetadata() {
		referencedRecordWithAStringListAndADateListValue = spy(new TestRecord(anotherSchema));
		idReferencedRecordWithAStringListAndADateListValue = referencedRecordWithAStringListAndADateListValue.getId();
		referencedRecordWithAStringListAndADateListValue.set(copiedMetadata, aStringList);

		referencedRecordWithAnotherStringListValue = spy(new TestRecord(anotherSchema));
		idReferencedRecordWithAnotherStringListValue = referencedRecordWithAnotherStringListValue.getId();
		referencedRecordWithAnotherStringListValue.set(copiedMetadata, anotherStringList);

		referencedRecordWithEmptyStringListAndEmptyDateListValue = spy(new TestRecord(anotherSchema));
		idReferencedRecordWithEmptyStringListAndEmptyDateListValue = referencedRecordWithEmptyStringListAndEmptyDateListValue
				.getId();
		referencedRecordWithEmptyStringListAndEmptyDateListValue.set(copiedMetadata, new ArrayList<>());

		when(recordProvider.getRecord(idReferencedRecordWithAStringListAndADateListValue)).thenReturn(
				referencedRecordWithAStringListAndADateListValue);
		when(recordProvider.getRecord(idReferencedRecordWithAnotherStringListValue)).thenReturn(
				referencedRecordWithAnotherStringListValue);
		when(recordProvider.getRecord(idReferencedRecordWithEmptyStringListAndEmptyDateListValue)).thenReturn(
				referencedRecordWithEmptyStringListAndEmptyDateListValue);
	}

	private void givenMultivalueMetadataWithCopiedDataEntry() {
		metadataWithCopyDataEntry = spy(metadataWithCopyDataEntry);
		when(metadataWithCopyDataEntry.isMultivalue()).thenReturn(true);
		when(metadataWithCopyDataEntry.getDataStoreType()).thenReturn("strings");
	}

	private void givenMultivalueReferences() {
		referenceMetadata = spy(referenceMetadata);
		when(referenceMetadata.isMultivalue()).thenReturn(true);
		when(referenceMetadata.getDataStoreType()).thenReturn("strings");
	}

	private void givenMultivalueCopiedMetadata() {
		copiedMetadata = spy(copiedMetadata);
		when(copiedMetadata.isMultivalue()).thenReturn(true);
		when(copiedMetadata.getDataStoreType()).thenReturn("strings");
	}
}
