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

import static jxl.CellType.EMPTY;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.services.schemas.bulkImport.data.ImportData;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataIterator;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataIteratorRuntimeException;
import jxl.Cell;
import jxl.CellType;
import jxl.DateCell;
import jxl.Sheet;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.constellio.data.utils.LazyIterator;
import com.drew.metadata.MetadataException;

public class ExcelImportDataIterator extends LazyIterator<ImportData> implements ImportDataIterator {

	public static final String ID_ATTR = "id";
	public static final String SCHEMA_ATTR = "schema";

	public static final String PATTERN = "pattern";
	public static final String SEPARATOR = "separator";
	public static final String DATE = "date";
	public static final String DEFAULT_SCHEMA = "default";
	private static final String DATETIME_VALUE = "datetime";
	private static final String DATE_VALUE = "date";

	private Sheet sheet;
	private int lineToParse = 0;
	private List<ExcelDataType> types;

	public ExcelImportDataIterator(Sheet sheet) {
		this.sheet = sheet;
		this.types = new ArrayList<>();
		initialize();
	}

	private void initialize() {
		for (Cell cell : sheet.getRow(0)) {
			if (!nullOrInvalidData(cell.getContents())) {
				try {
					types.add(parseCellTypeLine(cell));
				} catch (MetadataException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void close() {
	}

	@Override
	protected ImportData getNextOrNull() {
		lineToParse++;

		if (lineToParse == sheet.getRows()) {
			return null;
		}

		while (lineIsEmpty()) {
			lineToParse++;
		}

		return parseRecord();
	}

	public ExcelDataType parseCellTypeLine(Cell cell)
			throws MetadataException {
		String cellContent = cell.getContents();
		ExcelDataType dataType = new ExcelDataType();

		if (!cellContent.contains("\n")) {
			dataType.setTypeName(cellContent);
		} else {
			String[] metadatas = cellContent.split("\\n");
			dataType.setTypeName(metadatas[0]);
			for (int index = 1; index < metadatas.length; index++) {
				String line = metadatas[index];
				if (line.contains(DATE) || line.contains(PATTERN) || line.contains(SEPARATOR)) {
					if (line.contains(DATE)) {
						String[] splitDatas = line.split("=", 2);
						dataType.setDateType(splitDatas[splitDatas.length - 1]);
						if (metadatas[++index].contains(PATTERN)) {
							dataType.setDatePattern(splitDateFormat(metadatas[index]));
						}
					} else if (line.contains(PATTERN)) {
						dataType.setDataPattern(line.replace(PATTERN + "=", ""));
					}
					if (line.contains(SEPARATOR)) {
						String[] splitDatas = line.split("=", 2);
						dataType.setSeparator(splitDatas[splitDatas.length - 1]);
					}
				} else {
					throw new MetadataException("The file contains invalid metadatas on column type");
				}
			}

		}
		return dataType;
	}

	public ImportData parseRecord() {
		String schema = DEFAULT_SCHEMA;
		int column = 0;
		Map<String, Object> fields = new HashMap<>();
		String legacy = null;

		for (Cell cell : sheet.getRow(lineToParse)) {
			ExcelDataType currentType = types.get(column);

			if (currentType != null) {
				switch (currentType.getTypeName()) {
				case ID_ATTR:
					legacy = cell.getContents();
					break;
				case SCHEMA_ATTR:
					if (cell.getType() != CellType.EMPTY) {
						schema = cell.getContents();
					}
					break;
				default:
					fields.put(types.get(column).getTypeName(), parseCell(cell, types.get(column)));
					break;
				}
			} else {
				return null;
			}
			column++;

			if (column == types.size()) {
				break;
			}
		}

		for (ExcelDataType type : types) {
			if (!fields.containsKey(type.getTypeName())) {
				fields.put(type.getTypeName(), null);
			}
		}

		if (fields.containsKey(ID_ATTR)) {
			fields.remove(ID_ATTR);
		}
		if (fields.containsKey(SCHEMA_ATTR)) {
			fields.remove(SCHEMA_ATTR);
		}

		return new ImportData(lineToParse, schema, legacy, fields);
	}

	private boolean lineIsEmpty() {
		for (Cell cell : sheet.getRow(lineToParse)) {
			if (cell.getType() != EMPTY && !nullOrInvalidData(cell.getContents())) {
				return false;
			}
		}
		return true;
	}

	private boolean nullOrInvalidData(String content) {
		return content == null || content.equals("") || content.equals(" ") || content.equals("\n") || content.equals("null");
	}

	public Object parseCell(Cell cell, ExcelDataType type) {

		if (type.getSeparator() != null) {
			return parseCellWithMultiValue(cell, type);
		} else {
			return parseCellWithSimpleValue(cell, type);
		}
	}

	private Object parseCellWithSimpleValue(Cell cell, ExcelDataType type) {
		String cellContent = cell.getContents();

		if (cell.getType() == CellType.DATE) {
			Date date = ((DateCell) cell).getDate();
			DateTime dateTime = new DateTime(date).withZone(DateTimeZone.UTC);
			return dateTime.toLocalDate();
		} else {
			return readValue(cellContent, type);
		}
	}

	public Object parseCellWithMultiValue(Cell cell, ExcelDataType cellType) {
		String cellContent = cell.getContents();
		String[] multivalueContent;
		List<Object> datas = new ArrayList<>();

		multivalueContent = cellContent.split(cellType.getSeparator());
		for (String value : multivalueContent) {
			datas.add(readValue(value, cellType));
		}

		return validatedList(datas);
	}

	private Object validatedList(List<Object> datas) {
		List<Object> garbage = new ArrayList<>();

		for (Object content : datas) {
			if ((content instanceof String && nullOrInvalidData((String) content)) || content == null) {
				garbage.add(content);
			}
		}

		datas.removeAll(garbage);

		return datas;
	}

	public Object readValue(String value, ExcelDataType type) {
		if (nullOrInvalidData(value))
			return null;

		if (type.getDatePattern() != null) {
			return formatDateString(value, type);
		} else if (type.getDataPattern() != null) {
			return type.getDataPattern() + ":" + value;
		} else {
			return value;
		}
	}

	public Object formatDateString(String value, ExcelDataType type) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(type.getDatePattern());

		switch (type.getDateType()) {
		case DATE_VALUE:
			try {
				return dateTimeFormatter.parseLocalDate(value);
			} catch (IllegalArgumentException e) {
				throw new ImportDataIteratorRuntimeException.ImportDataIteratorRuntimeException_InvalidDate(type.getDatePattern(), value);
			}
		case DATETIME_VALUE:
			try {
				return dateTimeFormatter.parseLocalDateTime(value);
			} catch (IllegalArgumentException e) {
				throw new ImportDataIteratorRuntimeException.ImportDataIteratorRuntimeException_InvalidDate(type.getDatePattern(), value);
			}
		default:
			throw new ImportDataIteratorRuntimeException.ImportDataIteratorRuntimeException_InvalidDate(type.getDatePattern(), value);
		}
	}

	public String splitDateFormat(String line) {
		String[] splitLine = line.split("\\n");
		String dateFormat = null;

		for (String s : splitLine) {
			if (s.contains(PATTERN)) {
				String[] splitDatePatternLine = s.split("\\=");
				dateFormat = splitDatePatternLine[splitDatePatternLine.length - 1];
			}
		}
		return dateFormat;
	}
}