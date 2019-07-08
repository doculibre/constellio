package com.constellio.app.modules.rm.reports.model.decommissioning;

import com.constellio.app.ui.i18n.i18n;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class DecommissioningListExcelReportModel {
	private final List<String> columnsTitles = new ArrayList<>();
	private List<List<Object>> results = new ArrayList<>();
	public static final String SINGLE_SHEET_CAPTION_KEY = "Report.sheetName";

	public List<List<Object>> getResults() {
		return new ArrayList<>(CollectionUtils.unmodifiableCollection(results));
	}

	public String getSheetName() {
		return i18n.$(SINGLE_SHEET_CAPTION_KEY);
	}

	public List<String> getColumnsTitles() {
		return new ArrayList<>(columnsTitles);
	}

	public void addTitle(String title) {
		columnsTitles.add(title);
	}

	public void addLine(List<Object> recordLine) {
		results.add(recordLine);
	}
}
