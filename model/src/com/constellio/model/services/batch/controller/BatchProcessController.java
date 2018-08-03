package com.constellio.model.services.batch.controller;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.utils.LoggerUncaughtExceptionHandler;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.services.batch.controller.BatchProcessControllerRuntimeException.ControllerAlreadyStarted;
import com.constellio.model.services.batch.manager.BatchProcessesListUpdatedEventListener;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchProcessController implements StatefulService, BatchProcessesListUpdatedEventListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(BatchProcessController.class);

	private final ModelLayerFactory modelLayerFactory;
	private final int numberOfRecordsPerTask;
	private final ModelLayerConfiguration configuration;
	BatchProcessControllerThread thread;

	public BatchProcessController(ModelLayerFactory modelLayerFactory, int numberOfRecordsPerTask) {
		this.modelLayerFactory = modelLayerFactory;
		this.numberOfRecordsPerTask = numberOfRecordsPerTask;
		this.modelLayerFactory.getBatchProcessesManager().registerBatchProcessesListUpdatedEvent(this);
		this.configuration = modelLayerFactory.getConfiguration();
	}

	@Override
	public final void initialize() {

	}

	public final void start() {
		if (thread != null) {
			throw new ControllerAlreadyStarted();
		}
		if (configuration.isBatchProcessesThreadEnabled()) {
			this.thread = newBatchProcessControllerThread();
			thread.setUncaughtExceptionHandler(LoggerUncaughtExceptionHandler.instance);
			thread.setName(modelLayerFactory.toResourceName("BatchProcessController"));
			thread.start();
		}
	}

	@Override
	public void close() {
		if (thread != null) {
			try {

				while (thread.isAlive()) {
					thread.stopRequested();
					thread.join(500);
				}

				thread = null;
			} catch (InterruptedException e) {
				//throw new RuntimeException(e);
			}
		}
	}

	public BatchProcessControllerThread newBatchProcessControllerThread() {
		return new BatchProcessControllerThread(modelLayerFactory, numberOfRecordsPerTask);
	}

	@Override
	public void onBatchProcessesListUpdated() {
		if (thread != null) {
			thread.notifyBatchProcessesListConfigUpdated();
		}

	}
}
