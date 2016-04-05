package com.constellio.model.services.batch.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.solr.common.params.ModifiableSolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.threads.ConstellioThread;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.batch.state.BatchProcessProgressionServices;
import com.constellio.model.services.batch.state.InMemoryBatchProcessProgressionServices;
import com.constellio.model.services.batch.state.StoredBatchProcessPart;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.iterators.RecordSearchResponseIterator;

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

	public BatchProcessControllerThread(ModelLayerFactory modelLayerFactory, int numberOfRecordsPerTask) {
		super(RESOURCE_NAME);
		this.modelLayerFactory = modelLayerFactory;
		this.batchProcessesManager = modelLayerFactory.getBatchProcessesManager();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.numberOfRecordsPerTask = numberOfRecordsPerTask;
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.newEventSemaphore = new Semaphore(1);
	}

	@Override
	public void execute() {
		try {
			while (!isStopRequested()) {
				process();
			}
		} catch (Throwable t) {
			LOGGER.error("Error while batch processing", t);
		}
	}

	void process()
			throws Exception {

		BatchProcess batchProcess = batchProcessesManager.getCurrentBatchProcess();
		if (batchProcess != null) {
			BatchProcessProgressionServices batchProcessProgressionServices = new InMemoryBatchProcessProgressionServices();

			ModifiableSolrParams params = new ModifiableSolrParams();
			params.set("q", batchProcess.getQuery());
			params.set("sort", "principalPath_s asc, id asc");

			RecordSearchResponseIterator iterator = new RecordSearchResponseIterator(modelLayerFactory, params, 100, true);
			BatchBuilderIterator<Record> batchIterator = new BatchBuilderIterator<>(iterator, 100);
			StoredBatchProcessPart previousPart = batchProcessProgressionServices.getLastBatchProcessPart(batchProcess);
			if (previousPart != null) {
				iterator.beginAfterId(previousPart.getLastId());
			}

			ForkJoinPool pool = newForkJoinPool();
			TaskList taskList = new TaskList(pool);

			List<String> recordsWithErrors = new ArrayList<>();

			while (batchIterator.hasNext()) {
				List<Record> records = batchIterator.next();
				int index = previousPart == null ? 0 : previousPart.getIndex() + 1;
				String firstId = records.get(0).getId();
				String lastId = records.get(records.size() - 1).getId();
				StoredBatchProcessPart storedBatchProcessPart = new StoredBatchProcessPart(batchProcess.getId(), index, firstId,
						lastId, false, false);

				//System.out.println("processing batch #" + index + " [" + firstId + "-" + lastId + "]");
				batchProcessProgressionServices.markNewPartAsStarted(storedBatchProcessPart);
				List<BatchProcessTask> tasks = newBatchProcessTasksFactory(taskList).createBatchProcessTasks(batchProcess,
						records, recordsWithErrors, numberOfRecordsPerTask, schemasManager);

				for (BatchProcessTask task : tasks) {
					List<String> errors = pool.invoke(task);
					recordsWithErrors.addAll(errors);
				}
				batchProcessProgressionServices.markPartAsFinished(storedBatchProcessPart);
				previousPart = storedBatchProcessPart;
			}
			pool.shutdown();
			pool.awaitTermination(1, TimeUnit.DAYS);
			batchProcessesManager.markAsFinished(batchProcess, recordsWithErrors.size());
		}
		//newEventSemaphore.release();
		waitUntilNotified();
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
		return new BatchProcessTasksFactory(recordServices, searchServices, taskList);
	}

	ForkJoinPool newForkJoinPool() {
		return new ForkJoinPool(2);
	}

}
