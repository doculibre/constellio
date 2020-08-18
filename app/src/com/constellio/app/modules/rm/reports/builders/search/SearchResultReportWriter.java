package com.constellio.app.modules.rm.reports.builders.search;

import com.constellio.app.modules.rm.reports.builders.excel.BaseExcelReportWriter;
import com.constellio.app.modules.rm.reports.model.search.SearchResultReportModel;
import com.constellio.app.ui.framework.reports.ReportWriter;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.data.conf.FoldersLocator;
import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

public class SearchResultReportWriter extends BaseExcelReportWriter implements ReportWriter {
	private static final WritableFont.FontName FONT = WritableFont.TIMES;
	private static final int FONT_SIZE = 10;

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

	protected void addHeader(WritableSheet sheet, List<String> columnsTitles)
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

	protected void createContent(WritableSheet sheet, List<List<Object>> lines)
			throws WriteException {
		WritableCellFormat font = new WritableCellFormat(new WritableFont(FONT, FONT_SIZE));

		for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
			List<Object> currentLine = lines.get(lineNumber);
			writeLine(sheet, currentLine, lineNumber + 1, font);
		}
	}

	protected void writeLine(WritableSheet sheet, List<Object> currentLine, int lineNumber, WritableCellFormat font)
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

	protected void addString(WritableSheet sheet, WritableCellFormat font, int column, int row, String rawText)
			throws WriteException {
		String htmlStripped = "";
		if (rawText != null) {
			StringBuilder sb = new StringBuilder();
			final Document.OutputSettings outputSettings = new Document.OutputSettings().prettyPrint(false);
			String textWithFixedAccents = Jsoup.clean(rawText, "", Whitelist.none(), outputSettings);
			htmlStripped = Jsoup.clean(textWithFixedAccents, "", Whitelist.none(), outputSettings);
		}
		Label label = new Label(column, row, htmlStripped, font);
		sheet.addCell(label);
	}

	protected void addNumber(WritableSheet sheet, WritableCellFormat font, int column, int row,
							 double d)
			throws WriteException {
		jxl.write.Number number = new jxl.write.Number(column, row, d, font);
		sheet.addCell(number);
	}

}
