package com.constellio.app.modules.rm.reports.builders.excel;

import jxl.CellView;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WriteException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

import java.util.List;

public class BaseExcelReportWriter {
	private static final WritableFont.FontName FONT = WritableFont.TIMES;
	private static final int FONT_SIZE = 10;

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
		if(rawText != null) {
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
		Number number = new Number(column, row, d, font);
		sheet.addCell(number);
	}
}
