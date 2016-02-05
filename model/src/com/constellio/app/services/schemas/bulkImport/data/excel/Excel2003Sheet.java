package com.constellio.app.services.schemas.bulkImport.data.excel;

import java.util.ArrayList;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;

public class Excel2003Sheet implements ExcelSheet {

	private Sheet sheet;

	public Excel2003Sheet(Sheet sheet) {
		this.sheet = sheet;
	}

	public int getRows() {
		return sheet.getRows();
	}

	public List<ExcelCell> getRow(int lineToParse) {
		List<ExcelCell> cells = new ArrayList<>();
		for (Cell cell : sheet.getRow(lineToParse)) {
			cells.add(new Excel2003Cell(cell));
		}
		return cells;
	}
}
