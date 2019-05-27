package com.constellio.app.modules.rm.reports.model.decommissioning;

import com.constellio.app.ui.i18n.i18n;

import java.util.*;

public class DecommissioningListExcelReportModel {
    private final List<String> columnsTitles = new ArrayList<>();
    private Map<String, List<List<Object>>> resultBySheet = new HashMap<>();
    public static final String SINGLE_SHEET_CAPTION_KEY = "Report.sheetName";

    public List<List<Object>> getResults(String sheet) {
        if (resultBySheet.keySet().size() > 0) {
            return resultBySheet.get(sheet);
        } else {
            return Collections.emptyList();
        }
    }

    public List<String> getSheetNames() {
        if (resultBySheet.keySet().size() > 0) {
            return new ArrayList<>(resultBySheet.keySet());
        } else {
            return Arrays.asList(i18n.$(SINGLE_SHEET_CAPTION_KEY));
        }
    }

    public List<String> getColumnsTitles() {
        return new ArrayList<>(columnsTitles);
    }

    public void addTitle(String title) {
        columnsTitles.add(title);
    }

    public void addLine(String sheet, List<Object> recordLine) {
        if (resultBySheet.get(sheet) == null) {
            resultBySheet.put(sheet, new ArrayList<List<Object>>());
        }

        resultBySheet.get(sheet).add(recordLine);
    }
}
