package com.constellio.model.services.batch.manager;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.ConfigManagerException;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.managers.config.events.ConfigUpdatedEventListener;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import com.constellio.data.dao.services.bigVault.solr.SolrUtils;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.model.entities.batchprocess.AsyncTaskBatchProcess;
import com.constellio.model.entities.batchprocess.AsyncTaskCreationRequest;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.batchprocess.BatchProcessStatus;
import com.constellio.model.entities.batchprocess.RecordBatchProcess;
import com.constellio.model.services.background.RecordsReindexingBackgroundAction;
import com.constellio.model.services.batch.controller.BatchProcessState;
import com.constellio.model.services.batch.xml.detail.BatchProcessReader;
import com.constellio.model.services.batch.xml.list.BatchProcessListReader;
import com.constellio.model.services.batch.xml.list.BatchProcessListWriter;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.VisibilityStatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.jdom2.Document;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static com.constellio.data.dao.services.cache.InsertionReason.WAS_MODIFIED;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.entities.schemas.Schemas.MARKED_FOR_REINDEXING;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromEveryTypesOfEveryCollection;

public class BatchProcessesManager implements StatefulService, ConfigUpdatedEventListener {

	static final String BATCH_PROCESS_LIST_PATH = "/batchProcesses/list.xml";
	private static final Logger LOGGER = LoggerFactory.getLogger(BatchProcessesManager.class);
	private final ConfigManager configManager;
	private final List<BatchProcessesListUpdatedEventListener> listeners = new ArrayList<>();
	private final ModelLayerFactory modelLayerFactory;
	private ConstellioCache statusCache;
	private List<String> finishedBatchProcessIds = new LinkedList<>();

	public BatchProcessesManager(ModelLayerFactory modelLayerFactory) {
		super();
		this.configManager = modelLayerFactory.getDataLayerFactory().getConfigManager();
		this.modelLayerFactory = modelLayerFactory;
		statusCache = modelLayerFactory.getDataLayerFactory().getDistributedCacheManager().getCache("batchProcessesStatus");
		configManager.keepInCache(BATCH_PROCESS_LIST_PATH);
	}

	@Override
	public void initialize() {
		if (!configManager.exist(BATCH_PROCESS_LIST_PATH)) {
			saveEmptyProcessListXMLDocument();
		}
		configManager.registerListener(BATCH_PROCESS_LIST_PATH, this);
		markAllStandbyAsPending();

		deleteFinishedWithoutErrors();
	}

	void deleteFinishedWithoutErrors() {
		List<String> batchProcessIds = new ArrayList<>();
		for (BatchProcess batchProcess : getFinishedBatchProcesses()) {
			if (batchProcess.getErrors() == 0) {
				batchProcessIds.add(batchProcess.getId());
			}
		}

		delete(batchProcessIds);
	}

	private void delete(List<String> batchProcessIds) {
		configManager.updateXML(BATCH_PROCESS_LIST_PATH, newDeleteBatchProcessesAlteration(batchProcessIds));
		for (String batchProcessId : batchProcessIds) {
			configManager.delete("/batchProcesses/" + batchProcessId + ".xml");
		}

	}

	public RecordBatchProcess addPendingBatchProcess(LogicalSearchCondition condition, BatchProcessAction action,
													 String title) {
		BatchProcess batchProcess = addBatchProcessInStandby(condition, action, title);
		markAsPending(batchProcess);
		return (RecordBatchProcess) get(batchProcess.getId());
	}

	public RecordBatchProcess addPendingBatchProcess(LogicalSearchQuery query, BatchProcessAction action,
													 String username,
													 String title) {
		BatchProcess batchProcess = addBatchProcessInStandby(query, action, username, title);
		markAsPending(batchProcess);
		return (RecordBatchProcess) get(batchProcess.getId());
	}

	public RecordBatchProcess addPendingBatchProcess(List<String> records, BatchProcessAction action, String username,
													 String title,
													 String collection) {
		BatchProcess batchProcess = addBatchProcessInStandby(records, action, username, title, collection);
		markAsPending(batchProcess);
		return (RecordBatchProcess) get(batchProcess.getId());
	}

