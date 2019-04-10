package com.constellio.model.services.batch.manager;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.ConfigManagerException;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import com.constellio.data.dao.services.bigVault.solr.SolrUtils;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.records.DataStore;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.batchprocess.BatchProcessPart;
import com.constellio.model.entities.batchprocess.RecordBatchProcess;
import com.constellio.model.services.batch.xml.detail.BatchProcessReader;
import com.constellio.model.services.batch.xml.list.BatchProcessListReader;
import com.constellio.model.services.batch.xml.list.BatchProcessListWriter;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.CollectionFilters;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.SolrQueryBuilderContext;
import com.constellio.sdk.tests.ConstellioTest;
import org.jdom2.Document;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.constellio.model.services.batch.manager.BatchProcessesManager.BATCH_PROCESS_LIST_PATH;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BatchProcessesManagerTest extends ConstellioTest {

	BatchProcessesManager manager;

	String firstRecordId = aString();
	String secondRecordId = aString();
	List<String> zeNextPartRecords = asList(firstRecordId, secondRecordId);
	String thirdRecordId = aString();
	//List<String> recordIds = asList(firstRecordId, secondRecordId, thirdRecordId);
	List<String> zePreviousPartRecords = asList(thirdRecordId);
	String firstReindexedFieldCode = aString();
	String secondReindexedFieldCode = aString();
	String batchProcessListPath = "/batchProcesses/list.xml";
	@Mock BatchProcessAction action;
	@Mock Document batchProcessListDocument;
	@Mock BatchProcessListReader batchProcessListReader;
	@Mock BatchProcessListWriter batchProcessListWriter;
	String aBatchProcessPath = "/batchProcesses/aBatchProcess.xml";
	String aBatchProcessId = "aBatchProcess";
	@Mock RecordBatchProcess aBatchProcess;
	@Mock Document aBatchProcessDocument;
	@Mock XMLConfiguration aBatchProcessConfiguration;
	//@Mock BatchProcessReader aBatchProcessReader;
	String anotherBatchProcessPath = "/batchProcesses/anotherBatchProcess.xml";
	String anotherBatchProcessId = "anotherBatchProcess";
	@Mock RecordBatchProcess anotherBatchProcess;
	@Mock Document anotherBatchProcessDocument;
	@Mock XMLConfiguration anotherBatchProcessConfiguration;
	@Mock BatchProcessReader anotherBatchProcessReader;
	@Mock RecordServices recordServices;
	@Mock ConfigManager configManager;
	@Mock List<BatchProcess> batchProcesses;
	@Mock DocumentAlteration addBatchProcessDocumentAlteration;
	@Mock SearchServices searchServices;
	LocalDateTime currentDate = aDateTime();
	LocalDateTime newCurrentDate = aDateTime();
	XMLConfiguration initialBatchProcessListConfig, updatedBatchProcessListConfig;
	String initialHash = "initialHash";
	String updatedHash = "updatedHash";
	int theWantedPartSize = anInteger();
	String zeComputer = "zeComputer";
	@Mock List<String> theErrorsList;
	List<String> emptyNextPartRecords = new ArrayList<>();
	@Mock BatchProcessPart zeNextPart;
	@Mock BatchProcessPart zePreviousPart;
	@Mock CollectionsListManager collectionsListManager;

	@Mock ConstellioCacheManager constellioCacheManager;
	@Mock ConstellioCache cache;

	@Mock BatchProcessesListUpdatedEventListener firstListener;
	@Mock BatchProcessesListUpdatedEventListener secondListener;

	@Mock LogicalSearchCondition condition;

	@Mock ModelLayerFactory modelLayerFactory;
	@Mock DataLayerFactory dataLayerFactory;

	@Before
	public void setUp()
			throws Exception {
		when(aBatchProcess.getQuery()).thenReturn("zeQuery");
		when(condition.getFilters()).thenReturn(new CollectionFilters(zeCollection, DataStore.RECORDS, false));
		when(condition.getSolrQuery(any(SolrQueryBuilderContext.class))).thenReturn("zeQuery");
		when(condition.getCollection()).thenReturn(zeCollection);

		when(aBatchProcess.getId()).thenReturn(aBatchProcessId);
		when(anotherBatchProcess.getId()).thenReturn(anotherBatchProcessId);

		initialBatchProcessListConfig = mock(XMLConfiguration.class);
		when(initialBatchProcessListConfig.getDocument()).thenReturn(batchProcessListDocument);
		when(initialBatchProcessListConfig.getHash()).thenReturn(initialHash);

		updatedBatchProcessListConfig = mock(XMLConfiguration.class);
		when(updatedBatchProcessListConfig.getDocument()).thenReturn(batchProcessListDocument);
		when(updatedBatchProcessListConfig.getHash()).thenReturn(updatedHash);

		when(aBatchProcessConfiguration.getDocument()).thenReturn(aBatchProcessDocument);
		when(aBatchProcessConfiguration.getHash()).thenReturn(initialHash);
		when(configManager.getXML(aBatchProcessPath)).thenReturn(aBatchProcessConfiguration);
		when(anotherBatchProcessConfiguration.getDocument()).thenReturn(anotherBatchProcessDocument);
		when(anotherBatchProcessConfiguration.getHash()).thenReturn(initialHash);
		when(configManager.getXML(anotherBatchProcessPath)).thenReturn(anotherBatchProcessConfiguration);

		when(searchServices.getLanguages(any(LogicalSearchQuery.class))).thenReturn(asList("en"));
		when(searchServices.addSolrModifiableParams(any(LogicalSearchQuery.class))).thenCallRealMethod();

		when(modelLayerFactory.newSearchServices()).thenReturn(searchServices);
		when(modelLayerFactory.getDataLayerFactory()).thenReturn(dataLayerFactory);
		when(dataLayerFactory.getConfigManager()).thenReturn(configManager);

		when(dataLayerFactory.getDistributedCacheManager()).thenReturn(constellioCacheManager);
		when(dataLayerFactory.getLocalCacheManager()).thenReturn(constellioCacheManager);
		when(constellioCacheManager.getCache(anyString())).thenReturn(cache);
	}

	private void createManager() {

		manager = spy(new BatchProcessesManager(modelLayerFactory));
		doReturn(currentDate).doReturn(newCurrentDate).when(manager).getCurrentTime();
		doReturn(batchProcessListReader).when(manager).newBatchProcessListReader(any(Document.class));
		doReturn(batchProcessListWriter).when(manager).newBatchProcessListWriter(any(Document.class));
		//doReturn(aBatchProcessReader).when(manager).newBatchProcessReader(aBatchProcessDocument);
		doReturn(anotherBatchProcessReader).when(manager).newBatchProcessReader(anotherBatchProcessDocument);
		doNothing().when(manager).deleteFinishedWithoutErrors();
	}

	@Test
	public void givenNoBatchProcessesListConfigWhenCreatingBatchProcessesManagerThenCreateConfigAndListenIt()
			throws Exception {

		when(configManager.exist(BatchProcessesManager.BATCH_PROCESS_LIST_PATH)).thenReturn(false);

		createManager();
		manager.initialize();

		InOrder inOrder = inOrder(configManager, manager);
		inOrder.verify(configManager).add(eq(BatchProcessesManager.BATCH_PROCESS_LIST_PATH), any(Document.class));
		inOrder.verify(configManager).registerListener(BatchProcessesManager.BATCH_PROCESS_LIST_PATH, manager);

	}

	@Test
	public void givenBatchProcessesListConfigAlreadyCreatedWhenCreatingBatchProcessesManagerThenListenConfig()
			throws Exception {

		when(configManager.exist(BatchProcessesManager.BATCH_PROCESS_LIST_PATH)).thenReturn(true);

		createManager();
		manager.initialize();

		verify(configManager, never()).add(anyString(), any(Document.class));
		verify(configManager).registerListener(BatchProcessesManager.BATCH_PROCESS_LIST_PATH, manager);

	}

	@Test
	public void givenBatchProcessListConfigurationExistingWhenGetBatchProcessListDocumentThenReturnExistingOne()
			throws Exception {
		createManager();
		XMLConfiguration batchProcessListConfig = mock(XMLConfiguration.class);
		when(batchProcessListConfig.getDocument()).thenReturn(batchProcessListDocument);
		when(configManager.getXML(batchProcessListPath)).thenReturn(batchProcessListConfig);

		Document returnedDocument = manager.getProcessListXMLDocument();

		assertThat(returnedDocument).isEqualTo(batchProcessListDocument);

		verify(manager, never()).newBatchProcessListWriter(any(Document.class));
		verify(batchProcessListWriter, never()).createEmptyProcessList();

	}

	@Test
	public void whenGetBatchProcessByIdThenLoadFromBatchProcessList()
			throws Exception {
		createManager();
		givenExistingBatchProcessList();

		when(batchProcessListReader.read(aBatchProcessId)).thenReturn(aBatchProcess);

		BatchProcess returnedBatchProcess = manager.get(aBatchProcessId);

		assertThat(returnedBatchProcess).isSameAs(aBatchProcess);
	}

	@Test
	public void whenGetPendingBatchProcessThenLoadFromBatchProcessList()
			throws Exception {
		createManager();
		givenExistingBatchProcessList();
		when(batchProcessListReader.readPendingBatchProcesses()).thenReturn(batchProcesses);

		List<BatchProcess> returnedBatchProcesses = manager.getPendingBatchProcesses();

		assertThat(returnedBatchProcesses).isSameAs(batchProcesses);
	}

	@Test
	public void whenGetStandbyBatchProcessThenLoadFromBatchProcessList()
			throws Exception {
		createManager();
		givenExistingBatchProcessList();
		when(batchProcessListReader.readStandbyBatchProcesses()).thenReturn(batchProcesses);

		List<BatchProcess> returnedBatchProcesses = manager.getStandbyBatchProcesses();

		assertThat(returnedBatchProcesses).isSameAs(batchProcesses);
	}

	@Test
	public void whenGetFinishedBatchProcessThenLoadFromBatchProcessList()
			throws Exception {
		createManager();
		givenExistingBatchProcessList();
		when(batchProcessListReader.readFinishedBatchProcesses()).thenReturn(batchProcesses);

		List<BatchProcess> returnedBatchProcesses = manager.getFinishedBatchProcesses();

		assertThat(returnedBatchProcesses).isSameAs(batchProcesses);
	}

	@Test
	public void whenAddingBatchProcessThenAddToBatchProcessListAndCreateItsOwnXmlFile()
			throws Exception {
		//		String solrQuery = "fq=(*:*+-type_s:index)&fq=collection_s:zeCollection&fq=zeQuery&qt=/spell&shards.qt=/spell&q=*:*&rows=100000&start=0";
		String urlQueryString = SolrUtils.toSingleQueryString(searchServices.addSolrModifiableParams(new LogicalSearchQuery()
				.setCondition(
						condition)));//"?fq=%28*%3A*+-type_s%3Aindex%29&fq=collection_s%3AzeCollection&fq=zeQuery&qt=%2Fspell&shards.qt=%2Fspell&q=*%3A*&rows=100000&start=0";
		createManager();
		givenExistingBatchProcessList();
		doReturn(addBatchProcessDocumentAlteration).when(manager)
				.newAddBatchProcessDocumentAlteration(aBatchProcessId, urlQueryString, "zeCollection",
						currentDate, 42, action, null, null);
		doReturn(aBatchProcessDocument).when(manager).newDocument();
		doReturn(aBatchProcessId).when(manager).newBatchProcessId();
		when(batchProcessListReader.read(aBatchProcessId)).thenReturn(aBatchProcess);
		when(searchServices.getResultsCount(any(LogicalSearchQuery.class))).thenReturn(42L);
		BatchProcess returnedBatchProcess = manager.addBatchProcessInStandby(condition, action, null);

		assertThat(returnedBatchProcess).isEqualTo(aBatchProcess);
		InOrder inOrder = Mockito.inOrder(manager, configManager, batchProcessListReader);
		inOrder.verify(manager).newAddBatchProcessDocumentAlteration(
				aBatchProcessId, urlQueryString, "zeCollection", currentDate, 42, action, null, null);
		inOrder.verify(configManager).updateXML(BATCH_PROCESS_LIST_PATH, addBatchProcessDocumentAlteration);
		inOrder.verify(batchProcessListReader).read(aBatchProcessId);
	}

	@Test
	public void givenOptimisticLockingWhileGettingCurrentBatchProcessThenRetry()
			throws Exception {
		createManager();
		doThrow(ConfigManagerException.OptimisticLockingConfiguration.class)
				.doThrow(ConfigManagerException.OptimisticLockingConfiguration.class).doReturn(aBatchProcess).when(manager)
				.getCurrentBatchProcessWithPossibleOptimisticLocking();

		manager.getCurrentBatchProcess();

		InOrder inOrder = inOrder(manager);
		inOrder.verify(manager).getCurrentBatchProcess();
		inOrder.verify(manager).getCurrentBatchProcessWithPossibleOptimisticLocking();
		inOrder.verify(manager).getCurrentBatchProcess();
		inOrder.verify(manager).getCurrentBatchProcessWithPossibleOptimisticLocking();
		inOrder.verify(manager).getCurrentBatchProcess();
		inOrder.verify(manager).getCurrentBatchProcessWithPossibleOptimisticLocking();

	}

	@Test
	public void givenCurrentBatchProcessWhenGetCurrentBatchProcessThenReturnIt()
			throws Exception {
		createManager();
		givenExistingBatchProcessList();

		when(batchProcessListReader.readCurrent()).thenReturn(aBatchProcess);

		BatchProcess returnedBatchProcess = manager.getCurrentBatchProcessWithPossibleOptimisticLocking();

		assertThat(returnedBatchProcess).isEqualTo(aBatchProcess);

	}

	@Test
	public void givenNoCurrentBatchProcessWhenGetPendingBatchProcessThenStartNext()
			throws Exception {
		createManager();
		givenExistingBatchProcessList();

		when(batchProcessListReader.readPendingBatchProcesses())
				.thenReturn(asList((BatchProcess) aBatchProcess, anotherBatchProcess));
		when(batchProcessListReader.readCurrent()).thenReturn(BatchProcessListReader.NO_CURRENT_BATCH_PROCESS).thenReturn(
				aBatchProcess);

		BatchProcess returnedBatchProcess = manager.getCurrentBatchProcessWithPossibleOptimisticLocking();

		InOrder inOrder = Mockito.inOrder(batchProcessListWriter, batchProcessListReader, configManager);
		inOrder.verify(batchProcessListReader).readCurrent();
		inOrder.verify(batchProcessListWriter).startNextBatchProcess(any(LocalDateTime.class));
		inOrder.verify(configManager)
				.update(BatchProcessesManager.BATCH_PROCESS_LIST_PATH, initialHash, batchProcessListDocument);
		inOrder.verify(batchProcessListReader).readCurrent();

		assertThat(returnedBatchProcess).isEqualTo(aBatchProcess);
	}

	@Test
	public void givenNoCurrentBatchProcessAndNoPendingBatchProcessWhenGetCurrentBatchProcessThenReturnNoCurrentBatchProcess()
			throws Exception {
		createManager();
		givenExistingBatchProcessList();

		when(batchProcessListReader.readPendingBatchProcesses()).thenReturn(new ArrayList<BatchProcess>());
		when(batchProcessListReader.readCurrent()).thenReturn(BatchProcessListReader.NO_CURRENT_BATCH_PROCESS);

		BatchProcess returnedBatchProcess = manager.getCurrentBatchProcessWithPossibleOptimisticLocking();

		assertThat(returnedBatchProcess).isEqualTo(BatchProcessListReader.NO_CURRENT_BATCH_PROCESS);
		verify(batchProcessListWriter, never()).startNextBatchProcess(any(LocalDateTime.class));
	}
	//
	//	@Test
	//	public void givenOptimisticLockingWhenGettingBatchProcessPartThenRetry()
	//			throws Exception {
	//		createManager();
	//		doThrow(ConfigManagerException.OptimisticLockingConfiguration.class)
	//				.doThrow(ConfigManagerException.OptimisticLockingConfiguration.class).doReturn(zeNextPart).when(manager)
	//				.getBatchProcessPartWithPossibleOptimisticLocking();
	//
	//		BatchProcessPart nextPartReturned = manager.getCurrentBatchProcessPart();
	//
	//		assertThat(nextPartReturned).isSameAs(zeNextPart);
	//		InOrder inOrder = inOrder(manager);
	//		inOrder.verify(manager).getCurrentBatchProcessPart();
	//		inOrder.verify(manager).getBatchProcessPartWithPossibleOptimisticLocking();
	//		inOrder.verify(manager).getCurrentBatchProcessPart();
	//		inOrder.verify(manager).getBatchProcessPartWithPossibleOptimisticLocking();
	//		inOrder.verify(manager).getCurrentBatchProcessPart();
	//		inOrder.verify(manager).getBatchProcessPartWithPossibleOptimisticLocking();
	//	}
	//
	//	@Test
	//	public void whenGetBatchProcessPartThenGetCurrentBatchProcessAndReserveNextRecords()
	//			throws Exception {
	//		createManager();
	//		givenExistingBatchProcessList();
	//		doReturn(aBatchProcess).when(manager).getCurrentBatchProcess();
	//		when(aBatchProcessWriter.assignBatchProcessPartTo(zeComputer, theWantedPartSize)).thenReturn(zeNextPartRecords);
	//
	//		BatchProcessPart part = manager.getBatchProcessPartWithPossibleOptimisticLocking();
	//
	//		assertThat(part.getBatchProcess()).isSameAs(aBatchProcess);
	//		assertThat(part.getRecordIds()).isSameAs(zeNextPartRecords);
	//		verify(configManager).update(aBatchProcessPath, initialHash, aBatchProcessDocument);
	//	}
	//
	//	@Test
	//	public void givenNoCurrentBatchProcessWhenGetBatchProcessPartThenReturnNull()
	//			throws Exception {
	//		createManager();
	//		givenExistingBatchProcessList();
	//		doReturn(BatchProcessListReader.NO_CURRENT_BATCH_PROCESS).when(manager).getCurrentBatchProcess();
	//
	//		BatchProcessPart part = manager.getBatchProcessPartWithPossibleOptimisticLocking();
	//
	//		assertThat(part).isNull();
	//	}
	//
	//	@Test
	//	public void givenCurrentBatchProcessWithoutRemainingRecordsWhenGetBatchProcessPartThenReturnNull()
	//			throws Exception {
	//		createManager();
	//		givenExistingBatchProcessList();
	//		doReturn(aBatchProcess).when(manager).getCurrentBatchProcess();
	//		when(aBatchProcessWriter.assignBatchProcessPartTo(zeComputer, theWantedPartSize)).thenReturn(emptyNextPartRecords);
	//
	//		BatchProcessPart part = manager.getBatchProcessPartWithPossibleOptimisticLocking();
	//
	//		assertThat(part).isNull();
	//		verify(configManager, never()).update(aBatchProcessPath, initialHash, aBatchProcessDocument);
	//	}
	//
	//	@Test
	//	public void givenOptimisticLockingWhenMarkBatchProcessPartAsFinishedAndGetAnotherPartThenRetry()
	//			throws Exception {
	//		createManager();
	//		doThrow(ConfigManagerException.OptimisticLockingConfiguration.class)
	//				.doThrow(ConfigManagerException.OptimisticLockingConfiguration.class).doReturn(zeNextPart).when(manager)
	//				.markBatchProcessPartAsFinishedAndGetAnotherPartWithPossibleOptimisticLocking(zePreviousPart, theErrorsList);
	//
	//		BatchProcessPart nextPartReturned = manager
	//				.markBatchProcessPartAsFinishedAndGetAnotherPart(zePreviousPart, theErrorsList);
	//
	//		assertThat(nextPartReturned).isSameAs(zeNextPart);
	//
	//		InOrder inOrder = inOrder(manager);
	//		inOrder.verify(manager).markBatchProcessPartAsFinishedAndGetAnotherPart(zePreviousPart, theErrorsList);
	//		inOrder.verify(manager).markBatchProcessPartAsFinishedAndGetAnotherPartWithPossibleOptimisticLocking(zePreviousPart,
	//				theErrorsList);
	//		inOrder.verify(manager).markBatchProcessPartAsFinishedAndGetAnotherPart(zePreviousPart, theErrorsList);
	//		inOrder.verify(manager).markBatchProcessPartAsFinishedAndGetAnotherPartWithPossibleOptimisticLocking(zePreviousPart,
	//				theErrorsList);
	//		inOrder.verify(manager).markBatchProcessPartAsFinishedAndGetAnotherPart(zePreviousPart, theErrorsList);
	//		inOrder.verify(manager).markBatchProcessPartAsFinishedAndGetAnotherPartWithPossibleOptimisticLocking(zePreviousPart,
	//				theErrorsList);
	//	}
	//
	//	@Test
	//	public void whenMarkBatchProcessPartAsFinishedAndGetAnotherPartThenGetCurrentBatchProcessAndReserveNextRecords()
	//			throws Exception {
	//		createManager();
	//		DocumentAlteration incrementProgression = mock(DocumentAlteration.class);
	//
	//		doReturn(incrementProgression).when(manager).newIncrementProgressionDocumentAlteration(aBatchProcess, 3, 0);
	//		BatchProcessPart previousPart = new BatchProcessPart(aBatchProcess, recordIds);
	//
	//		givenExistingBatchProcessList();
	//		doReturn(aBatchProcess).when(manager).getCurrentBatchProcess();
	//		when(aBatchProcessWriter.assignBatchProcessPartTo(zeComputer, theWantedPartSize)).thenReturn(zeNextPartRecords);
	//
	//		BatchProcessPart part = manager.markBatchProcessPartAsFinishedAndGetAnotherPartWithPossibleOptimisticLocking(
	//				previousPart, theErrorsList);
	//
	//		assertThat(part.getBatchProcess()).isSameAs(aBatchProcess);
	//		assertThat(part.getRecordIds()).isSameAs(zeNextPartRecords);
	//		InOrder inOrder = inOrder(configManager, aBatchProcessWriter);
	//		inOrder.verify(aBatchProcessWriter).markHasDone(zeComputer, theErrorsList);
	//		inOrder.verify(aBatchProcessWriter).assignBatchProcessPartTo(zeComputer, theWantedPartSize);
	//		inOrder.verify(configManager).update(aBatchProcessPath, initialHash, aBatchProcessDocument);
	//		inOrder.verify(configManager).updateXML(BATCH_PROCESS_LIST_PATH, incrementProgression);
	//		verify(manager).newIncrementProgressionDocumentAlteration(aBatchProcess, 3, 0);
	//	}
	//
	//	@Test
	//	public void givenCurrentBatchProcessWithoutRemainingRecordsWhenMarkBatchProcessPartAsFinishedAndGetAnotherPartThenReturnNull()
	//			throws Exception {
	//		createManager();
	//		BatchProcessPart previousPart = new BatchProcessPart(aBatchProcess, recordIds);
	//
	//		givenExistingBatchProcessList();
	//		doReturn(aBatchProcess).when(manager).getCurrentBatchProcess();
	//		when(aBatchProcessWriter.assignBatchProcessPartTo(zeComputer, theWantedPartSize)).thenReturn(emptyNextPartRecords);
	//
	//		BatchProcessPart part = manager.markBatchProcessPartAsFinishedAndGetAnotherPartWithPossibleOptimisticLocking(
	//				previousPart, theErrorsList);
	//
	//		assertThat(part).isNull();
	//		InOrder inOrder = inOrder(configManager, aBatchProcessWriter);
	//		inOrder.verify(aBatchProcessWriter).markHasDone(zeComputer, theErrorsList);
	//		inOrder.verify(aBatchProcessWriter).assignBatchProcessPartTo(zeComputer, theWantedPartSize);
	//		inOrder.verify(configManager).update(aBatchProcessPath, initialHash, aBatchProcessDocument);
	//		verify(manager).newIncrementProgressionDocumentAlteration(aBatchProcess, 3, 0);
	//	}

	@Test
	public void whenBatchProcessesListUpdatedThenNotifyAllListeners()
			throws Exception {

		createManager();

		manager.registerBatchProcessesListUpdatedEvent(firstListener);
		manager.registerBatchProcessesListUpdatedEvent(secondListener);

		manager.onConfigUpdated(BatchProcessesManager.BATCH_PROCESS_LIST_PATH);

		verify(firstListener).onBatchProcessesListUpdated();
		verify(secondListener).onBatchProcessesListUpdated();
	}

	@Test
	public void whenMarkAllBATCHPROCEDocument()
			throws Exception {

		createManager();

		manager.registerBatchProcessesListUpdatedEvent(firstListener);
		manager.registerBatchProcessesListUpdatedEvent(secondListener);

		manager.onConfigUpdated(BatchProcessesManager.BATCH_PROCESS_LIST_PATH);

		verify(firstListener).onBatchProcessesListUpdated();
		verify(secondListener).onBatchProcessesListUpdated();
	}

	@Test
	public void whenCreatingBatchProcessThenCallMarkAllAsReady() {

		final AtomicInteger markAllStandbyAsPending = new AtomicInteger();

		BatchProcessesManager manager = spy(new BatchProcessesManager(modelLayerFactory) {

			@Override
			public void markAllStandbyAsPending() {
				markAllStandbyAsPending.incrementAndGet();
			}

		});

		doNothing().when(manager).deleteFinishedWithoutErrors();

		manager.initialize();

		assertThat(markAllStandbyAsPending.get()).isEqualTo(1);
	}

	private void givenExistingBatchProcessList()
			throws Exception {
		doReturn(initialBatchProcessListConfig).when(configManager).getXML(BATCH_PROCESS_LIST_PATH);
	}

}
