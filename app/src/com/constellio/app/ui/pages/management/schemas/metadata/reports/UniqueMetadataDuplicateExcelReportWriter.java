package com.constellio.app.ui.pages.management.schemas.metadata.reports;

import com.constellio.app.modules.rm.reports.builders.excel.BaseExcelReportWriter;
import com.constellio.app.ui.framework.reports.ReportWriter;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import java.io.IOException;
import java.io.OutputStream;

public class UniqueMetadataDuplicateExcelReportWriter extends BaseExcelReportWriter implements ReportWriter {
	private final UniqueMetadataDuplicateExcelReportModel model;

	public UniqueMetadataDuplicateExcelReportWriter(UniqueMetadataDuplicateExcelReportModel model) {

		this.model = model;
	}

	@Override
	public String getFileExtension() {
		return "xls";
	}

	@Override
	public void write(OutputStream output) throws IOException {
		WorkbookSettings wbSettings = new WorkbookSettings();

		wbSettings.setLocale(model.getLocale());

		WritableWorkbook workbook = Workbook.createWorkbook(output, wbSettings);

		model.getSheetNames().forEach(sheetName -> {

			workbook.createSheet(sheetName, 0);
			WritableSheet excelSheet = workbook.getSheet(0);

			if (model.hasHeader(sheetName)) {
				try {
					addHeader(excelSheet, model.getColumnsTitles(sheetName));

				} catch (WriteException e) {
					throw new RuntimeException(e);
				}
			}

			if (model.hasContent(sheetName)) {
				try {
					createContent(excelSheet, model.getContent(sheetName));
				} catch (WriteException e) {
					throw new RuntimeException(e);
				}
			}
		});

		workbook.write();
		try {
			workbook.close();
		} catch (WriteException e) {
			throw new RuntimeException(e);
		}
	}
}
