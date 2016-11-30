package com.constellio.app.modules.rm.reports.factories.labels;

import java.util.List;

import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;

public class LabelsReportParameters {

	private final List<String> recordIds;

	private final LabelTemplate labelConfiguration;

	private final int startPosition;

	private final int numberOfCopies;

	public LabelsReportParameters(List<String> recordIds, LabelTemplate labelTemplate, int startPosition,
			int numberOfCopies) {
		this.recordIds = recordIds;
		this.labelConfiguration = labelTemplate;
		this.startPosition = startPosition;
		this.numberOfCopies = numberOfCopies;
	}

	public List<String> getRecordIds() {
		return recordIds;
	}

	public LabelTemplate getLabelConfiguration() {
		return labelConfiguration;
	}

	public int getStartPosition() {
		return startPosition;
	}

	public int getNumberOfCopies() {
		return numberOfCopies;
	}

}
