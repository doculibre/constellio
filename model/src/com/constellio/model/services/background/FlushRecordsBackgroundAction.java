package com.constellio.model.services.background;

import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;

public class FlushRecordsBackgroundAction implements Runnable {

	private RecordServices recordServices;
	boolean firstTimeExecuted = true;

	public FlushRecordsBackgroundAction(ModelLayerFactory modelLayerFactory) {
		this.recordServices = modelLayerFactory.newRecordServices();
	}

	@Override
	public synchronized void run() {
		if (!firstTimeExecuted) {
			recordServices.flushRecords();
		} else {
			firstTimeExecuted = false;
		}
	}
}
