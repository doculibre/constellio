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
package com.constellio.model.services.records.bulkImport.data.xml;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.LazyIterator;
import com.constellio.model.entities.records.Content;
import com.constellio.model.services.records.ContentImport;
import com.constellio.model.services.records.ContentImportVersion;
import com.constellio.model.services.records.bulkImport.data.ImportData;
import com.constellio.model.services.records.bulkImport.data.ImportDataIterator;
import com.constellio.model.services.records.bulkImport.data.ImportDataIteratorRuntimeException.ImportDataIteratorRuntimeException_InvalidDate;
import com.google.common.base.Strings;

public class XMLFileImportDataIterator extends LazyIterator<ImportData> implements ImportDataIterator {

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
	public static final String FILENAME_ATTR = "filename";
	public static final String MAJOR_ATTR = "major";

	public static final String STRING_VALUE = "string";
	public static final String CONTENT_VALUE = "content";
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
	private XMLStreamReader xmlReader;

	private IOServices ioServices;

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

	private ImportData parseRecord()
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
					type = getType();
					value = isMultivalue() ? parseMultivalue(xmlReader.getLocalName(), type) : parseScalar(type);

					if (value != "" && !value.equals("null")) {
						fields.put(xmlReader.getLocalName(), value);
					}

					break;
				}
			} else if (event == XMLStreamConstants.END_ELEMENT && (xmlReader.getLocalName().equals(RECORD_TAG)
					|| xmlReader.getLocalName().equals(USER_CREDENTIAL_TAG))) {
				++index;
				return new ImportData(index, schema, previousSystemId, fields);
			}
		}
		return null;
	}

	private ContentImport parseContent()
			throws XMLStreamException {
		boolean closeContent = false;
		String url;
		String fileName;
		boolean major;
		int endClose = 0;

		List<ContentImportVersion> contentVersions = new ArrayList<>();

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
				major = Boolean.parseBoolean(xmlReader.getAttributeValue("", MAJOR_ATTR));

				contentVersions.add(new ContentImportVersion(url, fileName, major));
			}
		}

		return new ContentImport(contentVersions);
	}

	private Object parseMultivalue(String localName, String type)
			throws XMLStreamException {

		List<Object> values = new ArrayList<>();

		while (xmlReader.hasNext()) {

			int event = xmlReader.next();

			if (event == XMLStreamConstants.START_ELEMENT) {
				values.add(parseScalar(type));
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
				throw new ImportDataIteratorRuntimeException_InvalidDate(patterns.get(DATE_PATTERN), content);
			}

		case DATETIME_VALUE:
			DateTimeFormatter datetimePattern = DateTimeFormat.forPattern(patterns.get(DATETIME_PATTERN));
			try {
				return datetimePattern.parseLocalDateTime(content);
			} catch (IllegalArgumentException exception) {
				throw new ImportDataIteratorRuntimeException_InvalidDate(patterns.get(DATETIME_PATTERN), content);
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

	private XMLStreamReader newXMLStreamReader(Reader reader) {
		try {
			return XMLInputFactory.newInstance().createXMLStreamReader(reader);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		closeQuietly();
	}
}