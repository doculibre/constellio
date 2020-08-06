package com.constellio.app.services.schemas.bulkImport.data.excel;

import com.constellio.app.services.schemas.bulkImport.data.ImportDataIterator;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataIteratorRuntimeException.ImportDataIteratorRuntimeException_InvalidDate;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProviderRuntimeException;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProviderRuntimeException.ImportDataProviderRuntimeException_InvalidDate;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Excel2007ImportDataProvider implements ImportDataProvider {

	private File excelFile;

	private OPCPackage opcPackage;
	private XSSFWorkbook workbook;

	public Excel2007ImportDataProvider(File excelFile) {
		this.excelFile = excelFile;
	}

	@Override
	public int size(String schemaType) {
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
		try {
			opcPackage.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<String> getAvailableSchemaTypes() {
		List<String> sheetNames = new ArrayList<>();
		Iterator<Sheet> iterator = workbook.iterator();
		while (iterator.hasNext()) {
			sheetNames.add(iterator.next().getSheetName());
		}
		return sheetNames;
	}

	@Override
	public ImportDataIterator newDataIterator(String schemaType) {
		if (getAvailableSchemaTypes().contains(schemaType)) {
			return new ExcelImportDataIterator(getExcelSheet(schemaType));
		}
		throw new RuntimeException("There are no sheet with this schema type");
	}

	@Override
	public List<File> getImportedContents() {
		return null;
	}


	public XSSFWorkbook loadWorkbook(File workbookFile) {
		try {






			opcPackage = OPCPackage.open(new FileInputStream(workbookFile));
			return new XSSFWorkbook(opcPackage);
		} catch (InvalidFormatException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Excel2007ImportDataProvider fromFile(File excelFile) {
		Excel2007ImportDataProvider instance = new Excel2007ImportDataProvider(excelFile);
		instance.initialize();
		return instance;
	}

	public ExcelSheet getExcelSheet(String schemaType) {
		return new Excel2007Sheet(workbook.getSheet(schemaType));
	}

}
