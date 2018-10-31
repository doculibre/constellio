package com.constellio.app.modules.rm.reports.builders.search;

import com.constellio.app.modules.rm.reports.builders.excel.BaseExcelReportWriter;
import com.constellio.app.modules.rm.reports.model.search.SearchResultReportModel;
import com.constellio.app.ui.framework.reports.ReportWriter;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.model.conf.FoldersLocator;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

public class SearchResultReportWriter extends BaseExcelReportWriter implements ReportWriter {

	SearchResultReportModel model;
	FoldersLocator foldersLocator;
	Locale locale;

	public SearchResultReportWriter(SearchResultReportModel model, FoldersLocator foldersLocator, Locale locale) {
		this.model = model;
		this.foldersLocator = foldersLocator;
		this.locale = locale;
	}

	@Override
	public String getFileExtension() {
		return "xls";
	}

	@Override
	public void write(OutputStream output)
			throws IOException {
		WorkbookSettings wbSettings = new WorkbookSettings();

		wbSettings.setLocale(locale);

		WritableWorkbook workbook = Workbook.createWorkbook(output, wbSettings);
		workbook.createSheet(i18n.$("Report.sheetName"), 0);
		WritableSheet excelSheet = workbook.getSheet(0);
		try {
			addHeader(excelSheet, model.getColumnsTitles());
		} catch (WriteException e) {
			throw new RuntimeException(e);
		}
		try {
			createContent(excelSheet, model.getResults());
		} catch (WriteException e) {
			throw new RuntimeException(e);
		}

		workbook.write();
		try {
			workbook.close();
		} catch (WriteException e) {
			throw new RuntimeException(e);
		}
	}
}
