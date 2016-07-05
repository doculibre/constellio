package com.constellio.app.modules.es.services.crawler;

import com.constellio.model.services.records.RecordLogicalDeleteOptions;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;

public class DeleteEventOptions {

	RecordLogicalDeleteOptions logicalDeleteOptions = new RecordLogicalDeleteOptions();

	RecordPhysicalDeleteOptions physicalDeleteOptions = new RecordPhysicalDeleteOptions();

	public RecordLogicalDeleteOptions getLogicalDeleteOptions() {
		return logicalDeleteOptions;
	}

	public DeleteEventOptions setLogicalDeleteOptions(
			RecordLogicalDeleteOptions logicalDeleteOptions) {
		this.logicalDeleteOptions = logicalDeleteOptions;
		return this;
	}

	public RecordPhysicalDeleteOptions getPhysicalDeleteOptions() {
		return physicalDeleteOptions;
	}

	public DeleteEventOptions setPhysicalDeleteOptions(
			RecordPhysicalDeleteOptions physicalDeleteOptions) {
		this.physicalDeleteOptions = physicalDeleteOptions;
		return this;
	}
}
