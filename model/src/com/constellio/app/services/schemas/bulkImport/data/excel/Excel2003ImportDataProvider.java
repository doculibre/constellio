package com.constellio.app.services.schemas.bulkImport.data.excel;

import com.constellio.app.services.schemas.bulkImport.data.ImportDataIterator;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataIteratorRuntimeException.ImportDataIteratorRuntimeException_InvalidDate;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProviderRuntimeException;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProviderRuntimeException.ImportDataProviderRuntimeException_InvalidDate;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;

public class Excel2003ImportDataProvider implements ImportDataProvider {

	private File excelFile;

	private Workbook workbook;

	@Override
	public List<File> getImportedContents() {
		return null;
	}

	public Excel2003ImportDataProvider(File excelFile) {
		this.excelFile = excelFile;
	}

	@Override
	public int size(String schemaType)
			throws ImportDataProviderRuntimeException {
		int count = 0;
		ImportDataIterator iterator = newDataIterator(schemaType);
		try {
			while (iterator.hasNext()) {
				iterator.next();
				count++;
			}
		} catch (RuntimeException e) {
			if (e instanceof ImportDataIteratorRuntimeException_InvalidDate) {
				ImportDataIteratorRuntimeException_InvalidDate exception = (ImportDataIteratorRuntimeException_InvalidDate) e;
				throw new ImportDataProviderRuntimeException_InvalidDate(exception.getDateFormat(), exception.getInvalidValue());
			}
			throw new ImportDataProviderRuntimeException(e);
		}
		return count;
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
		if (getAvailableSchemaTypes().contains(schemaType)) {
			return new ExcelImportDataIterator(getExcelSheet(schemaType));
		}
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

	public static Excel2003ImportDataProvider fromFile(File excelFile) {
		Excel2003ImportDataProvider instance = new Excel2003ImportDataProvider(excelFile);
		instance.initialize();
		return instance;
	}

	public ExcelSheet getExcelSheet(String schemaType) {
		return new Excel2003Sheet(workbook.getSheet(schemaType));
	}

}
