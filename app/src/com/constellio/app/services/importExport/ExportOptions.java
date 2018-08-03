package com.constellio.app.services.importExport;

import com.constellio.app.services.importExport.records.RecordExportOptions;

public class ExportOptions {

	RecordExportOptions options;

	public RecordExportOptions getOptions() {
		return options;
	}

	public ExportOptions setOptions(RecordExportOptions options) {
		this.options = options;
		return this;
	}
}