	public RecordBatchProcess addPendingBatchProcess(LogicalSearchQuery query, BatchProcessAction action,
													 String title) {
		BatchProcess batchProcess = addBatchProcessInStandby(query, action, title);
		markAsPending(batchProcess);
		return (RecordBatchProcess) get(batchProcess.getId());
	}

	public RecordBatchProcess addBatchProcessInStandby(LogicalSearchCondition condition, BatchProcessAction action,
													   String title) {
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		String collection = condition.getCollection();
		String id = newBatchProcessId();

		return addBatchProcessInStandby(query, action, title);
	}

	public RecordBatchProcess addBatchProcessInStandby(LogicalSearchQuery logicalQuery, BatchProcessAction action,
													   String title) {
		return addBatchProcessInStandby(logicalQuery, action, null, title);
	}

	public RecordBatchProcess addBatchProcessInStandby(LogicalSearchCondition condition, BatchProcessAction action,
													   String username,
													   String title) {
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		String collection = condition.getCollection();
		String id = newBatchProcessId();

		return addBatchProcessInStandby(query, action, username, title);
	}

	public RecordBatchProcess addBatchProcessInStandby(LogicalSearchQuery logicalQuery, BatchProcessAction action,
													   String username,
													   String title) {
		String collection = logicalQuery.getCondition().getCollection();
		String id = newBatchProcessId();

		SearchServices searchServices = modelLayerFactory.newSearchServices();
		ModifiableSolrParams params = searchServices.addSolrModifiableParams(logicalQuery);
		String solrQuery = SolrUtils.toSingleQueryString(params);

		LocalDateTime requestDateTime = getCurrentTime();

		long recordsCount = searchServices.getResultsCount(logicalQuery);
		updateBatchProcesses(
				newAddBatchProcessDocumentAlteration(id, solrQuery, collection, requestDateTime, (int) recordsCount, action,
						username, title));

		return (RecordBatchProcess) newBatchProcessListReader(getProcessListXMLDocument()).read(id);
	}

	public RecordBatchProcess addBatchProcessInStandby(List<String> records, BatchProcessAction action, String username,
													   String title,
													   String collection) {
		String id = newBatchProcessId();

		LocalDateTime requestDateTime = getCurrentTime();

		long recordsCount = records.size();
		updateBatchProcesses(
				newAddBatchProcessDocumentAlteration(id, records, collection, requestDateTime, (int) recordsCount, action,
						username, title));

		return (RecordBatchProcess) newBatchProcessListReader(getProcessListXMLDocument()).read(id);
	}

