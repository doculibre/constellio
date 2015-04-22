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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.utils.LoggerUncaughtExceptionHandler;
import com.constellio.model.services.batch.controller.BatchProcessControllerRuntimeException.ControllerAlreadyStarted;
import com.constellio.model.services.batch.manager.BatchProcessesListUpdatedEventListener;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;

public class BatchProcessController implements StatefulService, BatchProcessesListUpdatedEventListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(BatchProcessController.class);

	private final BatchProcessesManager batchProcessesManager;
	private final RecordServices recordServices;
	private final SearchServices searchServices;
	private final MetadataSchemasManager metadataSchemasManager;
	private final int numberOfRecordsPerTask;
	BatchProcessControllerThread thread;

	public BatchProcessController(BatchProcessesManager batchProcessesManager, RecordServices recordServices,
			int numberOfRecordsPerTask, MetadataSchemasManager metadataSchemasManager, SearchServices searchServices) {
		this.batchProcessesManager = batchProcessesManager;
		this.recordServices = recordServices;
		this.numberOfRecordsPerTask = numberOfRecordsPerTask;
		this.metadataSchemasManager = metadataSchemasManager;
		this.batchProcessesManager.registerBatchProcessesListUpdatedEvent(this);
		this.searchServices = searchServices;
	}

	@Override
	public final void initialize() {
		if (thread != null) {
			throw new ControllerAlreadyStarted();
		}
		this.thread = newBatchProcessControllerThread();
		thread.setUncaughtExceptionHandler(LoggerUncaughtExceptionHandler.instance);
		thread.setName("BatchProcessController");
		thread.start();
	}

	@Override
	public void close() {
		if (thread != null) {
			try {
				thread.stopRequested();
				thread.join();
				thread = null;
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public BatchProcessControllerThread newBatchProcessControllerThread() {
		return new BatchProcessControllerThread(batchProcessesManager, recordServices, numberOfRecordsPerTask,
				metadataSchemasManager, searchServices);
	}

	@Override
	public void onBatchProcessesListUpdated() {
		if (thread != null) {
			thread.notifyBatchProcessesListConfigUpdated();
		}

	}
}
