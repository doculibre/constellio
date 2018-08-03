package com.constellio.app.modules.robots.reports;

import com.constellio.app.ui.framework.reports.ReportWriter;
import com.constellio.app.ui.i18n.i18n;
import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.*;
import jxl.write.Number;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

public class DryRunReportWriter implements ReportWriter {
	private static final WritableFont.FontName FONT = WritableFont.TIMES;
	private static final int FONT_SIZE = 10;
	DryRunReportModel model;
	Locale locale;

	public DryRunReportWriter(DryRunReportModel model, Locale locale) {
		this.model = model;
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

	private void addHeader(WritableSheet sheet, List<String> columnsTitles)
			throws WriteException {
		WritableCellFormat boldFont = new WritableCellFormat(new WritableFont(FONT, FONT_SIZE, WritableFont.BOLD));

		CellView cv = new CellView();
		cv.setFormat(boldFont);
		cv.setAutosize(true);
		for (int i = 0; i < columnsTitles.size(); i++) {
			String columnTitle = columnsTitles.get(i);
			addString(sheet, boldFont, i, 0, columnTitle);
		}
	}

	private void createContent(WritableSheet sheet, List<List<Object>> lines)
			throws WriteException {
		WritableCellFormat font = new WritableCellFormat(new WritableFont(FONT, FONT_SIZE));

		for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
			List<Object> currentLine = lines.get(lineNumber);
			writeLine(sheet, currentLine, lineNumber + 1, font);
		}
	}

	private void writeLine(WritableSheet sheet, List<Object> currentLine, int lineNumber, WritableCellFormat font)
			throws WriteException {
		CellView cv = new CellView();
		cv.setFormat(font);
		cv.setAutosize(true);
		for (int columnNumber = 0; columnNumber < currentLine.size(); columnNumber++) {
			Object cellObject = currentLine.get(columnNumber);
			if (cellObject == null) {
				continue;
			}
			if (cellObject instanceof Float ||
				cellObject instanceof Integer ||
				cellObject instanceof Double) {
				addNumber(sheet, font, columnNumber, lineNumber, new Double(cellObject.toString()));
			} else {
				addString(sheet, font, columnNumber, lineNumber, cellObject.toString());
			}
		}
	}

	private void addString(WritableSheet sheet, WritableCellFormat font, int column, int row, String s)
			throws WriteException {
		Label label = new Label(column, row, s, font);
		sheet.addCell(label);
	}

	private void addNumber(WritableSheet sheet, WritableCellFormat font, int column, int row,
						   double d)
			throws WriteException {
		Number number = new Number(column, row, d, font);
		sheet.addCell(number);
	}

}
