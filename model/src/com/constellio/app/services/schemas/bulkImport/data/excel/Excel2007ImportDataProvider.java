package com.constellio.app.services.schemas.bulkImport.data.excel;

import com.constellio.app.services.schemas.bulkImport.data.ImportDataIterator;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Excel2007ImportDataProvider implements ImportDataProvider {

	private File excelFile;

	private OPCPackage opcPackage;
	private XSSFWorkbook workbook;
	private Map<String, Integer> schemaSizeMap;
	private static final int EXCEL_HEADER_SIZE = 2;

	public Excel2007ImportDataProvider(File excelFile) {
		this.excelFile = excelFile;
		this.schemaSizeMap = new HashMap<>();
	}

	@Override
	public int size(String schemaType) {
		if (schemaSizeMap.get(schemaType) != null) {
			return schemaSizeMap.get(schemaType);
		}
		int count = workbook.getSheet(schemaType).getPhysicalNumberOfRows() - EXCEL_HEADER_SIZE;
		schemaSizeMap.put(schemaType, count);
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
