/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.batch.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.threads.ConstellioThread;
import com.constellio.model.entities.batchprocess.BatchProcessPart;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;

public class BatchProcessControllerThread extends ConstellioThread {

	private static final String RESOURCE_NAME = "BatchProcessControllerThread";

	private static final Logger LOGGER = LoggerFactory.getLogger(BatchProcessControllerThread.class);
	private final int numberOfRecordsPerTask;
	private final BatchProcessesManager batchProcessesManager;
	private final RecordServices recordServices;
	private final MetadataSchemasManager schemasManager;
	private final SearchServices searchServices;
	private boolean stopRequested;
	private Semaphore newEventSemaphore;
	private AtomicLong completed = new AtomicLong();

	public BatchProcessControllerThread(BatchProcessesManager batchProcessesManager, RecordServices recordServices,
			int numberOfRecordsPerTask, MetadataSchemasManager schemasManager, SearchServices searchServices) {
		super(RESOURCE_NAME);
		this.batchProcessesManager = batchProcessesManager;
		this.recordServices = recordServices;
		this.numberOfRecordsPerTask = numberOfRecordsPerTask;
		this.schemasManager = schemasManager;
		this.searchServices = searchServices;
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
		BatchProcessPart batchPart = batchProcessesManager.getCurrentBatchProcessPart();
		while (batchPart != null) {
			batchPart = processBatchProcessPart(batchPart);
		}
		//newEventSemaphore.release();
		waitUntilNotified();
	}

	void waitUntilNotified()
			throws InterruptedException {
		newEventSemaphore.acquire();

	}

	BatchProcessPart processBatchProcessPart(BatchProcessPart batchPart)
			throws Exception {
		List<String> recordsWithErrors = new ArrayList<>();

		ForkJoinPool pool = newForkJoinPool();
		TaskList taskList = new TaskList(pool);
		List<BatchProcessTask> tasks = newBatchProcessTasksFactory(taskList).createBatchProcessTasks(batchPart.getBatchProcess(),
				batchPart.getRecordIds(), recordsWithErrors, numberOfRecordsPerTask, schemasManager);

		for (BatchProcessTask task : tasks) {
			pool.execute(task);
		}
		pool.shutdown();
		pool.awaitTermination(1, TimeUnit.DAYS);

		return batchProcessesManager.markBatchProcessPartAsFinishedAndGetAnotherPart(batchPart,
				recordsWithErrors);
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
		return new ForkJoinPool();
	}

}
