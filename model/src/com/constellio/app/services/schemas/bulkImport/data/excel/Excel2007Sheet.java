package com.constellio.app.services.schemas.bulkImport.data.excel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class Excel2007Sheet implements ExcelSheet {

	private XSSFSheet sheet;

	public Excel2007Sheet(XSSFSheet sheet) {
		this.sheet = sheet;
	}

	@Override
	public int getRows() {
		return sheet.getLastRowNum() + 1;
	}

	@Override
	public List<ExcelCell> getRow(int lineToParse) {
		List<ExcelCell> cells = new ArrayList<>();
		XSSFRow row = sheet.getRow(lineToParse);
		if (row != null) {
			Iterator<Cell> iterator = row.cellIterator();
			while (iterator.hasNext()) {
				cells.add(new Excel2007Cell(iterator.next()));
			}
		}
		return cells;
	}
}
