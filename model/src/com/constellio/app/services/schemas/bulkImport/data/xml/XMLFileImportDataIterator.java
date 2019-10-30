package com.constellio.app.services.schemas.bulkImport.data.xml;

import com.constellio.app.services.schemas.bulkImport.data.ImportData;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataIterator;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataIteratorRuntimeException;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataOptions;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.LazyIterator;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.services.records.ContentImportVersion;
import com.constellio.model.services.records.SimpleImportContent;
import com.constellio.model.services.records.StructureImportContent;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMLFileImportDataIterator extends LazyIterator<ImportData> implements ImportDataIterator {

	public static final String IMPORT_AS_LEGACY_ID = "importAsLegacyId";
	public static final String MERGE_EXISTING_RECORD_WITH_SAME_UNIQUE_METADATA = "mergeExistingRecordWithSameUniqueMetadata";

	public static final String RECORDS_TAG = "records";
	public static final String RECORD_TAG = "record";
	public static final String USER_CREDENTIAL_TAG = "userCredential";
	public static final String USERS_CREDENTIAL_TAG = "userCredentials";
	public static final String ITEM_TAG = "item";

	public static final String TYPE_ATTR = "type";
	public static final String MULTIVALUE_ATTR = "multivalue";
	public static final String ID_ATTR = "id";
	public static final String SCHEMA_ATTR = "schema";
	public static final String URL_ATTR = "url";
	public static final String STRUCTURE_VALUE_ATTR = "structureValue";
	public static final String FILENAME_ATTR = "filename";
	public static final String COMMENT_ATTR = "comment";
	public static final String MAJOR_ATTR = "major";
	public static final String LAST_MODIFICATION_DATETIME = "lastModificationDateTime";

	public static final String STRING_VALUE = "string";
	public static final String CONTENT_VALUE = "content";
	public static final String STRUCTURE_CONTENT_VALUE = "structureContent";
	public static final String DATE_VALUE = "date";
	public static final String DATETIME_VALUE = "datetime";
	public static final String STRUCTURE_VALUE = "structure";

	public static final String DATE_PATTERN = "datePattern";
	public static final String DATETIME_PATTERN = "datetimePattern";

	public static final String USERNAME = "username";

	String previousSystemId;

	private Reader reader;
	private String schema = null;
	private Map<String, Object> fields = null;
	private Map<String, String> patterns = null;
	private int index = 0;
	protected XMLStreamReader xmlReader;

	private IOServices ioServices;

	private ImportDataOptions options = new ImportDataOptions();

	public XMLFileImportDataIterator(Reader reader, IOServices ioServices) {
		this.reader = reader;
		this.xmlReader = newXMLStreamReader(reader);
		this.ioServices = ioServices;
	}

	@Override
	protected ImportData getNextOrNull() {
		try {
			ImportData importData = parseRecord();
			if (importData == null) {
				closeQuietly();
			}
			return importData;
		} catch (XMLStreamException e) {
			closeQuietly();
			throw new RuntimeException(e);
		}
	}

	private void closeQuietly() {
		try {
			xmlReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		ioServices.closeQuietly(reader);
	}

	protected ImportData parseRecord()
			throws XMLStreamException {
		String type;
		Object value;

		while (xmlReader.hasNext()) {
			int event = xmlReader.next();
			if (event == XMLStreamConstants.START_ELEMENT) {
				String localName = xmlReader.getLocalName();

				switch (localName) {
					case RECORDS_TAG:
						patterns = new HashMap<>();
						patterns.put(DATETIME_PATTERN, xmlReader.getAttributeValue("", DATETIME_PATTERN));
						patterns.put(DATE_PATTERN, xmlReader.getAttributeValue("", DATE_PATTERN));

						options.setImportAsLegacyId(!"false".equals(xmlReader.getAttributeValue("", IMPORT_AS_LEGACY_ID)));
						options.setMergeExistingRecordWithSameUniqueMetadata(
								"true".equals(xmlReader.getAttributeValue("", MERGE_EXISTING_RECORD_WITH_SAME_UNIQUE_METADATA)));

						break;
					case RECORD_TAG:
						schema = xmlReader.getAttributeValue("", SCHEMA_ATTR);
						if (schema == null) {
							schema = "default";
						}
						previousSystemId = xmlReader.getAttributeValue("", ID_ATTR);
						if (previousSystemId == null) {
							return null;
						}
						fields = new HashMap<>();
						break;

					case CONTENT_VALUE:
						fields.put("content", parseContent());
						break;

					case STRUCTURE_CONTENT_VALUE:
						String structureValue = xmlReader.getAttributeValue("", STRUCTURE_VALUE);
						fields.put(xmlReader.getAttributeValue("", "key"), new StructureImportContent(structureValue));
						break;

					default:
						if (localName.equals(elementTag())) {
							fields = new HashMap<>();
							previousSystemId = getElementId(xmlReader);
							if (StringUtils.isBlank(previousSystemId)) {
								throw new InvalidIdRuntimeException(previousSystemId);
							}
							initElementFields(previousSystemId, fields);
						} else if (localName.equals(mainElementTag())) {
							patterns = new HashMap<>();
							initPatterns(xmlReader, patterns);
						} else {
							type = getType();
							value = isMultivalue() ? parseMultivalue(xmlReader.getLocalName(), type) : parseScalar(type);

							if (value != "" && !value.equals("null")) {
								fields.put(xmlReader.getLocalName(), value);
							}
						}

						break;
				}
			} else if (event == XMLStreamConstants.END_ELEMENT && (xmlReader.getLocalName().equals(RECORD_TAG)
																   || xmlReader.getLocalName().equals(elementTag()))) {
				++index;
				return new ImportData(index, schema, previousSystemId, fields);
			}
		}
		return null;
	}

	protected void initPatterns(XMLStreamReader xmlReader, Map<String, String> patterns) {
	}

	protected void initElementFields(String previousSystemId, Map<String, Object> fields) {
		fields.put(USERNAME, previousSystemId);
	}

	protected String getElementId(XMLStreamReader xmlReader) {
		return xmlReader.getAttributeValue("", USERNAME);
	}

	protected String mainElementTag() {
		return USERS_CREDENTIAL_TAG;
	}

	protected String elementTag() {
		return USER_CREDENTIAL_TAG;
	}

	/*protected ImportData parseRecord()
			throws XMLStreamException {
		String type;
		Object value;

		while (xmlReader.hasNext()) {
			int event = xmlReader.next();
			if (event == XMLStreamConstants.START_ELEMENT) {
				String localName = xmlReader.getLocalName();

				switch (localName) {
					case RECORDS_TAG:
						patterns = new HashMap<>();
						patterns.put(DATETIME_PATTERN, xmlReader.getAttributeValue("", DATETIME_PATTERN));
						patterns.put(DATE_PATTERN, xmlReader.getAttributeValue("", DATE_PATTERN));
						break;

					case USERS_CREDENTIAL_TAG:
						patterns = new HashMap<>();
						break;

					case USER_CREDENTIAL_TAG:
						fields = new HashMap<>();
						String username = xmlReader.getAttributeValue("", USERNAME);
						previousSystemId = username;
						fields.put(USERNAME, username);
						break;

					case RECORD_TAG:
						schema = xmlReader.getAttributeValue("", SCHEMA_ATTR);
						if (schema == null) {
							schema = "default";
						}
						previousSystemId = xmlReader.getAttributeValue("", ID_ATTR);
						if (previousSystemId == null) {
							return null;
						}
						fields = new HashMap<>();
						break;

					case CONTENT_VALUE:
						fields.put("content", parseContent());
						break;
					default:
						if(localName.equals(elementTag())){
							fields = new HashMap<>();
							previousSystemId = getElementId(xmlReader);
							if(StringUtils.isBlank(previousSystemId)){
								throw new InvalidIdRuntimeException(previousSystemId);
							}
							initElementFields(previousSystemId, fields);
						}else if(localName.equals(mainElementTag())){
							patterns = new HashMap<>();
							initPatterns(xmlReader, patterns);
						}else{
							type = getType();
							value = isMultivalue() ? parseMultivalue(xmlReader.getLocalName(), type) : parseScalar(type);

							if (value != "" && !value.equals("null")) {
								fields.put(xmlReader.getLocalName(), value);
							}
						}
						break;
				}
			} else if (event == XMLStreamConstants.END_ELEMENT && (xmlReader.getLocalName().equals(RECORD_TAG)
					|| xmlReader.getLocalName().equals(USER_CREDENTIAL_TAGT))) {
				++index;
				return new ImportData(index, schema, previousSystemId, fields);
			}
		}
		return null;
	}*/

	private SimpleImportContent parseContent()
			throws XMLStreamException {
		boolean closeContent = false;
		String url;
		String fileName;
		String comment;
		boolean major;
		LocalDateTime dateTime;
		int endClose = 0;

		List<ContentImportVersion> contentVersions = new ArrayList<>();

		DateTimeFormatter datetimePattern;
		try {
			datetimePattern = DateTimeFormat.forPattern(patterns.get(DATETIME_PATTERN));
		} catch (IllegalArgumentException e) {
			datetimePattern = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
		}
		while (xmlReader.hasNext() && !closeContent) {
			int event = xmlReader.next();
			if (event == XMLStreamConstants.END_ELEMENT) {
				if (endClose == 0) {
					closeContent = true;
				}
				endClose--;
			} else if (event == XMLStreamConstants.START_ELEMENT) {
				endClose++;
				url = xmlReader.getAttributeValue("", URL_ATTR);
				fileName = xmlReader.getAttributeValue("", FILENAME_ATTR);
				comment = xmlReader.getAttributeValue("", COMMENT_ATTR);
				major = Boolean.parseBoolean(xmlReader.getAttributeValue("", MAJOR_ATTR));

				try {
					String dateTimeStr = xmlReader.getAttributeValue("", LAST_MODIFICATION_DATETIME);
					dateTime = datetimePattern.parseLocalDateTime(dateTimeStr);
				} catch (Exception exception) {
					dateTime = TimeProvider.getLocalDateTime();
				}

				contentVersions.add(new ContentImportVersion(url, fileName, major, comment, dateTime));
			}
		}

		return new SimpleImportContent(contentVersions);
	}

	//	private SimpleImportContent parseStructureContent()
	//			throws XMLStreamException {
	//		boolean closeContent = false;
	//		String structureValue;
	//		int endClose = 0;
	//
	//		List<ContentImportVersion> contentVersions = new ArrayList<>();
	//
	//		DateTimeFormatter datetimePattern;
	//		try {
	//			datetimePattern = DateTimeFormat.forPattern(patterns.get(DATETIME_PATTERN));
	//		} catch (IllegalArgumentException e) {
	//			datetimePattern = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
	//		}
	//		while (xmlReader.hasNext() && !closeContent) {
	//			int event = xmlReader.next();
	//			if (event == XMLStreamConstants.END_ELEMENT) {
	//				if (endClose == 0) {
	//					closeContent = true;
	//				}
	//				endClose--;
	//			} else if (event == XMLStreamConstants.START_ELEMENT) {
	//				endClose++;
	//				structureValue = xmlReader.getAttributeValue("", STRUCTURE_VALUE_ATTR);
	//
	//			}
	//		}
	//
	//		return new SimpleImportContent(contentVersions);
	//	}

	private Object parseMultivalue(String localName, String type)
			throws XMLStreamException {

		List<Object> values = new ArrayList<>();

		while (xmlReader.hasNext()) {

			int event = xmlReader.next();

			if (event == XMLStreamConstants.START_ELEMENT) {
				if (CONTENT_VALUE.equals(type)) {
					values.add(parseContent());
				} else {
					values.add(parseScalar(type));
				}
			} else if (event == XMLStreamConstants.END_ELEMENT) {

				if (xmlReader.getLocalName().equals(localName)) {
					break;
				}
			}
		}
		return values;
	}

	private Object parseScalar(String type)
			throws XMLStreamException {

		String content = "";

		if (!type.equals(STRUCTURE_VALUE)) {
			while (xmlReader.hasNext()) {

				int event = xmlReader.next();

				if (event == XMLStreamConstants.CHARACTERS) {
					content = xmlReader.getText().trim();
				} else if (event == XMLStreamConstants.END_ELEMENT) {
					break;
				}
			}
		}

		switch (type) {
			case DATE_VALUE:
				DateTimeFormatter datePattern = DateTimeFormat.forPattern(patterns.get(DATE_PATTERN));
				try {
					return datePattern.parseLocalDate(content);
				} catch (IllegalArgumentException exception) {
					throw new ImportDataIteratorRuntimeException.ImportDataIteratorRuntimeException_InvalidDate(
							patterns.get(DATE_PATTERN), content);
				}

			case DATETIME_VALUE:
				DateTimeFormatter datetimePattern = DateTimeFormat.forPattern(patterns.get(DATETIME_PATTERN));
				try {
					return datetimePattern.parseLocalDateTime(content);
				} catch (IllegalArgumentException exception) {
					throw new ImportDataIteratorRuntimeException.ImportDataIteratorRuntimeException_InvalidDate(
							patterns.get(DATETIME_PATTERN), content);
				}

			case STRUCTURE_VALUE:
				Map<String, String> structure = new HashMap<>();
				for (int i = 0; i < xmlReader.getAttributeCount(); i++) {
					structure.put(xmlReader.getAttributeLocalName(i), xmlReader.getAttributeValue(i));
				}
				return structure;

			default:
				if (content.isEmpty()) {
					return "";
				}
				return content;
		}
	}

	private String getType() {
		String type = xmlReader.getAttributeValue("", TYPE_ATTR);
		return Strings.isNullOrEmpty(type) ? STRING_VALUE : type;
	}

	private boolean isMultivalue() {
		String multivalue = xmlReader.getAttributeValue("", MULTIVALUE_ATTR);
		return !Strings.isNullOrEmpty(multivalue) && Boolean.parseBoolean(multivalue);
	}

	private XMLStreamReader
	newXMLStreamReader(Reader reader) {
		try {
			//			return XMLInputFactory.newInstance().createXMLStreamReader(reader);
			XMLInputFactory factory = XMLInputFactory.newInstance();
			factory.setProperty("javax.xml.stream.isCoalescing", true);  // decode entities into one string
			return factory.createXMLStreamReader(reader);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ImportDataOptions getOptions() {
		hasNext();
		return options;
	}

	@Override
	public void close() {
		closeQuietly();
	}
}