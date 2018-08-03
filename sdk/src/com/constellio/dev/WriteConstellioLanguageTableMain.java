package com.constellio.dev;

import com.constellio.app.ui.framework.reports.ReportWriter;
import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.*;
import jxl.write.Number;
import org.apache.commons.collections.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WriteConstellioLanguageTableMain extends ConstellioLanguageTableIO implements ReportWriter {

	private ReportModelImpl model;
	private WritableWorkbook workbook;

	public WriteConstellioLanguageTableMain(String minVersion) throws IOException {
		super(minVersion, null, true);
	}

	public static void main(String[] args) throws IOException {
		writeLanguageFile();
	}

	private static void writeLanguageFile() throws IOException {
		WriteConstellioLanguageTableMain convertConstellioLanguageTable = new WriteConstellioLanguageTableMain(VERSION_NUMBER_SEPARATOR);

		convertConstellioLanguageTable.write(convertConstellioLanguageTable.getFileOutputStream());
		convertConstellioLanguageTable.convert();
		convertConstellioLanguageTable.endWrite();
	}

	private void convert() {

		Map<String, String> arabicInfos = getFileInfos(getArabicFile().getParentFile(), getArabicFile().getName());

		for (File file : getFilesInPath()) {

			String fileName = file.getName();

			if (isBasePropertyFile(fileName) || isComboPropertyFile(fileName) || isRootPropertyFile(fileName)) {
				initExcelModel();

				Map<String, String> frenchInfos = getFileInfos(file.getParentFile(), fileName);

				Map<String, String> englishInfos = getFileInfos(file.getParentFile(), fileName.replace(PROPERTIES_FILE_EXTENSION, "_en.properties"));

				for (Map.Entry<String, String> entry : frenchInfos.entrySet()) {

					String property = entry.getKey();
					String frenchTerm = removeSpecialChars(entry.getValue());

					List<Object> line = new ArrayList<>();
					line.add(property);
					line.add(frenchTerm);

					// languages other than french can have missing properties
					if (englishInfos.containsKey(property)) {
						line.add(removeSpecialChars(englishInfos.get(property)));
					}
					if (arabicInfos.containsKey(property)) {
						line.add(removeSpecialChars(arabicInfos.get(property)));
					}

					model.addLine(line);
				}

				writeSheet(file.getName().replace(PROPERTIES_FILE_EXTENSION, ""));
			}
		}
	}

	private String removeSpecialChars(String value) {

		char[] charArray = value.toCharArray();

		for (int i = 0; i < charArray.length; i++) {

			char currentChar = charArray[i];

			if ((int) currentChar > ARABIC_CHARACTER_ASSIGNATION_LIMIT) {
				value = value.replace(currentChar, ' ');
			}
		}

		return value.trim();
	}

	// EXCEL SETUP

	/**
	 * Initialize Excel model (headers).
	 */
	private void initExcelModel() {
		model = new ReportModelImpl();
		model.addTitle("Property");
		model.addTitle("French");
		model.addTitle("English");
		model.addTitle("Arabic");
	}

	// EXCEL WRITER

	@Override
	public String getFileExtension() {
		return EXCEL_OUTPUT_FILE_EXTENSION;
	}

	public void write(OutputStream output)
			throws IOException {
		WorkbookSettings wbSettings = new WorkbookSettings();
		workbook = Workbook.createWorkbook(output, wbSettings);
	}

	private void writeSheet(String sheetName) {
		workbook.createSheet(sheetName, 0);
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
	}

	private void endWrite() throws IOException {
		workbook.write();
		try {
			workbook.close();
		} catch (WriteException e) {
			throw new RuntimeException(e);
		}
	}

	// EXCEL TOOLS

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

	// DATA HOLDERS

	public class ReportModelImpl {
		private final List<List<Object>> results = new ArrayList<>();
		private final List<String> columnsTitles = new ArrayList<>();

		public List<List<Object>> getResults() {
			return new ArrayList<>(CollectionUtils.unmodifiableCollection(results));
		}

		public List<String> getColumnsTitles() {
			return new ArrayList<>(CollectionUtils.unmodifiableCollection(columnsTitles));
		}

		public void addTitle(String title) {
			columnsTitles.add(title);
		}

		public void addLine(List<Object> recordLine) {
			results.add(recordLine);
		}
	}
}