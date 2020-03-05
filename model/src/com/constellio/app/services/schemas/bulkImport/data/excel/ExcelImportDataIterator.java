package com.constellio.app.services.schemas.bulkImport.data.excel;

import com.constellio.app.services.schemas.bulkImport.data.ImportData;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataIterator;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataIteratorRuntimeException;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataOptions;
import com.constellio.data.utils.LazyIterator;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.services.records.SimpleImportContent;
import com.drew.metadata.MetadataException;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ExcelImportDataIterator extends LazyIterator<ImportData> implements ImportDataIterator {

	public static final String ID_ATTR = "id";
	public static final String SCHEMA_ATTR = "schema";

	public static final String PATTERN = "pattern";
	public static final String SEPARATOR = "separator";
	public static final String DATE = "date";
	public static final String DEFAULT_SCHEMA = "default";
	private static final String DATETIME_VALUE = "datetime";
	private static final String DATE_VALUE = "date";
	public static final String STRUCTURE = "structure";
	public static final String ITEM = "item";
	public static final String MULTILINE = "multiline";
	public static final String LEGACY_ID = "importAsLegacyId";
	public static final String FILENAME_HASH_CONTENT = "filename:hash";

	private ExcelSheet sheet;
	private int lineToParse = 1;
	private List<ExcelDataType> types;

	private ImportDataOptions options;

	public ExcelImportDataIterator(ExcelSheet sheet) {
		this.sheet = sheet;
		this.types = new ArrayList<>();
		this.options = new ImportDataOptions();
		initialize();
	}

	private void initialize() {
		for (ExcelCell cell : sheet.getRow(1)) {
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
	public ImportDataOptions getOptions() {
		return new ImportDataOptions();
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

		try {
			while (lineIsEmpty()) {
				lineToParse++;
			}

			return parseRecord();
		} catch (ArrayIndexOutOfBoundsException e) {
			//OK
			return null;
		}
	}

	public ExcelDataType parseCellTypeLine(ExcelCell cell)
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
				if (line.contains(DATE) || line.contains(PATTERN) || line.contains(SEPARATOR) || line.contains(STRUCTURE) || line
						.contains(ITEM) || line.contains(MULTILINE) || line.contains(LEGACY_ID) || line.contains(FILENAME_HASH_CONTENT)) {
					if (line.contains(DATE)) {
						String[] splitDatas = line.split("=", 2);
						dataType.setDateType(splitDatas[splitDatas.length - 1]);
						if (metadatas[++index].contains(PATTERN)) {
							dataType.setDatePattern(splitDateFormat(metadatas[index]));
						}
					} else if (line.contains(PATTERN)) {
						dataType.setDataPattern(line.replace(PATTERN + "=", ""));
					} else if (line.contains(STRUCTURE)) {
						dataType.setStructure(line.replace(STRUCTURE + "=", ""));
					} else if (line.contains(ITEM)) {
						dataType.setItem(line.replace(ITEM + "=", ""));
					} else if (line.contains(MULTILINE)) {
						dataType.setMultiline(true);
					} else if (line.contains(FILENAME_HASH_CONTENT)) {
						dataType.setFilenameHashImport(true);
					} else if (line.contains(LEGACY_ID)) {
						boolean isLegacyId = line.split("=", 2)[1].equalsIgnoreCase("true") ? true : false;
						options.setImportAsLegacyId(isLegacyId);
					}

					if (line.contains(SEPARATOR)) {
						String[] splitDatas = line.split("=", 2);
						dataType.setSeparator(splitDatas[splitDatas.length - 1]);
					}
				} else {
					throw new MetadataException("The file contains invalid metadatas on column type: " + StringUtils.defaultIfBlank(line, ""));
				}
			}

		}
		return dataType;
	}

	public ImportData parseRecord() {
		String schema = DEFAULT_SCHEMA;
		Map<String, Object> fields = new HashMap<>();
		String legacy = null;

		for (ExcelCell cell : sheet.getRow(lineToParse)) {
			ExcelDataType currentType;
			try {
				currentType = types.get(cell.getColumn());
			} catch (IndexOutOfBoundsException e) {
				currentType = null;
			}

			if (currentType != null) {
				switch (currentType.getTypeName()) {
					case ID_ATTR:
						legacy = StringUtils.trim(cell.getContents());
						break;
					case SCHEMA_ATTR:
						if (cell.isNotEmpty()) {
							schema = cell.getContents();
						}
						break;
					default:
						if (currentType.getStructure() != null) {
							List<Map<String, String>> structure = (List<Map<String, String>>) fields.get(currentType.getStructure());
							fields.put(currentType.getStructure(), createOrUpdateStructureContent(currentType, structure, cell));
						} else {
							fields.put(types.get(cell.getColumn()).getTypeName(), parseCell(cell, types.get(cell.getColumn())));
							break;
						}
				}
			}

			if (cell.getColumn() == types.size()) {
				break;
			}
		}

		for (ExcelDataType type : types) {
			if (!fields.containsKey(type.getTypeName())) {
				if (type.getStructure() == null) {
					if (type.getSeparator() != null) {
						fields.put(type.getTypeName(), new ArrayList<Object>());
					} else {
						fields.put(type.getTypeName(), null);
					}
				} else {
					List<Map<String, String>> structure = (List<Map<String, String>>) fields.get(type.getStructure());
					if (structure.size() >= type.getItem() && !structure.isEmpty() && type.getItem() > 0) {
						Map<String, String> itemStructure = structure.get(type.getItem() - 1);
						if (!itemStructure.containsKey(type.getTypeName())) {
							itemStructure.put(type.getTypeName(), null);
						}
					}
				}
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

	private List<Map<String, String>> createOrUpdateStructureContent(ExcelDataType currentType,
																	 List<Map<String, String>> structure,
																	 ExcelCell cell) {
		structure = structure != null ? structure : new ArrayList<Map<String, String>>();
		if (currentType.isMultiline() && currentType.getSeparator() != null) {
			structure = createMultilineSubStructure(currentType, cell);
		} else {
			Map<String, String> itemStructure = createUpdateItemSubStructure(currentType, cell, structure);
			if (structure.indexOf(itemStructure) != -1) {
				structure.set(currentType.getItem() - 1, itemStructure);
			} else {
				structure.add(itemStructure);
			}
		}
		return structure;
	}

	private List<Map<String, String>> createMultilineSubStructure(ExcelDataType currentType, ExcelCell cell) {
		List<Map<String, String>> structure = new ArrayList<Map<String, String>>();
		String[] metadataNames = currentType.getTypeName().split(currentType.getSeparator());
		String[] lines = cell.getContents().split("\n");
		for (int i = 0; i < lines.length; i++) {
			Map<String, String> subStructure = new HashMap<String, String>();
			if (StringUtils.isNotBlank(lines[i])) {
				String[] values = lines[i].split(currentType.getSeparator());
				for (int j = 0; j < values.length; j++) {
					subStructure.put(metadataNames[j], values[j]);
				}
				structure.add(subStructure);
			}
		}
		return structure;
	}

	private Map<String, String> createUpdateItemSubStructure(ExcelDataType currentType, ExcelCell cell,
															 List<Map<String, String>> structure) {
		Map<String, String> itemStructure;
		if (structure.size() >= currentType.getItem() && !structure.isEmpty()) {
			itemStructure = structure.get(currentType.getItem() - 1);
		} else {
			itemStructure = new HashMap<String, String>();
		}
		itemStructure.put(currentType.getTypeName(), convertToStructure(parseCell(cell, types.get(cell.getColumn()))));
		return itemStructure;
	}

	private String convertToStructure(Object value) {
		if (value instanceof List) {
			StringBuilder builder = new StringBuilder();
			Iterator<Object> iterator = ((List) value).iterator();
			while (iterator.hasNext()) {
				builder.append(String.valueOf(iterator.next()).replace("code:", ""));
				if (iterator.hasNext()) {
					builder.append(",");
				}
			}
			return builder.toString();
		}
		return value != null ? String.valueOf(value) : null;
		//		return String.valueOf(value);
	}

	private boolean lineIsEmpty() {
		for (ExcelCell cell : sheet.getRow(lineToParse)) {
			if (cell.isNotEmpty() && !nullOrInvalidData(cell.getContents())) {
				return false;
			}
		}
		return true;
	}

	private boolean nullOrInvalidData(String content) {
		return content == null || content.equals("") || content.equals(" ") || content.equals("\n") || content.equals("null");
	}

	public Object parseCell(ExcelCell cell, ExcelDataType type) {
		if (type.getSeparator() != null) {
			return parseCellWithMultiValue(cell, type);
		} else {
			return parseCellWithSimpleValue(cell, type);
		}
	}

	private Object parseCellWithSimpleValue(ExcelCell cell, ExcelDataType type) {
		String cellContent = cell.getContents();

		if (cell.isDate()) {
			Date date = cell.getDate();
			DateTime dateTime = new DateTime(date).withZone(DateTimeZone.UTC);
			if ("dateTime".equals(type.getDateType())) {
				return dateTime.toLocalDateTime();
			} else {
				return dateTime.toLocalDate();
			}
		} else {
			return readValue(cellContent, type);
		}
	}

	public Object parseCellWithMultiValue(ExcelCell cell, ExcelDataType cellType) {
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
		if (nullOrInvalidData(value)) {
			return null;
		}

		value = StringUtils.trim(value);
		if (type.getDatePattern() != null) {
			return formatDateString(value, type);
		} else if (type.getDataPattern() != null) {
			return type.getDataPattern() + ":" + value;
		} else if (type.isFilenameHashImport() && StringUtils.isNotBlank(value) && value.contains(":")) {
			String[] parts = value.split(":");
			return new SimpleImportContent("hash:" + parts[1], parts[0], true, TimeProvider.getLocalDateTime());} else {
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
					throw new ImportDataIteratorRuntimeException.ImportDataIteratorRuntimeException_InvalidDate(type.getDatePattern(),
							value);
				}
			case DATETIME_VALUE:
				try {
					return dateTimeFormatter.parseLocalDateTime(value);
				} catch (IllegalArgumentException e) {
					throw new ImportDataIteratorRuntimeException.ImportDataIteratorRuntimeException_InvalidDate(type.getDatePattern(),
							value);
				}
			default:
				throw new ImportDataIteratorRuntimeException.ImportDataIteratorRuntimeException_InvalidDate(type.getDatePattern(),
						value);
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

	//    private SimpleImportContent parseContent()
	//            throws XMLStreamException {
	//        boolean closeContent = false;
	//
	//
	//        String fileName;
	//        String comment;
	//        boolean major;
	//        LocalDateTime dateTime;
	//        int endClose = 0;
	//
	//        List<ContentImportVersion> contentVersions = new ArrayList<>();
	//
	//        DateTimeFormatter datetimePattern;
	//        try {
	//            datetimePattern = DateTimeFormat.forPattern(patterns.get(DATETIME_PATTERN));
	//        } catch (IllegalArgumentException e) {
	//            datetimePattern = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
	//        }
	//        while (xmlReader.hasNext() && !closeContent) {
	//            int event = xmlReader.next();
	//            if (event == XMLStreamConstants.END_ELEMENT) {
	//                if (endClose == 0) {
	//                    closeContent = true;
	//                }
	//                endClose--;
	//            } else if (event == XMLStreamConstants.START_ELEMENT) {
	//                endClose++;
	//                url = xmlReader.getAttributeValue("", URL_ATTR);
	//                fileName = xmlReader.getAttributeValue("", FILENAME_ATTR);
	//                comment = xmlReader.getAttributeValue("", COMMENT_ATTR);
	//                major = Boolean.parseBoolean(xmlReader.getAttributeValue("", MAJOR_ATTR));
	//
	//                try {
	//                    String dateTimeStr = xmlReader.getAttributeValue("", LAST_MODIFICATION_DATETIME);
	//                    dateTime = datetimePattern.parseLocalDateTime(dateTimeStr);
	//                } catch (Exception exception) {
	//                    dateTime = TimeProvider.getLocalDateTime();
	//                }
	//
	//                contentVersions.add(new ContentImportVersion(url, fileName, major, comment, dateTime));
	//            }
	//        }
	//
	//        return new SimpleImportContent(contentVersions);
	//    }
}