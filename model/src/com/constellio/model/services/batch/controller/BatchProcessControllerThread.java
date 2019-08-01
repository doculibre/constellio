package com.constellio.model.services.batch.controller;

import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.services.bigVault.solr.SolrUtils;
import com.constellio.data.threads.ConstellioThread;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.batchprocess.AsyncTask;
import com.constellio.model.entities.batchprocess.AsyncTaskBatchProcess;
import com.constellio.model.entities.batchprocess.AsyncTaskExecutionParams;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.RecordBatchProcess;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.BatchProcessReport;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.batch.state.BatchProcessProgressionServices;
import com.constellio.model.services.batch.state.InMemoryBatchProcessProgressionServices;
import com.constellio.model.services.batch.state.StoredBatchProcessPart;
import com.constellio.model.services.batch.xml.list.BatchProcessListWriterRuntimeException;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.iterators.RecordSearchResponseIterator;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.constellio.model.conf.FoldersLocatorMode.PROJECT;

public class BatchProcessControllerThread extends ConstellioThread {

	private static final String RESOURCE_NAME = "BatchProcessControllerThread";

	private static final Logger LOGGER = LoggerFactory.getLogger(BatchProcessControllerThread.class);
	private final int numberOfRecordsPerTask;
	private final BatchProcessesManager batchProcessesManager;
	private final RecordServices recordServices;
	private final MetadataSchemasManager schemasManager;
	private final SearchServices searchServices;
	private final ModelLayerFactory modelLayerFactory;
	private boolean stopRequested;
	private Semaphore newEventSemaphore;
	private AtomicLong completed = new AtomicLong();
	private UserServices userServices;
	private boolean nextBatchProcessReportExistenceCheckRequired;

	public BatchProcessControllerThread(ModelLayerFactory modelLayerFactory, int numberOfRecordsPerTask) {
		super(modelLayerFactory.toResourceName(RESOURCE_NAME));
		this.modelLayerFactory = modelLayerFactory;
		this.batchProcessesManager = modelLayerFactory.getBatchProcessesManager();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.numberOfRecordsPerTask = numberOfRecordsPerTask;
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.newEventSemaphore = new Semaphore(1);
		this.userServices = modelLayerFactory.newUserServices();

		if (modelLayerFactory == null || modelLayerFactory.getDataLayerFactory() == null
			|| modelLayerFactory.getDataLayerFactory().getLeaderElectionService() == null) {
			throw new IllegalArgumentException("modelLayerFactory parameter is invalid");
		}

		this.nextBatchProcessReportExistenceCheckRequired = new FoldersLocator().getFoldersLocatorMode() != PROJECT;
	}

	@Override
	public void execute() {
		while (!isStopRequested()) {
			try {
				process();
			} catch (NullPointerException t) {
				t.printStackTrace();
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				LOGGER.error("Error while batch processing", t);

			} catch (Throwable t) {
				LOGGER.error("Error while batch processing", t);
			}
		}
	}

