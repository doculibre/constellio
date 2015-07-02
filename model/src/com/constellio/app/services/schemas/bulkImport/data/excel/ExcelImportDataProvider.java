/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.services.schemas.bulkImport.data.excel;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.constellio.app.services.schemas.bulkImport.data.ImportDataIterator;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

public class ExcelImportDataProvider implements ImportDataProvider {

	private File excelFile;

	private Workbook workbook;

	public ExcelImportDataProvider(File excelFile) {
		this.excelFile = excelFile;
	}

	@Override
	public void initialize() {
		this.workbook = loadWorkbook(excelFile);
	}

	@Override
	public void close() {
		workbook.close();
	}

	@Override
	public List<String> getAvailableSchemaTypes() {
		return asList(workbook.getSheetNames());
	}

	@Override
	public ImportDataIterator newDataIterator(String schemaType) {
		if(getAvailableSchemaTypes().contains(schemaType))
			return new ExcelImportDataIterator(workbook.getSheet(schemaType));
		throw new RuntimeException("There are no sheet with this schema type");
	}

	public Workbook loadWorkbook(File workbookFile) {
		WorkbookSettings settings = new WorkbookSettings();
		settings.setEncoding("LATIN1");
		try {
			return Workbook.getWorkbook(workbookFile, settings);
		} catch (BiffException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static ExcelImportDataProvider fromFile(File excelFile) {
		ExcelImportDataProvider instance = new ExcelImportDataProvider(excelFile);
		instance.initialize();
		return instance;
	}

}
