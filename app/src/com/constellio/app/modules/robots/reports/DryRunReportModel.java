package com.constellio.app.modules.robots.reports;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

public class DryRunReportModel {
	private final List<List<String>> results = new ArrayList<>();
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

	public void addLine(List<String> recordLine) {
		results.add(recordLine);
	}
}
