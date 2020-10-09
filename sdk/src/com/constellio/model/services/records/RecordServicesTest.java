package com.constellio.model.services.records;

import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDTOMode;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.SolrRecordDTO;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.dto.records.TransactionResponseDTO;
import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.dao.services.bigVault.RecordDaoException;
import com.constellio.data.dao.services.bigVault.RecordDaoException.NoSuchRecordWithId;
import com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking;
import com.constellio.data.dao.services.bigVault.RecordDaoRuntimeException.RecordDaoRuntimeException_RecordsFlushingFailed;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.utils.Factory;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordMigrationScript;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.ModificationImpact;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentModifications;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.services.extensions.ModelLayerExtensions;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.migrations.RecordMigrationsManager;
import com.constellio.model.services.migrations.RequiredRecordMigrations;
import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_RecordsFlushingFailed;
import com.constellio.model.services.records.RecordServicesRuntimeException.UnresolvableOptimsiticLockingCausingInfiniteLoops;
import com.constellio.model.services.records.cache.RecordsCache;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.records.extractions.RecordPopulateServices;
import com.constellio.model.services.records.preparation.AggregatedMetadataIncrementation;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.ModificationImpactCalculator;
import com.constellio.model.services.schemas.ModificationImpactCalculatorResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.SolrQueryBuilderContext;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.TestUtils;
import com.constellio.sdk.tests.schemas.FakeDataStoreTypeFactory;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.sdk.tests.TestUtils.asMap;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class RecordServicesTest extends ConstellioTest {

	private final Long theDocumentCount = aLong();
	long theNewVersion = 9L;

	Map<String, Object> dtoValues = asMap("schema_s", (Object) "schematype_default", "collection_s", "zeCollection");
	RecordDTO firstSearchResult = new SolrRecordDTO("1", 1, null, dtoValues, RecordDTOMode.FULLY_LOADED);
	RecordDTO secondSearchResult = new SolrRecordDTO("2", 1, null, dtoValues, RecordDTOMode.FULLY_LOADED);
	List<RecordDTO> theSearchResults = Arrays.asList(firstSearchResult, secondSearchResult);
	RecordDTO recordDTO = new SolrRecordDTO("3", 1, null, dtoValues, RecordDTOMode.FULLY_LOADED);
	@Mock RecordDeltaDTO deltaDTO;
	DataStoreTypesFactory typesFactory = new FakeDataStoreTypeFactory();
	@Mock MetadataSchemasManager schemaManager;
	@Mock RecordDao recordDao;
	@Mock RecordDao eventsDao;
	@Mock RecordDao searchDao;
	@Mock RecordDao notificationsDao;
	@Mock RecordsCaches recordsCaches;
	@Mock RecordsCache recordsCache;
	@Mock RecordValidationServices validationServices;
	@Mock RecordAutomaticMetadataServices automaticMetadataServices;
	@Mock ContentManager contentManager;
	@Mock RecordModificationImpactHandler recordModificationImpactHandler;
	@Mock CollectionsListManager collectionsListManager;
	@Mock RecordPopulateServices recordPopulateServices;
	@Mock Factory<EncryptionServices> encryptionServiceFactory;
	@Mock AuthorizationsServices authorizationServices;
	@Mock RecordMigrationsManager recordMigrationsManager;
	@Mock ConstellioEIMConfigs systemConfigs;
	ModelLayerExtensions extensions = new ModelLayerExtensions();

	long firstVersion = anInteger();
	long secondVersion = anInteger();
	TestRecord record, otherRecord, savedRecord, otherSavedRecord, recordWithATitleAndStringMetadataValue,
			anotherRecordWithATitleAndStringMetadataValue;
	String theFreeTextSearch = aString();
	String theId = aString();
	String anotherSavedDocumentId = aString();
	TestsSchemasSetup schemas;
	ZeSchemaMetadatas zeSchema;
	RecordServicesImpl recordServices;
	String theTitle = aString();
	String theStringMetadata = aString();
	String anotherTitle = aString();
	String anotherStringMetadata = aString();

	@Mock Metadata firstReindexedMetadata, secondReindexedMetadata;
	TransactionRecordsReindexation reindexedMetadata;
	long firstUpdatedRecordVersion = aLong();
	long secondUpdatedRecordVersion = aLong();
	long firstAddedRecordVersion = aLong();
	long secondAddedRecordVersion = aLong();
	String firstUpdatedRecordId = "firstUpdatedRecordId";
	String secondUpdatedRecordId = "secondUpdatedRecordId";
	String firstAddedRecordId = "firstAddedRecordId";
	String secondAddedRecordId = "secondAddedRecordId";

	@Mock ModificationImpact aModificationImpact;
	@Mock ModificationImpact anotherModificationImpact;

	@Mock BatchProcessesManager batchProcessesManager;

	@Mock SearchServices searchServices;

	@Mock ModelLayerFactory modelFactory;

	@Mock Metadata firstMetadataToReindex;
	@Mock Metadata secondMetadataToReindex;
	@Mock Metadata thirdMetadataToReindex;

	@Mock LogicalSearchCondition firstSearchCondition;
	@Mock LogicalSearchCondition secondSearchCondition;

	@Mock RecordImpl firstRecordConditionRecord1;
	@Mock RecordImpl firstRecordConditionRecord2;
	@Mock RecordImpl secondRecordConditionRecord1;
	@Mock RecordImpl secondRecordConditionRecord2;

	@Mock TransactionRecordsReindexation alreadyReindexedMetadata;

	@Mock OptimisticLocking optimisticLockingException;

	@Mock TransactionResponseDTO transactionResponseDTO;

	String firstRecordId = aString();
	String secondRecordId = aString();
	String thirdRecordId = aString();

	long firstRecordVersion = aLong();
	long secondRecordVersion = aLong();
	long thirdRecordVersion = aLong();

	@Mock RecordImpl firstRecord;
	@Mock RecordImpl secondRecord;
	@Mock RecordImpl thirdRecord;

	@Mock RecordImpl newFirstRecordVersion;
	@Mock RecordImpl newSecondRecordVersion;

	@Mock TaxonomiesManager taxonomiesManager;
	@Mock LoggingServices loggingServices;

	@Mock CollectionsManager collectionsManager;

	MetadataSchemaTypes metadataSchemaTypes;
	@Mock MetadataSchemaType metadataSchemaType;
	@Mock MetadataSchema metadataSchema;
	@Mock DataLayerFactory dataLayerFactory;
	@Mock DataLayerConfiguration dataLayerConfiguration;

	@Before
	public void setUp()
			throws Exception {

		schemas = new TestsSchemasSetup();
		zeSchema = schemas.new ZeSchemaMetadatas();

		UniqueIdGenerator uniqueIdGenerator = new UniqueIdGenerator() {

			int i = 0;

			@Override
			public synchronized String next() {
				return "" + (++i);
			}
		};

		doReturn(recordPopulateServices).when(modelFactory).newRecordPopulateServices();

		when(newFirstRecordVersion.getId()).thenReturn(firstRecordId);
		when(newSecondRecordVersion.getId()).thenReturn(secondRecordId);
		when(newFirstRecordVersion.getSchemaCode()).thenReturn(zeSchema.code());
		when(newSecondRecordVersion.getSchemaCode()).thenReturn(zeSchema.code());
		when(newFirstRecordVersion.getCollection()).thenReturn(zeCollection);
		when(newSecondRecordVersion.getCollection()).thenReturn(zeCollection);

		when(firstRecord.getId()).thenReturn(firstRecordId);
		when(secondRecord.getId()).thenReturn(secondRecordId);
		when(thirdRecord.getId()).thenReturn(thirdRecordId);
		when(firstRecord.getCollection()).thenReturn(zeCollection);
		when(secondRecord.getCollection()).thenReturn(zeCollection);
		when(thirdRecord.getCollection()).thenReturn(zeCollection);

		when(firstRecord.getSchemaCode()).thenReturn(zeSchema.code());
		when(secondRecord.getSchemaCode()).thenReturn(zeSchema.code());
		when(thirdRecord.getSchemaCode()).thenReturn(zeSchema.code());
		when(firstRecord.getVersion()).thenReturn(firstRecordVersion);
		when(secondRecord.getVersion()).thenReturn(secondRecordVersion);
		when(thirdRecord.getVersion()).thenReturn(thirdRecordVersion);


		when(dataLayerConfiguration.isCopyingRecordsInSearchCollection()).thenReturn(false);

		when(dataLayerFactory.getDataLayerConfiguration()).thenReturn(dataLayerConfiguration);

		when(modelFactory.getDataLayerFactory()).thenReturn(dataLayerFactory);
		when(modelFactory.getBatchProcessesManager()).thenReturn(batchProcessesManager);
		when(modelFactory.getMetadataSchemasManager()).thenReturn(schemaManager);
		when(modelFactory.newSearchServices()).thenReturn(searchServices);
		when(modelFactory.getTaxonomiesManager()).thenReturn(taxonomiesManager);
		when(modelFactory.getContentManager()).thenReturn(contentManager);
		when(modelFactory.newLoggingServices()).thenReturn(loggingServices);
		when(modelFactory.getExtensions()).thenReturn(extensions);
		when(modelFactory.getRecordMigrationsManager()).thenReturn(recordMigrationsManager);
		when(modelFactory.getSystemConfigs()).thenReturn(systemConfigs);

		when(systemConfigs.getFileExtensionsExcludedFromParsing()).thenReturn(new HashSet<String>());

		when(recordMigrationsManager.getCurrentDataVersion(anyString(), anyString())).thenReturn(0L);
		when(recordMigrationsManager.getRecordMigrationsFor(any(Record.class)))
				.thenReturn(new RequiredRecordMigrations(0L, new ArrayList<RecordMigrationScript>()));

		when(collectionsManager.getCollectionLanguages(zeCollection)).thenReturn(Arrays.asList("fr", "en"));

		recordServices = spy(
				(RecordServicesImpl) new RecordServicesImpl(recordDao, eventsDao, searchDao, notificationsDao, modelFactory, typesFactory,
						uniqueIdGenerator, recordsCaches));
		doNothing().when(recordServices).sleep(anyLong());

		doReturn(validationServices).when(recordServices).newRecordValidationServices(any(RecordProvider.class));
		doReturn(automaticMetadataServices).when(recordServices).newAutomaticMetadataServices();
		define(schemaManager).using(schemas.withATitle().withAStringMetadata());

		record = spy(new TestRecord(zeSchema, "record"));
		otherRecord = spy(new TestRecord(zeSchema, "otherRecord"));

		savedRecord = spy(new TestRecord(zeSchema, "savedRecord"));
		theId = savedRecord.getId();
		savedRecord.refresh(firstVersion, TestUtils.newRecordDTO("savedRecord", zeSchema));

		otherSavedRecord = spy(new TestRecord(zeSchema, "otherSavedRecord"));
		anotherSavedDocumentId = otherSavedRecord.getId();
		otherSavedRecord.refresh(firstVersion, TestUtils.newRecordDTO("otherSavedRecord", zeSchema));

		recordWithATitleAndStringMetadataValue = spy(new TestRecord(zeSchema, "recordWithATitleAndStringMetadataValue"));
		recordWithATitleAndStringMetadataValue.set(zeSchema.title(), theTitle);
		recordWithATitleAndStringMetadataValue.set(zeSchema.stringMetadata(), theStringMetadata);

		anotherRecordWithATitleAndStringMetadataValue = spy(new TestRecord(zeSchema, "recordWithATitleAndStringMetadataValue"));
		anotherRecordWithATitleAndStringMetadataValue.set(zeSchema.title(), anotherTitle);
		anotherRecordWithATitleAndStringMetadataValue.set(zeSchema.stringMetadata(), anotherStringMetadata);

		reindexedMetadata = new TransactionRecordsReindexation(new MetadataList(firstReindexedMetadata, secondReindexedMetadata));

		when(firstRecordConditionRecord1.getSchemaCode()).thenReturn(schemas.anotherDefaultSchemaCode());
		when(firstRecordConditionRecord2.getSchemaCode()).thenReturn(schemas.anotherDefaultSchemaCode());
		when(secondRecordConditionRecord1.getSchemaCode()).thenReturn(schemas.anotherDefaultSchemaCode());
		when(secondRecordConditionRecord2.getSchemaCode()).thenReturn(schemas.anotherDefaultSchemaCode());
		when(firstRecordConditionRecord1.getId()).thenReturn("firstRecordConditionRecord1");
		when(firstRecordConditionRecord2.getId()).thenReturn("firstRecordConditionRecord2");
		when(secondRecordConditionRecord1.getId()).thenReturn("secondRecordConditionRecord1");
		when(secondRecordConditionRecord2.getId()).thenReturn("secondRecordConditionRecord2");
		when(firstRecordConditionRecord1.getCollection()).thenReturn("zeCollection");
		when(firstRecordConditionRecord2.getCollection()).thenReturn("zeCollection");
		when(secondRecordConditionRecord1.getCollection()).thenReturn("zeCollection");
		when(secondRecordConditionRecord2.getCollection()).thenReturn("zeCollection");

		when(firstRecord.getCollection()).thenReturn("zeCollection");
		when(secondRecord.getCollection()).thenReturn("zeCollection");
		when(thirdRecord.getCollection()).thenReturn("zeCollection");

		metadataSchemaTypes = schemaManager.getSchemaTypes(zeCollection);
		when(modelFactory.getCollectionsListManager()).thenReturn(collectionsListManager);
		when(collectionsListManager.getCollectionLanguages(anyString())).thenReturn(asList("fr"));
		when(metadataSchemaType.getDataStore()).thenReturn("records");

		when(metadataSchemaType.getSchema("zeSchemaType_default")).thenReturn(metadataSchema);
		when(metadataSchema.getMetadata(anyString())).thenReturn(mock(Metadata.class));

		when(recordsCaches.getCache(anyString())).thenReturn(recordsCache);
		when(collectionsListManager.getCollectionInfo(zeCollection)).thenReturn(new CollectionInfo((byte) 0, zeCollection, "fr", asList("fr")));
	}

	@Test
	public void whenGettingDocumentsCountTheReturnDaoDocumentsCount() {
		when(recordDao.documentsCount()).thenReturn(theDocumentCount);

		assertThat(recordServices.documentsCount()).isEqualTo(theDocumentCount);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = RecordServicesRuntimeException.NoSuchRecordWithId.class)
	public void givenInexistentIdWhenGetDocumentByIdThenThrowException()
			throws Exception {

		when(recordDao.realGet(theId, true)).thenThrow(RecordDaoException.NoSuchRecordWithId.class);

		recordServices.get(theId);

	}


	@Test
	public void whenAddingRecordThenSaveInTransaction()
			throws Exception {
		ArgumentCaptor<Transaction> transaction = ArgumentCaptor.forClass(Transaction.class);

		when(recordDao.get(theId)).thenReturn(recordDTO);
		doNothing().when(recordServices).execute(any(Transaction.class));

		recordServices.add(record);

		verify(recordServices).execute(transaction.capture());

		assertThat(transaction.getValue().getRecords()).containsOnly(record);
	}

	@Test
	public void whenUpdatingRecordThenSaveInTransaction()
			throws Exception {
		ArgumentCaptor<Transaction> transaction = ArgumentCaptor.forClass(Transaction.class);
		when(recordDao.get(theId)).thenReturn(recordDTO);
		doNothing().when(recordServices).execute(any(Transaction.class));

		RecordUpdateOptions options = mock(RecordUpdateOptions.class);

		record.set(zeSchema.title(), "value");
		recordServices.update(record, options);

		verify(recordServices).execute(transaction.capture());

		assertThat(transaction.getValue().getRecords()).containsOnly(record);
		assertThat(transaction.getValue().getRecordUpdateOptions()).isSameAs(options);
	}

	@Test
	public void whenUpdatingUnsavedRecordThenExecuteInTransactionAnyway()
			throws Exception {
		when(recordDao.get(theId)).thenReturn(recordDTO);
		doNothing().when(recordServices).execute(any(Transaction.class));

		RecordUpdateOptions options = mock(RecordUpdateOptions.class);

		recordServices.update(record, options);

		verify(recordServices).execute(any(Transaction.class));
		verifyZeroInteractions(recordDao);

	}

	@Test
	public void whenUpdatingRecordHandlingImpactsAsyncThenExecuteWithDefaultOptions()
			throws Exception {
		List<BatchProcess> batchProcesses = mock(List.class);
		ArgumentCaptor<Transaction> transaction = ArgumentCaptor.forClass(Transaction.class);
		when(recordDao.get(theId)).thenReturn(recordDTO);
		doReturn(batchProcesses).when(recordServices).executeHandlingImpactsAsync(any(Transaction.class));

		RecordUpdateOptions options = mock(RecordUpdateOptions.class);
		List<BatchProcess> returnedBatchProcesses = recordServices.updateAsync(record, options);

		verify(recordServices).executeHandlingImpactsAsync(transaction.capture());
		assertThat(transaction.getValue().getRecords()).containsOnly(record);
		assertThat(transaction.getValue().getRecordUpdateOptions()).isEqualTo(options);
		assertThat(returnedBatchProcesses).isEqualTo(batchProcesses);

	}

	@Test
	public void whenUpdatingRecordHandlingImpactsAsyncThenExecuteInAsyncTransaction()
			throws Exception {
		List<BatchProcess> batchProcesses = mock(List.class);
		ArgumentCaptor<Transaction> transaction = ArgumentCaptor.forClass(Transaction.class);
		when(recordDao.get(theId)).thenReturn(recordDTO);
		doReturn(batchProcesses).when(recordServices).executeHandlingImpactsAsync(any(Transaction.class));

		List<BatchProcess> returnedBatchProcesses = recordServices.updateAsync(record);

		verify(recordServices).executeHandlingImpactsAsync(transaction.capture());
		assertThat(transaction.getValue().getRecords()).containsOnly(record);
		assertThat(transaction.getValue().getRecordUpdateOptions()).isNotNull();
		assertThat(returnedBatchProcesses).isEqualTo(batchProcesses);

	}

	@Test
	public void whenCreatingTransactionDTOThenAddRecordsAndUpdateRecords()
			throws Exception {
		record.set(zeSchema.stringMetadata(), "recordString");
		record.set(zeSchema.title(), "recordTitle");
		otherRecord.set(zeSchema.stringMetadata(), "otherRecordString");
		otherRecord.set(zeSchema.title(), "otherRecordTitle");
		savedRecord.set(zeSchema.stringMetadata(), "savedRecordString");
		savedRecord.set(zeSchema.title(), "savedRecordTitle");
		otherSavedRecord.set(zeSchema.stringMetadata(), "otherSavedRecordString");
		otherSavedRecord.set(zeSchema.title(), "otherSavedRecordTitle");

		RecordsFlushing recordsFlushing = mock(RecordsFlushing.class);
		Transaction transaction = new Transaction();
		transaction.addUpdate(record);
		transaction.addUpdate(otherRecord);
		transaction.addUpdate(savedRecord);
		transaction.addUpdate(otherSavedRecord);

		Map<String, TransactionDTO> transactionDTOs = recordServices
				.createTransactionDTOs(transaction, transaction.getModifiedRecords());
		TransactionDTO transactionDTO = transactionDTOs.get("records");

		assertThat(transactionDTO.getNewRecords()).hasSize(2);
		assertThat(transactionDTO.getModifiedRecords()).hasSize(2);
		RecordDTO firstRecordDTO = transactionDTO.getNewRecords().get(0);
		RecordDTO secondRecordDTO = transactionDTO.getNewRecords().get(1);
		RecordDeltaDTO firstDeltaRecordDTO = transactionDTO.getModifiedRecords().get(1);
		RecordDeltaDTO secondDeltaRecordDTO = transactionDTO.getModifiedRecords().get(0);
		assertThat(firstRecordDTO.getFields()).containsEntry(zeSchema.stringMetadata().getDataStoreCode(), "recordString");
		assertThat(firstRecordDTO.getFields()).containsEntry(zeSchema.title().getDataStoreCode(), "recordTitle");
		assertThat(secondRecordDTO.getFields()).containsEntry(zeSchema.stringMetadata().getDataStoreCode(), "otherRecordString");
		assertThat(secondRecordDTO.getFields()).containsEntry(zeSchema.title().getDataStoreCode(), "otherRecordTitle");

		assertThat(firstDeltaRecordDTO.getModifiedFields()).containsEntry(zeSchema.stringMetadata().getDataStoreCode(),
				"savedRecordString");
		assertThat(firstDeltaRecordDTO.getModifiedFields())
				.containsEntry(zeSchema.title().getDataStoreCode(), "savedRecordTitle");
		assertThat(secondDeltaRecordDTO.getModifiedFields()).containsEntry(zeSchema.stringMetadata().getDataStoreCode(),
				"otherSavedRecordString");
		assertThat(secondDeltaRecordDTO.getModifiedFields()).containsEntry(zeSchema.title().getDataStoreCode(),
				"otherSavedRecordTitle");
	}

	@Test
	public void whenCreatingTransactionDTOThenDotNotUpdateRecordsWithoutModifications()
			throws Exception {
		record.set(zeSchema.stringMetadata(), "recordString");
		record.set(zeSchema.title(), "recordTitle");

		Transaction transaction = new Transaction();
		transaction.addUpdate(record);
		transaction.addUpdate(savedRecord);
		transaction.addUpdate(otherSavedRecord);

		Map<String, TransactionDTO> transactionDTOs = recordServices
				.createTransactionDTOs(transaction, transaction.getModifiedRecords());
		TransactionDTO transactionDTO = transactionDTOs.get("records");

		assertThat(transactionDTO.getNewRecords()).hasSize(1);
		assertThat(transactionDTO.getModifiedRecords()).hasSize(0);
		RecordDTO firstRecordDTO = transactionDTO.getNewRecords().get(0);
		assertThat(firstRecordDTO.getFields()).containsEntry(zeSchema.stringMetadata().getDataStoreCode(), "recordString");
		assertThat(firstRecordDTO.getFields()).containsEntry(zeSchema.title().getDataStoreCode(), "recordTitle");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void whenExecutingTransactionThenPrepareRecordsAndAddThemInATransaction()
			throws Exception {

		RecordsFlushing recordsFlushing = mock(RecordsFlushing.class);
		TransactionDTO transactionDTO = mock(TransactionDTO.class);
		Transaction transaction = new Transaction();
		transaction.addUpdate(record);
		transaction.addUpdate(otherRecord);
		transaction.addUpdate(savedRecord);
		transaction.addUpdate(otherSavedRecord);
		transaction.setRecordFlushing(recordsFlushing);
		doReturn(asMap("records", transactionDTO)).when(recordServices).createTransactionDTOs(eq(transaction), anyList());
		doReturn(transactionResponseDTO).when(recordDao).execute(transactionDTO);
		doNothing().when(recordServices).refreshRecordsAndCaches(eq(zeCollection), anyList(), anySet(), anyList(),
				any(TransactionResponseDTO.class), any(MetadataSchemaTypes.class), any(RecordProvider.class));

		recordServices.execute(transaction);

		verify(recordDao).execute(transactionDTO);
		verify(recordServices).prepareRecords(transaction);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void givenOptimisticLockingExceptionWhenExecutingTransactionThenHandleIt()
			throws Exception {

		RecordsFlushing recordsFlushing = mock(RecordsFlushing.class);
		TransactionDTO transactionDTO = mock(TransactionDTO.class);
		Transaction transaction = new Transaction();
		transaction.addUpdate(record);
		transaction.addUpdate(otherRecord);
		transaction.addUpdate(savedRecord);
		transaction.addUpdate(otherSavedRecord);
		transaction.setRecordFlushing(recordsFlushing);
		doReturn(asMap("records", transactionDTO)).when(recordServices)
				.createTransactionDTOs(eq(transaction), anyList());
		doNothing().when(recordServices).refreshRecordsAndCaches(eq(zeCollection), anyList(), anySet(), anyList(),
				any(TransactionResponseDTO.class), any(MetadataSchemaTypes.class), any(RecordProvider.class));
		doNothing().when(recordServices).handleOptimisticLocking(any(TransactionDTO.class), any(Transaction.class),
				any(RecordModificationImpactHandler.class), any(OptimisticLocking.class), anyInt());
		RecordDaoException.OptimisticLocking exception = mock(RecordDaoException.OptimisticLocking.class);
		doThrow(exception).when(recordDao).execute(transactionDTO);

		recordServices.execute(transaction);

		verify(recordServices)
				.handleOptimisticLocking(any(TransactionDTO.class), eq(transaction),
						isNull(RecordModificationImpactHandler.class), eq(exception), anyInt());

	}

	@Test
	public void whenHandlingOptimisticLockingWithExceptionThenThrowException()
			throws Exception {

		Transaction transaction = new Transaction();
		transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);

		try {
			recordServices
					.handleOptimisticLocking(mock(TransactionDTO.class), transaction, recordModificationImpactHandler,
							optimisticLockingException, 0);
			fail("Exception expected");
		} catch (RecordServicesException.OptimisticLocking e) {
			// OK
		}
		verify(recordServices)
				.handleOptimisticLocking(any(TransactionDTO.class), eq(transaction), eq(recordModificationImpactHandler),
						eq(optimisticLockingException), anyInt());
	}

	@Test
	public void whenHandlingOptimisticLockingKeepingOlderThenDoNothing()
			throws Exception {

		Transaction transaction = new Transaction();
		transaction.setOptimisticLockingResolution(OptimisticLockingResolution.KEEP_OLDER);
		transaction.add(record);

		recordServices.handleOptimisticLocking(mock(TransactionDTO.class), transaction, recordModificationImpactHandler,
				optimisticLockingException, 0);

		verify(recordServices)
				.handleOptimisticLocking(any(TransactionDTO.class), eq(transaction), eq(recordModificationImpactHandler),
						eq(optimisticLockingException), anyInt());
		verifyZeroInteractions(recordDao);
	}

	@Test
	public void givenOptimisticLockingInAsyncTransactionWhenHandlingOptimisticLockingWithMergeThenMergeAndExecuteNewTransactionAsync()
			throws Exception {

		Transaction transaction = new Transaction();
		transaction.setOptimisticLockingResolution(OptimisticLockingResolution.TRY_MERGE);
		transaction.add(record);
		doNothing().when(recordServices).refreshRecordsAndCaches(anyString(), anyList(), anySet(), anyList(),
				any(TransactionResponseDTO.class), any(MetadataSchemaTypes.class), any(RecordProvider.class));

		doNothing().when(recordServices).mergeRecords(eq(transaction), any(OptimisticLocking.class));
		doNothing().when(recordServices).executeWithImpactHandler(any(Transaction.class),
				any(RecordModificationImpactHandler.class));

		recordServices.handleOptimisticLocking(mock(TransactionDTO.class), transaction, recordModificationImpactHandler,
				optimisticLockingException, 3);

		InOrder inOrder = inOrder(recordServices);
		inOrder.verify(recordServices).mergeRecords(eq(transaction), any(OptimisticLocking.class));
		inOrder.verify(recordServices).executeWithImpactHandler(transaction, recordModificationImpactHandler, false, 4);
	}

	@Test
	public void givenOptimisticLockingInTransactionWhenHandlingOptimisticLockingWithMergeThenMergeAndExecuteNewTransaction()
			throws Exception {

		Transaction transaction = new Transaction();
		transaction.setOptimisticLockingResolution(OptimisticLockingResolution.TRY_MERGE);
		transaction.add(record);
		doNothing().when(recordServices).refreshRecordsAndCaches(anyString(), anyList(), anySet(), anyList(),
				any(TransactionResponseDTO.class), any(MetadataSchemaTypes.class), any(RecordProvider.class));

		doNothing().when(recordServices).mergeRecords(any(Transaction.class), any(OptimisticLocking.class));

		doNothing().when(recordServices).execute(any(Transaction.class));

		recordServices
				.handleOptimisticLocking(mock(TransactionDTO.class), transaction, null, optimisticLockingException, 2);

		InOrder inOrder = inOrder(recordServices);
		inOrder.verify(recordServices).mergeRecords(eq(transaction), any(OptimisticLocking.class));
		inOrder.verify(recordServices).execute(transaction, 3);
	}

	//@Test
	public void whenMergingThenGetListOfModifiedDocumentsAndMergeEachDocument()
			throws Exception {

		ArgumentCaptor<LogicalSearchQuery> query = ArgumentCaptor.forClass(LogicalSearchQuery.class);

		when(firstRecord.isSaved()).thenReturn(true);
		when(secondRecord.isSaved()).thenReturn(true);
		when(thirdRecord.isSaved()).thenReturn(true);

		Transaction transaction = new Transaction();
		transaction.addUpdate(asList((Record) firstRecord, secondRecord, thirdRecord));

		List<Record> modifiedRecords = Arrays.asList((Record) newFirstRecordVersion, newSecondRecordVersion);

		when(searchServices.search(query.capture())).thenReturn(modifiedRecords);

		OptimisticLocking optimisticLocking = mock(OptimisticLocking.class);
		when(optimisticLocking.getId()).thenReturn("zeId");
		recordServices.mergeRecords(transaction, optimisticLocking);

		verify(firstRecord).merge(eq(newFirstRecordVersion), any(MetadataSchema.class));
		verify(secondRecord).merge(eq(newSecondRecordVersion), any(MetadataSchema.class));
		verify(thirdRecord, never()).merge(any(RecordImpl.class), any(MetadataSchema.class));

		LogicalSearchCondition condition = query.getValue().getCondition();
		LogicalSearchCondition firstRecordCondition = LogicalSearchQueryOperators.where(Schemas.IDENTIFIER).is(firstRecordId)
				.andWhere(Schemas.VERSION).isNotEqual(firstRecordVersion);
		LogicalSearchCondition secondRecordCondition = LogicalSearchQueryOperators.where(Schemas.IDENTIFIER).is(secondRecordId)
				.andWhere(Schemas.VERSION).isNotEqual(secondRecordVersion);
		LogicalSearchCondition thirdRecordCondition = LogicalSearchQueryOperators.where(Schemas.IDENTIFIER).is(thirdRecordId)
				.andWhere(Schemas.VERSION).isNotEqual(thirdRecordVersion);

		SolrQueryBuilderContext params = new SolrQueryBuilderContext(false, new ArrayList<>(), null, null, null, null);
		assertThat(condition.getSolrQuery(params)).isEqualTo(
				LogicalSearchQueryOperators.fromAllSchemasIn(condition.getCollection())
						.whereAnyCondition(Arrays.asList(firstRecordCondition, secondRecordCondition, thirdRecordCondition))
						.getSolrQuery(params));
	}

	@Test
	public void whenRefreshRecordsThenRefreshRecords()
			throws Exception {

		when(transactionResponseDTO.getNewDocumentVersion(firstAddedRecordId)).thenReturn(firstAddedRecordVersion);
		when(transactionResponseDTO.getNewDocumentVersion(secondAddedRecordId)).thenReturn(secondAddedRecordVersion);
		when(transactionResponseDTO.getNewDocumentVersion(firstUpdatedRecordId)).thenReturn(firstUpdatedRecordVersion);
		when(transactionResponseDTO.getNewDocumentVersion(secondUpdatedRecordId)).thenReturn(secondUpdatedRecordVersion);

		RecordImpl firstUpdatedRecord = spy(new TestRecord(zeSchema, firstUpdatedRecordId));
		RecordImpl firstAddedRecord = spy(new TestRecord(zeSchema, firstAddedRecordId));
		RecordImpl secondAddedRecord = spy(new TestRecord(zeSchema, secondAddedRecordId));
		RecordImpl secondUpdatedRecord = spy(new TestRecord(zeSchema, secondUpdatedRecordId));

		List<Record> records = asList((Record) firstUpdatedRecord, firstAddedRecord, secondAddedRecord, secondUpdatedRecord);
		Set<String> idMarkedForReindexing = TestUtils.asSet("idNotInCache");

		recordServices.refreshRecordsAndCaches(zeCollection, records, idMarkedForReindexing, new ArrayList<AggregatedMetadataIncrementation>(),
				transactionResponseDTO, metadataSchemaTypes, new RecordProvider(recordServices));

		verify(firstAddedRecord).markAsSaved(firstAddedRecordVersion, zeSchema.instance());
		verify(secondAddedRecord).markAsSaved(secondAddedRecordVersion, zeSchema.instance());
		verify(firstUpdatedRecord).markAsSaved(firstUpdatedRecordVersion, zeSchema.instance());
		verify(secondUpdatedRecord).markAsSaved(secondUpdatedRecordVersion, zeSchema.instance());

	}

	private Record recordWithIdAndDTO(String id, RecordDTO dto) {
		RecordImpl record = mock(RecordImpl.class, id);
		when(record.getId()).thenReturn(id);
		when(record.getRecordDTO()).thenReturn(dto);
		return record;
	}

	@Test
	public void givenRecordNotDirtyUpdatedInTransactionThenNotSaved()
			throws Exception {

		RecordImpl zeRecord = spy(new TestRecord(zeSchema));
		when(zeRecord.getId()).thenReturn("anId");
		when(zeRecord.isDirty()).thenReturn(false);

		Transaction transaction = new Transaction();
		transaction.update(zeRecord);
		verifyZeroInteractions(recordDao);
	}

	@Test
	public void givenNoModificationImpactHandlerDefinedWhenExecutingTransactionUpdatingRecordWithModificationImpactThenException()
			throws Exception {

		AddToBatchProcessImpactHandler defaultHandler = mock(AddToBatchProcessImpactHandler.class);

		RecordImpl zeRecord = spy(new TestRecord(zeSchema));
		when(zeRecord.getId()).thenReturn("anId");
		when(zeRecord.isDirty()).thenReturn(true);
		doNothing().when(recordServices).refreshRecordsAndCaches(eq(zeCollection), anyList(), anySet(), anyList(),
				any(TransactionResponseDTO.class), any(MetadataSchemaTypes.class), any(RecordProvider.class));
		Transaction transaction = new Transaction();
		transaction.update(zeRecord);
		transaction.getRecordUpdateOptions().setForcedReindexationOfMetadatas(alreadyReindexedMetadata);
		ModificationImpactCalculatorResponse response = new ModificationImpactCalculatorResponse(
				asList(aModificationImpact, anotherModificationImpact), new ArrayList<>());
		doReturn(response).when(recordServices).calculateImpactOfModification(
				transaction, taxonomiesManager, searchServices, metadataSchemaTypes, true);
		doReturn(defaultHandler).when(recordServices).addToBatchProcessModificationImpactHandler();
		when(aModificationImpact.getMarkForReindexingInsteadOfBatchProcess()).thenReturn(null);
		when(anotherModificationImpact.getMarkForReindexingInsteadOfBatchProcess()).thenReturn(null);

		recordServices.executeHandlingImpactsAsync(transaction);

		verify(defaultHandler).prepareToHandle(aModificationImpact);
		verify(defaultHandler).prepareToHandle(anotherModificationImpact);
		verify(defaultHandler).handle();
	}

	@Test
	public void givenAsyncModificationImpactWhenUpdatingCachedRecordWithModificationImpactWithIdsThenIdsMarkedForReindexing()
			throws Exception {

		AddToBatchProcessImpactHandler defaultHandler = mock(AddToBatchProcessImpactHandler.class);

		RecordImpl zeRecord = spy(new TestRecord(zeSchema));
		when(zeRecord.getId()).thenReturn("anId");
		when(zeRecord.isDirty()).thenReturn(true);
		doNothing().when(recordServices).refreshRecordsAndCaches(eq(zeCollection), anyList(), anySet(), anyList(),
				any(TransactionResponseDTO.class), any(MetadataSchemaTypes.class), any(RecordProvider.class));
		Transaction transaction = new Transaction();
		transaction.update(zeRecord);
		transaction.getRecordUpdateOptions().setForcedReindexationOfMetadatas(alreadyReindexedMetadata);
		ModificationImpactCalculatorResponse response = new ModificationImpactCalculatorResponse(
				asList(aModificationImpact, anotherModificationImpact), new ArrayList<>());
		doReturn(response).when(recordServices).calculateImpactOfModification(
				transaction, taxonomiesManager, searchServices, metadataSchemaTypes, true);
		doReturn(defaultHandler).when(recordServices).addToBatchProcessModificationImpactHandler();
		when(metadataSchemaType.getCacheType()).thenReturn(RecordCacheType.FULLY_CACHED);
		when(aModificationImpact.getMarkForReindexingInsteadOfBatchProcess()).thenReturn(TestUtils.asSet("idToReindex1"));
		when(anotherModificationImpact.getMarkForReindexingInsteadOfBatchProcess()).thenReturn(TestUtils.asSet("idToReindex2"));

		recordServices.executeHandlingImpactsAsync(transaction);

		verify(defaultHandler).handle();
		verify(defaultHandler).getAllCreatedBatchProcesses();
		verifyZeroInteractions(defaultHandler);
		ArgumentCaptor<Transaction> newTransaction = ArgumentCaptor.forClass(Transaction.class);
		verify(recordServices).saveContentsAndRecords(newTransaction.capture(), eq(defaultHandler), eq(0));
		assertThat(newTransaction.getValue().getIdsToReindex()).containsOnly("idToReindex1", "idToReindex2");
	}


	@Test
	public void whenExecutingWithImpactHandlerATransactionWithModificationImpactThenImpactHandledAfterTransaction()
			throws Exception {

		RecordsFlushing recordsFlushing = mock(RecordsFlushing.class);
		RecordImpl zeRecord = spy(new TestRecord(zeSchema));
		when(zeRecord.getId()).thenReturn("anId");
		when(zeRecord.isDirty()).thenReturn(true);

		Transaction transaction = new Transaction();
		transaction.getRecordUpdateOptions().setForcedReindexationOfMetadatas(alreadyReindexedMetadata);
		transaction.update(zeRecord);
		transaction.setRecordFlushing(recordsFlushing);

		doNothing().when(recordServices).refreshRecordsAndCaches(eq(zeCollection), anyList(), anySet(), anyList(),
				any(TransactionResponseDTO.class), any(MetadataSchemaTypes.class), any(RecordProvider.class));
		ModificationImpactCalculatorResponse response = new ModificationImpactCalculatorResponse(
				asList(aModificationImpact, anotherModificationImpact), new ArrayList<String>());
		doReturn(response).when(recordServices)
				.calculateImpactOfModification(transaction, taxonomiesManager, searchServices, metadataSchemaTypes, true);
		RecordModificationImpactHandler handler = mock(RecordModificationImpactHandler.class);

		TransactionDTO transactionDTO = mock(TransactionDTO.class);
		doReturn(asMap("records", transactionDTO)).when(recordServices)
				.createTransactionDTOs(any(Transaction.class), anyList());
		when(aModificationImpact.getMarkForReindexingInsteadOfBatchProcess()).thenReturn(null);
		when(anotherModificationImpact.getMarkForReindexingInsteadOfBatchProcess()).thenReturn(null);

		recordServices.executeWithImpactHandler(transaction, handler);

		InOrder inOrder = inOrder(recordDao, handler);
		inOrder.verify(handler).prepareToHandle(aModificationImpact);
		inOrder.verify(handler).prepareToHandle(anotherModificationImpact);
		inOrder.verify(recordDao).execute(transactionDTO);
		inOrder.verify(handler).handle();
	}

	@Test
	public void whenExecutingTransactionAndUpdatedRecordHasModificationImpactThenExecuteWithImpactedRecordsInNewTransaction()
			throws RecordServicesException {
		ArgumentCaptor<Transaction> nestedTransaction = ArgumentCaptor.forClass(Transaction.class);
		when(aModificationImpact.getMetadataToReindex()).thenReturn(asList(firstReindexedMetadata, secondReindexedMetadata));
		when(aModificationImpact.getLogicalSearchCondition()).thenReturn(firstSearchCondition);
		when(anotherModificationImpact.getMetadataToReindex()).thenReturn(asList(firstReindexedMetadata, thirdMetadataToReindex));
		when(anotherModificationImpact.getLogicalSearchCondition()).thenReturn(secondSearchCondition);

		when(searchServices.search(new LogicalSearchQuery(firstSearchCondition))).thenReturn(
				asList((Record) firstRecordConditionRecord1, firstRecordConditionRecord2));
		when(searchServices.search(new LogicalSearchQuery(secondSearchCondition))).thenReturn(
				asList((Record) secondRecordConditionRecord1, secondRecordConditionRecord2));

		RecordImpl zeRecord = spy(new TestRecord(zeSchema));
		when(zeRecord.getId()).thenReturn("anId");
		when(zeRecord.isDirty()).thenReturn(true);

		Transaction transaction = spy(new Transaction());
		transaction.update(zeRecord);

		ModificationImpactCalculatorResponse response = new ModificationImpactCalculatorResponse(
				asList(aModificationImpact, anotherModificationImpact), new ArrayList<String>());
		doReturn(response).when(recordServices).getModificationImpacts(transaction, false);
		doNothing().when(recordServices).refreshRecordsAndCaches(eq(zeCollection), anyList(), anySet(), anyList(),
				any(TransactionResponseDTO.class), any(MetadataSchemaTypes.class), any(RecordProvider.class));
		doNothing().when(recordServices).prepareRecords(any(Transaction.class));
		doNothing().when(recordServices).saveContentsAndRecords(any(Transaction.class),
				any(RecordModificationImpactHandler.class), anyInt());
		when(aModificationImpact.getMarkForReindexingInsteadOfBatchProcess()).thenReturn(null);
		when(anotherModificationImpact.getMarkForReindexingInsteadOfBatchProcess()).thenReturn(null);
		recordServices.execute(transaction);

		InOrder inOrder = inOrder(recordServices, transaction);
		inOrder.verify(recordServices).execute(transaction);
		inOrder.verify(transaction).sortRecords(schemaManager.getSchemaTypes(zeCollection));
		inOrder.verify(recordServices).execute(nestedTransaction.capture(), anyInt());
		inOrder.verify(recordServices).saveContentsAndRecords(eq(nestedTransaction.getValue()),
				isNull(RecordModificationImpactHandler.class), anyInt());


		verify(recordServices, never()).saveContentsAndRecords(eq(transaction),
				isNull(RecordModificationImpactHandler.class), anyInt());
		assertThat(nestedTransaction.getValue().getRecords()).containsExactly(firstRecordConditionRecord1,
				firstRecordConditionRecord2, secondRecordConditionRecord1, secondRecordConditionRecord2, zeRecord);

	}

	@Test
	public void whenRefreshingRecordThenObtainRecordDTOAndRefreshRecord()
			throws Exception {

		Record firstRecord = mock(RecordImpl.class);
		String firstRecordId = aString();
		when(firstRecord.getId()).thenReturn(firstRecordId);
		when(firstRecord.isSaved()).thenReturn(true);

		Record deletedRecord = mock(RecordImpl.class);
		String deletedRecordId = aString();
		when(deletedRecord.getId()).thenReturn(deletedRecordId);
		when(deletedRecord.isSaved()).thenReturn(true);

		Record newRecord = mock(RecordImpl.class);

		RecordDTO currentFirstRecordDTO = mock(RecordDTO.class);
		long currentFirstRecordDTOVersion = aLong();
		when(currentFirstRecordDTO.getVersion()).thenReturn(currentFirstRecordDTOVersion);

		when(recordDao.realGet(firstRecordId, true)).thenReturn(currentFirstRecordDTO);
		when(recordDao.realGet(deletedRecordId, true)).thenThrow(NoSuchRecordWithId.class);

		recordServices.refresh(asList(firstRecord, deletedRecord, newRecord));

		verify((RecordImpl) firstRecord).refresh(currentFirstRecordDTOVersion, currentFirstRecordDTO);
		verify((RecordImpl) firstRecord, never()).markAsDisconnected();
		verify((RecordImpl) deletedRecord, never()).refresh(currentFirstRecordDTOVersion, currentFirstRecordDTO);
		verify((RecordImpl) deletedRecord).markAsDisconnected();
		verify((RecordImpl) newRecord, never()).refresh(anyLong(), any(RecordDTO.class));
		verify((RecordImpl) newRecord, never()).markAsDisconnected();
	}

	@Test
	public void whenCalculatingModificationImpactThenCallModificationImpactCalculator()
			throws Exception {

		List<String> transactionIds = new ArrayList<>();

		Record zeRecord = mock(Record.class);
		List<ModificationImpact> zeModifications = mock(List.class);
		ModificationImpactCalculator impactCalculator = mock(ModificationImpactCalculator.class);
		List<Metadata> alreadyReindexedMetadata = new ArrayList<>();

		Transaction transaction = new Transaction(zeRecord);

		ModificationImpactCalculatorResponse response = new ModificationImpactCalculatorResponse(
				zeModifications, new ArrayList<String>());
		when(impactCalculator.findTransactionImpact(transaction, true)).thenReturn(response);
		doReturn(impactCalculator).when(recordServices).newModificationImpactCalculator(taxonomiesManager, metadataSchemaTypes,
				searchServices);

		assertThat(recordServices.calculateImpactOfModification(
				transaction, taxonomiesManager, searchServices, metadataSchemaTypes, true).getImpacts())
				.isEqualTo(zeModifications);

	}

	@Test
	public void givenUnresolvableOptimistickLockingWhenExecutingATransactionThenNoInfiniteLoop()
			throws Exception {

		RecordImpl zeRecord = spy(new TestRecord(zeSchema));
		when(recordDao.get("anId")).thenThrow(RecordDaoException.NoSuchRecordWithId.class);
		when(zeRecord.getId()).thenReturn("anId");
		when(zeRecord.isDirty()).thenReturn(true);
		when(zeRecord.getSchemaCode()).thenReturn("zeSchemaType_default");
		Transaction transaction = new Transaction(zeRecord);
		doNothing().when(recordServices).mergeRecords(any(Transaction.class), any(OptimisticLocking.class));
		when(metadataSchemaType.getCode()).thenReturn("fakeSchemaTypeForTest");
		when(schemaManager.getSchemaTypeOf(any(Record.class))).thenReturn(metadataSchemaType);
		doThrow(RecordDaoException.OptimisticLocking.class).when(recordDao).execute(any(TransactionDTO.class));

		try {
			recordServices.execute(transaction);

			verifyNoMoreInteractions(recordDao);

			fail("Exception expected");
		} catch (UnresolvableOptimsiticLockingCausingInfiniteLoops e) {
			e.printStackTrace();
		}

	}

	@Test
	public void givenUnresolvableOptimistickLockingWhenExecutingATransactionHandlingImpactsAsyncThenNoInfiniteLoop()
			throws Exception {

		RecordImpl zeRecord = spy(new TestRecord(zeSchema));
		when(recordDao.get("anId")).thenThrow(RecordDaoException.NoSuchRecordWithId.class);
		when(zeRecord.getId()).thenReturn("anId");
		when(zeRecord.isDirty()).thenReturn(true);
		Transaction transaction = new Transaction(zeRecord);
		doNothing().when(recordServices).mergeRecords(any(Transaction.class), any(OptimisticLocking.class));

		doThrow(RecordDaoException.OptimisticLocking.class).when(recordDao).execute(any(TransactionDTO.class));

		try {
			recordServices.executeHandlingImpactsAsync(transaction);
			fail("Exception expected");
		} catch (UnresolvableOptimsiticLockingCausingInfiniteLoops e) {
			e.printStackTrace();
		}

	}

	@Test
	public void givenRecordServicesExceptionCausedByDTOWhenSavingContentAndRecordsThenDeleteAllNewContents()
			throws Exception {

		String firstHash = "firstHash";
		String secondHash = "secondHash";
		List<String> newContents = Arrays.asList(firstHash, secondHash);
		RecordServicesException zeException = new RecordServicesException("test");
		Transaction transaction = mock(Transaction.class);
		when(transaction.getCollection()).thenReturn(zeCollection);
		doReturn(new ContentModifications(new ArrayList<String>(), newContents)).when(recordServices)
				.findContentsModificationsIn(metadataSchemaTypes, transaction);
		doThrow(zeException).when(recordServices).saveTransactionDTO(eq(transaction),
				eq(recordModificationImpactHandler), anyInt());

		try {
			recordServices.saveContentsAndRecords(transaction, recordModificationImpactHandler, 0);
			fail("Exception expected");
		} catch (Exception e) {
			assertThat(e).isEqualTo(zeException);
			verify(contentManager).silentlyMarkForDeletionIfNotReferenced(firstHash);
			verify(contentManager).silentlyMarkForDeletionIfNotReferenced(secondHash);
		}
	}

	@Test
	public void givenRecordServicesRuntimeExceptionCausedByDTOWhenSavingContentAndRecordsThenDeleteAllNewContents()
			throws Exception {

		String firstHash = "firstHash";
		String secondHash = "secondHash";
		List<String> newContents = Arrays.asList(firstHash, secondHash);
		RecordServicesRuntimeException zeException = new RecordServicesRuntimeException("test");
		Transaction transaction = mock(Transaction.class);
		when(transaction.getCollection()).thenReturn(zeCollection);
		doReturn(new ContentModifications(new ArrayList<String>(), newContents)).when(recordServices)
				.findContentsModificationsIn(metadataSchemaTypes, transaction);
		doThrow(zeException).when(recordServices).saveTransactionDTO(eq(transaction),
				eq(recordModificationImpactHandler), anyInt());

		try {
			recordServices.saveContentsAndRecords(transaction, recordModificationImpactHandler, 0);
			fail("Exception expected");
		} catch (Exception e) {
			assertThat(e).isEqualTo(zeException);
			verify(contentManager).silentlyMarkForDeletionIfNotReferenced(firstHash);
			verify(contentManager).silentlyMarkForDeletionIfNotReferenced(secondHash);
		}
	}

	@Test
	public void givenAdvancedSearchConfigsWhenFlushingThenFlushInDao()
			throws Exception {
		Toggle.ADVANCED_SEARCH_CONFIGS.enable();

		recordServices.flush();

		verify(recordDao).flush();
		verify(eventsDao).flush();
		verify(searchDao).flush();

	}


	@Test
	public void givenNoAdvancedSearchConfigsWhenFlushingThenFlushInDao()
			throws Exception {

		recordServices.flush();

		verify(recordDao).flush();
		verify(eventsDao, never()).flush();

	}

	@Test(expected = RecordServicesRuntimeException_RecordsFlushingFailed.class)
	public void givenRecordDaoRuntimeExceptionWhenFlushingThenFlushInDao()
			throws Exception {

		doThrow(RecordDaoRuntimeException_RecordsFlushingFailed.class).when(recordDao).flush();

		recordServices.flush();

	}

	@Test(expected = RecordServicesRuntimeException_RecordsFlushingFailed.class)
	public void givenEventsDaoRuntimeExceptionWhenFlushingThenFlushInDao()
			throws Exception {
		Toggle.ADVANCED_SEARCH_CONFIGS.enable();
		doThrow(RecordDaoRuntimeException_RecordsFlushingFailed.class).when(eventsDao).flush();

		recordServices.flush();

	}

}
