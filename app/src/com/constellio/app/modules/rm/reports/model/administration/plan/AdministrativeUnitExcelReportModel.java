package com.constellio.app.modules.rm.reports.model.administration.plan;

import com.constellio.app.ui.i18n.i18n;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdministrativeUnitExcelReportModel {
	private final List<String> columnsTitles = new ArrayList<>();
	private Map<String, List<Object>> resultBysheet = new HashMap<>();
	public static final String SINGLE_SHEET_CAPTION_KEY = "Report.sheetName";

	public List<List<Object>> getResults(String sheet) {
		if(resultBysheet.keySet().size() > 0) {
			return new ArrayList<>(CollectionUtils.unmodifiableCollection(resultBysheet.get(sheet)));
		} else {
			return Collections.emptyList();
		}
	}

	public List<String> getSheetNames() {
		if(resultBysheet.keySet().size() > 0) {
			return new ArrayList<>(resultBysheet.keySet());
		} else {
			return Arrays.asList(i18n.$(SINGLE_SHEET_CAPTION_KEY));
		}
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
