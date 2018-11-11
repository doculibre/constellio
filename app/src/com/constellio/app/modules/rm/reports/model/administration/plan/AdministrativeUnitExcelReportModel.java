package com.constellio.app.modules.rm.reports.model.administration.plan;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class AdministrativeUnitExcelReportModel {
	private final List<List<Object>> results = new ArrayList<>();
	private final List<String> columnsTitles = new ArrayList<>();

	public List<List<Object>> getResults() {
		return new ArrayList<>(CollectionUtils.unmodifiableCollection(results));
	}

	public List<String> getColumnsTitles() {
		return new ArrayList<>(CollectionUtils.unmodifiableCollection(columnsTitles));
	}

	public void addTitle(String title) {
		columnsTitles.add(title);
	}

	public void addLine(List<Object> recordLine) {
		results.add(recordLine);
	}
}
