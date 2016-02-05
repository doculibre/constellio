package com.constellio.app.services.schemas.bulkImport.data.excel;

import java.util.Date;

public interface ExcelCell {

	int getColumn();
	String getContents();
	boolean isNotEmpty();
	boolean isDate();
	Date getDate();
}
