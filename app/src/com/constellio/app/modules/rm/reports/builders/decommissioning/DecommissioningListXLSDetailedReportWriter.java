package com.constellio.app.modules.rm.reports.builders.decommissioning;

import com.constellio.app.modules.rm.reports.model.decommissioning.DecommissioningListXLSDetailedReportModel;
import com.constellio.app.ui.framework.reports.ReportWriter;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.*;
import jxl.write.Number;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


public class DecommissioningListXLSDetailedReportWriter implements ReportWriter
{
	private static final WritableFont.FontName FONT = WritableFont.TIMES;
	private static final int FONT_SIZE = 10;
	private WritableCellFormat font;
	private WritableCellFormat boldFont;

	Locale locale;
	DecommissioningListXLSDetailedReportModel model;

	public DecommissioningListXLSDetailedReportWriter(DecommissioningListXLSDetailedReportModel model, Locale locale) {
		this.model = model;
		this.locale = locale;
	}

	@Override
	public String getFileExtension() {
		return "xls";
	}

	@Override
	public void write(OutputStream output) throws IOException {
		WorkbookSettings wbSettings = new WorkbookSettings();
		wbSettings.setLocale(locale);

		WritableWorkbook workbook = Workbook.createWorkbook(output, wbSettings);
		font = new WritableCellFormat(new WritableFont(FONT, FONT_SIZE));
		boldFont = new WritableCellFormat(new WritableFont(FONT, FONT_SIZE, WritableFont.BOLD));

		try {
			createHeader(workbook, 0, 3);
			createValidation(workbook, 1, 4);
			createFolder(workbook, 2, 7);
			createExclusion(workbook, 3, 7);
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

	private void createHeader(WritableWorkbook workbook, int sheetIndex, int columnCount) throws WriteException {
		WritableSheet sheet = createSheet(workbook, sheetIndex, columnCount, model.getHeaderSheetName());

		for(int i = 0; i < model.getHeaderTitles().size(); i++) {
			writeLine(sheet, Arrays.asList(model.getHeaderTitles().get(i), model.getHeaderInfos().get(i)), i, font,
					boldFont);
		}

		int commentLineNumber = model.getHeaderTitles().size() + 1;
		writeLine(sheet, model.getCommentTitles(), commentLineNumber, boldFont);
		for(int i = 0; i < model.getComments().size(); i++) {
			writeLine(sheet, model.getComments().get(i), commentLineNumber + 1 + i, font);
		}
	}

	private void createValidation(WritableWorkbook workbook, int sheetIndex, int columnCount) throws WriteException {
		WritableSheet sheet = createSheet(workbook, sheetIndex, columnCount, model.getValidationSheetName());

		writeLine(sheet, model.getValidationTitles(), 0, boldFont);
		for (int i = 0; i < model.getValidations().size(); i++) {
			writeLine(sheet, model.getValidations().get(i), 1 + i, font);
		}
	}

	private void createFolder(WritableWorkbook workbook, int sheetIndex, int columnCount) throws WriteException {
		WritableSheet sheet = createSheet(workbook, sheetIndex, columnCount, model.getFolderSheetName());

		writeLine(sheet, model.getFolderTitles(), 0, boldFont);
		for (int i = 0; i < model.getFolders().size(); i++) {
			writeLine(sheet, model.getFolders().get(i), 1 + i, font);
		}
	}

	private void createExclusion(WritableWorkbook workbook, int sheetIndex, int columnCount) throws WriteException {
		WritableSheet sheet = createSheet(workbook, sheetIndex, columnCount, model.getExclusionSheetName());

		writeLine(sheet, model.getFolderTitles(), 0, boldFont);
		for (int i = 0; i < model.getExclusions().size(); i++) {
			writeLine(sheet, model.getExclusions().get(i), 1 + i, font);
		}
	}

	private WritableSheet createSheet(WritableWorkbook workbook, int sheetIndex, int columnCount, String name)
			throws WriteException {
		workbook.createSheet(name, sheetIndex);
		WritableSheet excelSheet = workbook.getSheet(sheetIndex);
		for (int i = 0; i < columnCount; i++) {
			excelSheet.setColumnView(i, 25);
		}
		return excelSheet;
	}

	private void writeLine(WritableSheet sheet, List<Object> currentLine, int lineNumber, WritableCellFormat font)
			throws WriteException {
		writeLine(sheet, currentLine, lineNumber, font, font);
	}

	private void writeLine(WritableSheet sheet, List<Object> currentLine, int lineNumber, WritableCellFormat font,
						   WritableCellFormat titleFont) throws WriteException {
		for (int columnNumber = 0; columnNumber < currentLine.size(); columnNumber++) {
			WritableCellFormat format = columnNumber == 0 ? titleFont : font;
			Object cellObject = currentLine.get(columnNumber);
			if (cellObject == null) {
				continue;
			}
			if (cellObject instanceof Float ||
					cellObject instanceof Integer ||
					cellObject instanceof Double) {
				addNumber(sheet, format, columnNumber, lineNumber, new Double(cellObject.toString()));
			} else {
				addString(sheet, format, columnNumber, lineNumber, cellObject.toString());
			}
		}
	}

	private void addString(WritableSheet sheet, WritableCellFormat font, int column, int row, String s)
			throws WriteException {
		Label label = new Label(column, row, s, font);
		sheet.addCell(label);
	}

	private void addNumber(WritableSheet sheet, WritableCellFormat font, int column, int row, double d)
			throws WriteException {
		Number number = new Number(column, row, d, font);
		sheet.addCell(number);
	}
}