	public AsyncTaskBatchProcess addAsyncTask(final AsyncTaskCreationRequest request) {

		final String id = newBatchProcessId();
		final LocalDateTime requestDateTime = getCurrentTime();

		DocumentAlteration alteration = new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				newBatchProcessListWriter(document).addAsyncTaskBatchProcess(id, requestDateTime, request);
			}
		};

		updateBatchProcesses(alteration);
		if (!request.isInStandby()) {
			markAsPending(newBatchProcessListReader(getProcessListXMLDocument()).read(id));
		}

		return (AsyncTaskBatchProcess) newBatchProcessListReader(getProcessListXMLDocument()).read(id);

	}

	public void markAsPending(List<BatchProcess> batchProcesses) {
		for (BatchProcess batchProcess : batchProcesses) {
			markAsPending(batchProcess);
		}
	}

	public void markAsPending(BatchProcess batchProcess) {
		updateBatchProcesses(markBatchProcessAsPendingDocumentAlteration(batchProcess.getId()));
	}

	public void cancelStandByBatchProcesses(List<BatchProcess> batchProcesses) {
		for (BatchProcess batchProcess : batchProcesses) {
			cancelStandByBatchProcess(batchProcess);
		}
	}

	public void cancelStandByBatchProcess(BatchProcess batchProcess) {
		updateBatchProcesses(cancelStandByBatchProcessDocumentAlteration(batchProcess.getId()));
	}

	public void cancelPendingBatchProcess(BatchProcess batchProcess) {
		updateBatchProcesses(cancelPendingBatchProcessDocumentAlteration(batchProcess.getId()));
	}

	public void cancelBatchProcessNoMatterItStatus(BatchProcess batchProcess) {
		if (batchProcess.getStatus() == BatchProcessStatus.PENDING) {
			cancelPendingBatchProcess(batchProcess);
		}

		if (batchProcess.getStatus() == BatchProcessStatus.STANDBY) {
			cancelStandByBatchProcess(batchProcess);
		}
	}

	public void markAllStandbyAsPending() {
		updateBatchProcesses(markAllBatchProcessAsPendingDocumentAlteration());
	}

	private void updateBatchProcesses(DocumentAlteration documentAlteration) {
		configManager.updateXML(BATCH_PROCESS_LIST_PATH, documentAlteration);
	}

	private BatchProcess withQueryIfBatchProcessFromPreviousFramework(BatchProcess batchProcess) {
		if (batchProcess != null && batchProcess instanceof RecordBatchProcess) {

			RecordBatchProcess recordBatchProcess = (RecordBatchProcess) batchProcess;
			if (recordBatchProcess.getQuery() == null && recordBatchProcess.getRecords() == null) {
				List<String> records = getRecords(batchProcess);
				LogicalSearchCondition condition = fromAllSchemasIn(batchProcess.getCollection()).where(IDENTIFIER)
						.isIn(records);
				ModifiableSolrParams params = modelLayerFactory.newSearchServices()
						.addSolrModifiableParams(new LogicalSearchQuery(condition));
				String query = SolrUtils.toSingleQueryString(params);
				batchProcess = recordBatchProcess.withQuery(query);
			}
		}
		return batchProcess;
	}

	public BatchProcess get(String id) {

		BatchProcessListReader reader = newBatchProcessListReader(getProcessListXMLDocument());
		return withQueryIfBatchProcessFromPreviousFramework(reader.read(id));
	}

	public BatchProcess getCurrentBatchProcess() {
		try {
			return getCurrentBatchProcessWithPossibleOptimisticLocking();
		} catch (ConfigManagerException.OptimisticLockingConfiguration e) {
			LOGGER.info("Optimistic locking while getting current batch process, retrying...");
			return getCurrentBatchProcess();
		}
	}

	BatchProcess getCurrentBatchProcessWithPossibleOptimisticLocking()
			throws ConfigManagerException.OptimisticLockingConfiguration {
		XMLConfiguration xmlConfiguration = getProcessListXMLConfiguration();
		Document processList = getProcessListXMLDocument();
		BatchProcessListReader reader = newBatchProcessListReader(processList);
		BatchProcess batchProcess = reader.readCurrent();
		List<BatchProcess> pendingBatchProcesses = reader.readPendingBatchProcesses();

		if (batchProcess == BatchProcessListReader.NO_CURRENT_BATCH_PROCESS && !pendingBatchProcesses.isEmpty()) {
			startNextBatchProcess(xmlConfiguration, processList);
			batchProcess = getCurrentBatchProcess();
		}
		return withQueryIfBatchProcessFromPreviousFramework(batchProcess);
	}

	LocalDateTime getCurrentTime() {
		return new LocalDateTime();
	}

	XMLConfiguration getExistingXMLConfiguration(String id) {
		return configManager.getXML("/batchProcesses/" + id + ".xml");
	}

	public List<BatchProcess> getFinishedBatchProcesses() {
		BatchProcessListReader reader = newBatchProcessListReader(getProcessListXMLDocument());
		return reader.readFinishedBatchProcesses();
	}

	public List<BatchProcess> getPendingBatchProcesses() {
		BatchProcessListReader reader = newBatchProcessListReader(getProcessListXMLDocument());
		return reader.readPendingBatchProcesses();
	}

	public List<BatchProcess> getStandbyBatchProcesses() {
		BatchProcessListReader reader = newBatchProcessListReader(getProcessListXMLDocument());
		return reader.readStandbyBatchProcesses();
	}

	public int getAllBatchProcessesCount() {
		BatchProcessListReader reader = newBatchProcessListReader(getProcessListXMLDocument());
		return reader.getAllBatchProcessesCount();
	}

	XMLConfiguration getProcessListXMLConfiguration() {

		XMLConfiguration config = configManager.getXML(BATCH_PROCESS_LIST_PATH);
		if (config == null) {
			saveEmptyProcessListXMLDocument();

			config = getProcessListXMLConfiguration();
		}
		return config;
	}

	Document getProcessListXMLDocument() {
		return getProcessListXMLConfiguration().getDocument();
	}

	public List<String> getRecords(BatchProcess batchProcess) {

		XMLConfiguration batchProcessConfiguration = getExistingXMLConfiguration(batchProcess.getId());
		Document document = batchProcessConfiguration.getDocument();

		return newBatchProcessReader(document).getRecords();
	}

	DocumentAlteration newDeleteBatchProcessesAlteration(final List<String> ids) {
		return new DocumentAlteration() {

			@Override
			public void alter(Document document) {
				newBatchProcessListWriter(document).deleteBatchProcessesAlteration(ids);
			}
		};
	}

	DocumentAlteration newAddBatchProcessDocumentAlteration(final String id, final String query,
															final String collection,
															final LocalDateTime requestDateTime,
															final int recordsCount, final BatchProcessAction action) {
		return new DocumentAlteration() {

			@Override
			public void alter(Document document) {
				newBatchProcessListWriter(document)
						.addRecordBatchProcess(id, query, collection, requestDateTime, recordsCount, action);
			}
		};
	}

	DocumentAlteration newAddBatchProcessDocumentAlteration(final String id, final String query,
															final String collection,
															final LocalDateTime requestDateTime,
															final int recordsCount, final BatchProcessAction action,
															final String username, final String title) {
		return new DocumentAlteration() {

			@Override
			public void alter(Document document) {
				newBatchProcessListWriter(document)
						.addRecordBatchProcess(id, query, collection, requestDateTime, recordsCount, action, username, title);
			}
		};
	}

	DocumentAlteration newAddBatchProcessDocumentAlteration(final String id, final List<String> records,
															final String collection,
															final LocalDateTime requestDateTime,
															final int recordsCount, final BatchProcessAction action,
															final String username, final String title) {
		return new DocumentAlteration() {

			@Override
			public void alter(Document document) {
				newBatchProcessListWriter(document)
						.addRecordBatchProcess(id, records, collection, requestDateTime, recordsCount, action, username, title);
			}
		};
	}

	DocumentAlteration markAllBatchProcessAsPendingDocumentAlteration() {
		return new DocumentAlteration() {

			@Override
			public void alter(Document document) {
				newBatchProcessListWriter(document).markAllBatchProcessAsPending();
			}
		};
	}

	DocumentAlteration markBatchProcessAsPendingDocumentAlteration(final String id) {
		return new DocumentAlteration() {

			@Override
			public void alter(Document document) {
				newBatchProcessListWriter(document).markBatchProcessAsPending(id);
			}
		};
	}

	DocumentAlteration cancelStandByBatchProcessDocumentAlteration(final String id) {
		return new DocumentAlteration() {

			@Override
			public void alter(Document document) {
				newBatchProcessListWriter(document).cancelStandByBatchProcess(id);
			}
		};
	}

	DocumentAlteration cancelPendingBatchProcessDocumentAlteration(final String id) {
		return new DocumentAlteration() {

			@Override
			public void alter(Document document) {
				newBatchProcessListWriter(document).cancelPendingBatchProcess(id);
			}
		};
	}

	String newBatchProcessId() {
		return UUID.randomUUID().toString();
	}

	BatchProcessListReader newBatchProcessListReader(Document document) {
		return new BatchProcessListReader(document);
	}

	BatchProcessListWriter newBatchProcessListWriter(Document document) {
		return new BatchProcessListWriter(document);
	}

	BatchProcessReader newBatchProcessReader(Document document) {
		return new BatchProcessReader(document);
	}

	Document newDocument() {
		return new Document();
	}

	void saveEmptyProcessListXMLDocument() {
		Document document = new Document();

		BatchProcessListWriter writer = newBatchProcessListWriter(document);
		writer.createEmptyProcessList();

		configManager.add(BATCH_PROCESS_LIST_PATH, document);

	}

	private void startNextBatchProcess(XMLConfiguration xmlConfiguration, Document processList)
			throws OptimisticLockingConfiguration {
		BatchProcessListWriter writer = newBatchProcessListWriter(processList);
		writer.startNextBatchProcess(getCurrentTime());
		configManager.update(BATCH_PROCESS_LIST_PATH, xmlConfiguration.getHash(), processList);
	}

	public void registerBatchProcessesListUpdatedEvent(BatchProcessesListUpdatedEventListener listener) {
		listeners.add(listener);
	}

	@Override
	public void onConfigUpdated(String configPath) {
		for (BatchProcessesListUpdatedEventListener listener : listeners) {
			listener.onBatchProcessesListUpdated();
		}
	}

	public List<BatchProcess> getAllNonFinishedBatchProcesses() {
		BatchProcessListReader reader = newBatchProcessListReader(getProcessListXMLDocument());
		List<BatchProcess> batchProcesses = new ArrayList<>();
		if (reader.readCurrent() != null) {
			batchProcesses.add(reader.readCurrent());
		}
		batchProcesses.addAll(reader.readPendingBatchProcesses());
		batchProcesses.addAll(reader.readStandbyBatchProcesses());

		return batchProcesses;
	}

	@Override
	public void close() {

	}

	public void waitUntilAllFinished() {


		for (int i = 0; i < 10; i++) {
			for (BatchProcess batchProcess : getAllNonFinishedBatchProcesses()) {
				waitUntilFinished(batchProcess);
			}

			RecordsReindexingBackgroundAction recordsReindexingBackgroundAction = modelLayerFactory
					.getModelLayerBackgroundThreadsManager().getRecordsReindexingBackgroundAction();

			SearchServices searchServices = modelLayerFactory.newSearchServices();
			if (recordsReindexingBackgroundAction != null
				&& ReindexingServices.getReindexingInfos() == null) {

				LogicalSearchQuery query = new LogicalSearchQuery(fromEveryTypesOfEveryCollection().where(MARKED_FOR_REINDEXING).isTrue());
				query.filteredByStatus(StatusFilter.ALL);
				query.filteredByVisibilityStatus(VisibilityStatusFilter.ALL);
				query.setName("*SDK* BatchProcessesManager.waitUntilAllFinished()");

				while (searchServices.hasResults(query) &&
					   (modelLayerFactory.getRecordsCaches().areSummaryCachesInitialized() ||
						!modelLayerFactory.getConfiguration().isSummaryCacheEnabled())) {
					recordsReindexingBackgroundAction.run(false);
				}
			}
		}
	}


	public void waitUntilFinished(BatchProcess batchProcess) {

		while (get(batchProcess.getId()).getStatus() != BatchProcessStatus.FINISHED) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void markAsFinished(final BatchProcess batchProcess, final int errorsCount) {
		configManager.updateXML(BATCH_PROCESS_LIST_PATH, new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				new BatchProcessListWriter(document).markBatchProcessAsFinished(batchProcess, errorsCount);
			}
		});

		LoggingServices loggingServices = modelLayerFactory.newLoggingServices();
		loggingServices.updateBatchProcess(batchProcess);

		addFinishedBatchProcessIdToHistoryList(batchProcess.getId());
	}

	public void updateProgression(final RecordBatchProcess batchProcess, final int progressionIncrement,
								  final int errorsIncrement) {
		configManager.updateXML(BATCH_PROCESS_LIST_PATH, new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				new BatchProcessListWriter(document).incrementProgression(batchProcess, progressionIncrement, errorsIncrement);
			}
		});
	}

	public BatchProcessState getBatchProcessState(String batchProcessId) {
		return statusCache.get(batchProcessId);
	}

	public void updateBatchProcessState(String batchProcessId, BatchProcessState state) {
		statusCache.put(batchProcessId, state, WAS_MODIFIED);
	}

	private void addFinishedBatchProcessIdToHistoryList(String id) {
		int size = finishedBatchProcessIds.size();
		int limit = getBatchProcessMaximumHistorySize();
		if (size >= limit) {
			List<String> finishedBatchProcessIdsToRemove = new ArrayList<>();
			for (int i = 0; i < size - limit + 1; i++) {
				finishedBatchProcessIdsToRemove.add(finishedBatchProcessIds.remove(0));
			}
			delete(finishedBatchProcessIdsToRemove);
		}
		finishedBatchProcessIds.add(id);
	}

	private int getBatchProcessMaximumHistorySize() {
		return new ConstellioEIMConfigs(modelLayerFactory).getBatchProcessMaximumHistorySize();
	}
}