	void process()
			throws Exception {

		if (modelLayerFactory.getDataLayerFactory().getLeaderElectionService().isCurrentNodeLeader()
			&& new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager()).isInBatchProcessesSchedule()) {
			final BatchProcess batchProcess = batchProcessesManager.getCurrentBatchProcess();
			if (batchProcess != null) {
				try {
					final BatchProcessState state = new BatchProcessState();
					if (batchProcess instanceof RecordBatchProcess) {
						RecordBatchProcess recordBatchProcess = (RecordBatchProcess) batchProcess;
						if (recordBatchProcess.getRecords() != null) {
							processFromIds(recordBatchProcess);
						} else {
							processFromQuery(recordBatchProcess);
						}
					} else if (batchProcess instanceof AsyncTaskBatchProcess) {

						final AsyncTaskBatchProcess process = (AsyncTaskBatchProcess) batchProcess;
						AsyncTask task = process.getTask();
						final Class<? extends AsyncTask> taskClass = task.getClass();
						AsyncTaskExecutionParams params = new AsyncTaskExecutionParams() {

							@Override
							public String getCollection() {
								return process.getCollection();
							}

							@Override
							public void logWarning(String code, Map<String, Object> parameters) {
								state.getWarnings().add(new ValidationError(taskClass, code, parameters));
								batchProcessesManager.updateBatchProcessState(batchProcess.getId(), state);
							}

							@Override
							public void logError(String code, Map<String, Object> parameters) {
								state.getErrors().add(new ValidationError(taskClass, code, parameters));
								batchProcessesManager.updateBatchProcessState(batchProcess.getId(), state);
							}

							@Override
							public void incrementProgression(int numberToAdd) {
								state.incrementCurrentlyProcessed(numberToAdd);
								batchProcessesManager.updateBatchProcessState(batchProcess.getId(), state);
							}

							@Override
							public void setProgressionUpperLimit(long progressionUpperLimit) {
								state.setTotalToProcess(progressionUpperLimit);
								batchProcessesManager.updateBatchProcessState(batchProcess.getId(), state);
							}

							@Override
							public AsyncTaskBatchProcess getBatchProcess() {
								return process;
							}

						};
						task.execute(params);
						batchProcessesManager.updateBatchProcessState(batchProcess.getId(), state);
						batchProcessesManager.markAsFinished(batchProcess, 0);
					}
				} catch (BatchProcessListWriterRuntimeException.BatchProcessAlreadyFinished alreadyFinished) {

				} catch (Exception e) {
					batchProcessesManager.markAsFinished(batchProcess, 1);
					throw e;
				}
			}
		}
		waitUntilNotified();
	}

	private void processFromIds(RecordBatchProcess batchProcess)
			throws Exception {
		BatchProcessProgressionServices batchProcessProgressionServices = new InMemoryBatchProcessProgressionServices();
		BatchProcessReport report = getLinkedBatchProcessReport(batchProcess);
		RecordFromIdListIterator iterator = new RecordFromIdListIterator(batchProcess.getRecords(), modelLayerFactory);
		BatchBuilderIterator<Record> batchIterator = new BatchBuilderIterator<>(iterator, 1000);
		StoredBatchProcessPart previousPart = batchProcessProgressionServices.getLastBatchProcessPart(batchProcess);
		if (previousPart != null) {
			iterator.beginAfterId(previousPart.getLastId());
		}

		ForkJoinPool pool = newForkJoinPool();
		TaskList taskList = new TaskList(pool);

		List<String> recordsWithErrors = new ArrayList<>();

		while (batchIterator.hasNext()) {
			int oldErrorCount = recordsWithErrors.size();
			List<String> newErrors = new ArrayList<>();
			List<Record> records = batchIterator.next();
			int index = previousPart == null ? 0 : previousPart.getIndex() + 1;
			String firstId = records.get(0).getId();
			String lastId = records.get(records.size() - 1).getId();
			StoredBatchProcessPart storedBatchProcessPart = new StoredBatchProcessPart(batchProcess.getId(), index, firstId,
					lastId, false, false);

			batchProcessProgressionServices.markNewPartAsStarted(storedBatchProcessPart);
			List<BatchProcessTask> tasks = newBatchProcessTasksFactory(taskList).createBatchProcessTasks(batchProcess,
					records, recordsWithErrors, numberOfRecordsPerTask, schemasManager, report);

			for (BatchProcessTask task : tasks) {
				newErrors = pool.invoke(task);
				recordsWithErrors.addAll(newErrors);
			}
			batchProcessProgressionServices.markPartAsFinished(storedBatchProcessPart);
			previousPart = storedBatchProcessPart;
			report.addSkippedRecords(newErrors);
			updateBatchProcessReport(report);

			if (batchIterator.hasNext()) {
				batchProcessesManager.updateProgression(batchProcess, records.size(), recordsWithErrors.size() - oldErrorCount);
			}
		}
		pool.shutdown();
		pool.awaitTermination(1, TimeUnit.DAYS);
		batchProcessesManager.markAsFinished(batchProcess, recordsWithErrors.size());
	}

	private void processFromQuery(RecordBatchProcess batchProcess)
			throws Exception {
		BatchProcessProgressionServices batchProcessProgressionServices = new InMemoryBatchProcessProgressionServices();
		BatchProcessReport report = getLinkedBatchProcessReport(batchProcess);
		ModifiableSolrParams params = new ModifiableSolrParams();
		try {
			params = SolrUtils.parseQueryString(batchProcess.getQuery());
		} catch (Exception e) {
			e.printStackTrace();
		}

		params.set("sort", "principalPath_s asc, id asc");

		RecordSearchResponseIterator iterator = new RecordSearchResponseIterator(modelLayerFactory, params, 1000, true, null);
		BatchBuilderIterator<Record> batchIterator = new BatchBuilderIterator<>(iterator, 1000);
		StoredBatchProcessPart previousPart = batchProcessProgressionServices.getLastBatchProcessPart(batchProcess);
		if (previousPart != null) {
			iterator.beginAfterId(previousPart.getLastId());
		}

		ForkJoinPool pool = newForkJoinPool();
		TaskList taskList = new TaskList(pool);

		List<String> recordsWithErrors = new ArrayList<>();

		while (batchIterator.hasNext()) {
			int oldErrorCount = recordsWithErrors.size();
			List<String> newErrors = new ArrayList<>();
			List<Record> records = batchIterator.next();
			int index = previousPart == null ? 0 : previousPart.getIndex() + 1;
			String firstId = records.get(0).getId();
			String lastId = records.get(records.size() - 1).getId();
			StoredBatchProcessPart storedBatchProcessPart = new StoredBatchProcessPart(batchProcess.getId(), index, firstId,
					lastId, false, false);

			batchProcessProgressionServices.markNewPartAsStarted(storedBatchProcessPart);
			List<BatchProcessTask> tasks = newBatchProcessTasksFactory(taskList).createBatchProcessTasks(batchProcess,
					records, recordsWithErrors, numberOfRecordsPerTask, schemasManager, report);

			for (BatchProcessTask task : tasks) {
				newErrors = pool.invoke(task);
				recordsWithErrors.addAll(newErrors);
			}
			batchProcessProgressionServices.markPartAsFinished(storedBatchProcessPart);
			previousPart = storedBatchProcessPart;
			report.addSkippedRecords(newErrors);
			updateBatchProcessReport(report);
			if (batchIterator.hasNext()) {
				batchProcessesManager.updateProgression(batchProcess, records.size(), recordsWithErrors.size() - oldErrorCount);
			}
		}
		pool.shutdown();
		pool.awaitTermination(1, TimeUnit.DAYS);
		batchProcessesManager.markAsFinished(batchProcess, recordsWithErrors.size());
	}

	private BatchProcessReport getLinkedBatchProcessReport(BatchProcess batchProcess) {
		BatchProcessReport report = null;
		String collection = batchProcess.getCollection();
		if (collection != null) {
			SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);

			String userId = null;
			if (StringUtils.isNotBlank(batchProcess.getUsername())) {
				User user = userServices.getUserRecordInCollection(batchProcess.getUsername(), collection);
				userId = user != null ? user.getId() : null;
			}
			if (nextBatchProcessReportExistenceCheckRequired) {

				nextBatchProcessReportExistenceCheckRequired = false;
				try {
					MetadataSchema batchProcessReportSchema = schemasManager.getSchemaTypes(collection)
							.getSchema(BatchProcessReport.FULL_SCHEMA);
					Record reportRecord = searchServices.searchSingleResult(LogicalSearchQueryOperators.from(batchProcessReportSchema)
							.where(batchProcessReportSchema.getMetadata(BatchProcessReport.LINKED_BATCH_PROCESS))
							.isEqualTo(batchProcess.getId()));
					if (reportRecord != null) {
						report = new BatchProcessReport(reportRecord, schemasManager.getSchemaTypes(collection));
					}
				} catch (Exception e) {
					//Ok
				}
			}

			if (report == null) {
				report = schemas.newBatchProcessReport();
				report.setLinkedBatchProcess(batchProcess.getId());
				report.setCreatedBy(userId);
			}
		}
		return report;
	}

	private void updateBatchProcessReport(BatchProcessReport report) {
		try {
			Transaction transaction = new Transaction();
			transaction.addUpdate(report.getWrappedRecord());
			transaction.setRecordFlushing(RecordsFlushing.LATER());
			recordServices.execute(transaction);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void waitUntilNotified()
			throws InterruptedException {
		newEventSemaphore.tryAcquire(500, TimeUnit.MILLISECONDS);

	}

	public boolean isStopRequested() {
		return stopRequested;
	}

	public void stopRequested() {
		this.stopRequested = true;
		if (newEventSemaphore.availablePermits() == 0) {
			newEventSemaphore.release();
		}
	}

	public void notifyBatchProcessesListConfigUpdated() {
		newEventSemaphore.release();
	}

	BatchProcessTasksFactory newBatchProcessTasksFactory(TaskList taskList) {
		return new BatchProcessTasksFactory(recordServices, searchServices, userServices, taskList, modelLayerFactory);
	}

	ForkJoinPool newForkJoinPool() {
		return new ForkJoinPool(1);
	}

}
