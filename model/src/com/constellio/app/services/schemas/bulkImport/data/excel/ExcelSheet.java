package com.constellio.app.services.schemas.bulkImport.data.excel;

import java.util.List;

public interface ExcelSheet {

	int getRows();
	List<ExcelCell> getRow(int lineToParse);
}
