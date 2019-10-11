package com.constellio.app.modules.rm.reports.builders.decommissioning;

import com.constellio.app.modules.rm.reports.model.decommissioning.DecommissioningListXLSDetailedReportModel;
import com.constellio.app.ui.framework.reports.ReportWriter;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.joda.time.LocalDateTime;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;

public class DecommissioningListXLSDetailedReportWriter implements ReportWriter {
	private static final WritableFont.FontName FONT = WritableFont.TIMES;
	private static final int FONT_SIZE = 10;
	private WritableCellFormat font;
	private WritableCellFormat boldFont;

	private Locale locale;
	private DecommissioningListXLSDetailedReportModel model;
	private int currentSheet;

	public DecommissioningListXLSDetailedReportWriter(DecommissioningListXLSDetailedReportModel model, Locale locale) {
		this.model = model;
		this.locale = locale;
		currentSheet = -1;
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
			createHeader(workbook);
			createComment(workbook);
			createValidation(workbook);
			createIncludedFolder(workbook);
			createExcludedFolder(workbook);
			createUndefinedFolder(workbook);
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

	private void createHeader(WritableWorkbook workbook) throws WriteException {
		WritableSheet sheet = createSheet(workbook, 2, model.getHeaderSheetName());

		for (int i = 0; i < model.getHeaderTitles().size(); i++) {
			writeLine(sheet, Arrays.asList(model.getHeaderTitles().get(i), model.getHeaderInfos().get(i)), i, font,
					boldFont);
		}

		int generationLineNumber = model.getHeaderTitles().size() + 1;
		String title = $("DecommissioningListDetailedReport.generationDate");
		writeLine(sheet, Arrays.asList(title, LocalDateTime.now()), generationLineNumber, font, boldFont);
	}

	private void createComment(WritableWorkbook workbook) throws WriteException {
		WritableSheet sheet = createSheet(workbook, model.getCommentTitles().size(), model.getCommentSheetName());

		writeLine(sheet, model.getCommentTitles(), 0, boldFont);
		for (int i = 0; i < model.getComments().size(); i++) {
			writeLine(sheet, model.getComments().get(i), 1 + i, font);
		}
	}

	private void createValidation(WritableWorkbook workbook) throws WriteException {
		WritableSheet sheet = createSheet(workbook, model.getValidationTitles().size(), model.getValidationSheetName());

		writeLine(sheet, model.getValidationTitles(), 0, boldFont);
		for (int i = 0; i < model.getValidations().size(); i++) {
			writeLine(sheet, model.getValidations().get(i), 1 + i, font);
		}
	}

	private void createIncludedFolder(WritableWorkbook workbook) throws WriteException {
		WritableSheet sheet = createSheet(workbook, model.getIncludedFolderTitles().size(),
				model.getIncludedFolderSheetName());

		writeLine(sheet, model.getIncludedFolderTitles(), 0, boldFont);
		for (int i = 0; i < model.getIncludedFolders().size(); i++) {
			writeLine(sheet, model.getIncludedFolders().get(i), 1 + i, font);
		}
	}

	private void createExcludedFolder(WritableWorkbook workbook) throws WriteException {
		WritableSheet sheet = createSheet(workbook, model.getExcludedFolderTitles().size(),
				model.getExcludedFolderSheetName());

		writeLine(sheet, model.getExcludedFolderTitles(), 0, boldFont);
		for (int i = 0; i < model.getExcludedFolders().size(); i++) {
			writeLine(sheet, model.getExcludedFolders().get(i), 1 + i, font);
		}
	}

	private void createUndefinedFolder(WritableWorkbook workbook) throws WriteException {
		if (!model.getUseDecommissionningListWithSelectedFolders()) {
			return;
		}

		WritableSheet sheet = createSheet(workbook, model.getUndefinedFolderTitles().size(),
				model.getUndefinedFolderSheetName());

		writeLine(sheet, model.getUndefinedFolderTitles(), 0, boldFont);
		for (int i = 0; i < model.getUndefinedFolders().size(); i++) {
			writeLine(sheet, model.getUndefinedFolders().get(i), 1 + i, font);
		}
	}

	private WritableSheet createSheet(WritableWorkbook workbook, int columnCount, String name)
			throws WriteException {
		currentSheet++;
		workbook.createSheet(name, currentSheet);
		WritableSheet excelSheet = workbook.getSheet(currentSheet);
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