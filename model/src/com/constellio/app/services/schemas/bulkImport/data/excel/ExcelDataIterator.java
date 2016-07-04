package com.constellio.app.services.schemas.bulkImport.data.excel;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

import org.joda.time.LocalDate;

import com.constellio.data.utils.LazyIterator;

public class ExcelDataIterator extends LazyIterator<Map<String, Object>> implements Closeable {

	private ExcelSheet sheet;
	private int lineToParse = 0;
	private List<String> columns;

	public ExcelDataIterator(ExcelSheet sheet) {
		this.sheet = sheet;
		this.columns = new ArrayList<>();
		initialize();
	}

	private void initialize() {
		for (ExcelCell cell : sheet.getRow(0)) {
			if (!nullOrInvalidData(cell.getContents())) {
				columns.add(cell.getContents());
			} else {
				columns.add(null);
			}
		}
	}

	@Override
	public void close() {
	}

	@Override
	protected Map<String, Object> getNextOrNull() {
		lineToParse++;

		if (lineToParse == sheet.getRows()) {
			return null;
		}

		try {
			while (lineIsEmpty()) {
				lineToParse++;
			}

			return parseLine();
		} catch (ArrayIndexOutOfBoundsException e) {
			//OK
			return null;
		}
	}

	private Map<String, Object> parseLine() {
		Map<String, Object> line = new HashMap<>();

		List<ExcelCell> cells = sheet.getRow(lineToParse);
		for (int i = 0; i < columns.size(); i++) {
			String column = columns.get(i);
			if (column != null) {
				ExcelCell cell = cells.get(i);

				if (!cellIsEmpty(cell)) {
					if (cell.isDate()) {
						line.put(column, new LocalDate(cell.getDate()));
					} else {
						line.put(column, cell.getContents());
					}
				}
			}
		}

		return line;
	}

	private boolean lineIsEmpty() {
		for (ExcelCell cell : sheet.getRow(lineToParse)) {
			if (cell.isNotEmpty() && !nullOrInvalidData(cell.getContents())) {
				return false;
			}
		}

		return true;
	}

	private boolean cellIsEmpty(ExcelCell cell) {
		if (cell.isNotEmpty() && !nullOrInvalidData(cell.getContents())) {
			return false;
		}
		return true;
	}

	private boolean nullOrInvalidData(String content) {
		return content == null || content.equals("") || content.equals(" ") || content.equals("\n") || content.equals("null");
	}

	public static ExcelDataIterator overSheet(File file, String sheet) {
		Workbook workbook = loadWorkbook(file);
		ExcelSheet excelSheet = new Excel2003Sheet(workbook.getSheet(sheet));
		return new ExcelDataIterator(excelSheet);
	}

	public static Workbook loadWorkbook(File workbookFile) {
		WorkbookSettings settings = new WorkbookSettings();
		settings.setEncoding("LATIN1");
		try {
			return Workbook.getWorkbook(workbookFile, settings);
		} catch (BiffException | IOException e) {
			throw new RuntimeException(e);
		}
	}

}