package com.constellio.app.services.schemas.bulkImport.data.excel;

import java.util.Date;

import jxl.Cell;
import jxl.CellType;
import jxl.DateCell;

public class Excel2003Cell implements ExcelCell {

	private Cell cell;

	public Excel2003Cell(Cell cell) {
		this.cell = cell;
	}

	@Override
	public int getColumn() {
		return cell.getColumn();
	}

	@Override
	public String getContents() {
		return cell.getContents();
	}

	@Override
	public boolean isNotEmpty() {
		return cell.getType() != CellType.EMPTY;
	}

	@Override
	public boolean isDate() {
		return cell.getType().equals(CellType.DATE);
	}

	@Override
	public Date getDate() {
		return ((DateCell) cell).getDate();
	}
}
