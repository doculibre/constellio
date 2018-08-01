package com.constellio.app.services.schemas.bulkImport.data.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;

import java.util.Date;

public class Excel2007Cell implements ExcelCell {

	private Cell cell;
	private DataFormatter formatter = new DataFormatter();

	public Excel2007Cell(Cell cell) {
		this.cell = cell;
	}

	@Override
	public int getColumn() {
		return cell.getColumnIndex();
	}

	@Override
	public String getContents() {
		CellType cellType = cell.getCellTypeEnum();
		if (cellType == CellType.BOOLEAN) {
			return cell.getBooleanCellValue() ? "true" : "false";
		} else {
			return formatter.formatCellValue(cell);
		}
	}

	@Override
	public boolean isNotEmpty() {
		return cell.getCellType() != Cell.CELL_TYPE_BLANK;
	}

	@Override
	public boolean isDate() {
		return cell.getCellType() == Cell.CELL_TYPE_NUMERIC && DateUtil.isCellDateFormatted(cell);
	}

	@Override
	public Date getDate() {
		return cell.getDateCellValue();
	}
}
