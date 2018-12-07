package com.constellio.app.modules.rm.reports.model.administration.plan;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdministrativeUnitExcelReportModel {
	private final List<String> columnsTitles = new ArrayList<>();
	private Map<String, List<Object>> resultBysheet = new HashMap<>();
	public static final String SINGLE_SHEET_CAPTION_KEY = "Report.sheetName";

	public List<List<Object>> getResults(String sheet) {
		return new ArrayList<>(CollectionUtils.unmodifiableCollection(resultBysheet.get(sheet)));
	}

	public List<String> getSheetNames() {
		return new ArrayList<>(resultBysheet.keySet());
	}

	public List<String> getColumnsTitles() {
		return new ArrayList<>(CollectionUtils.unmodifiableCollection(columnsTitles));
	}

	public void addTitle(String title) {
		columnsTitles.add(title);
	}

	public void addLine(String sheet, List<Object> recordLine) {
		if(resultBysheet.get(sheet) == null) {
			resultBysheet.put(sheet, new ArrayList<>());
		}

		resultBysheet.get(sheet).add(recordLine);
	}
}
