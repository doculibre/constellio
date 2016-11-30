package com.constellio.app.modules.rm.reports.factories;

import java.util.List;

public class ExampleReportParameters {

	private final List<String> recordIds;

	public ExampleReportParameters(List<String> recordIds) {
		this.recordIds = recordIds;
	}

	public List<String> getRecordIds() {
		return recordIds;
	}

}
