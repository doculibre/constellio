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

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.LazyIterator;
import com.constellio.model.services.records.bulkImport.data.ImportData;
import com.constellio.model.services.records.bulkImport.data.ImportDataIterator;
import com.constellio.model.services.records.bulkImport.data.ImportDataIteratorRuntimeException.ImportDataIteratorRuntimeException_InvalidDate;
import com.google.common.base.Strings;

public class XMLFileImportDataIterator extends LazyIterator<ImportData> implements ImportDataIterator {
	
	public static final String RECORDS_TAG = "records";
	public static final String RECORD_TAG = "record";
	public static final String ITEM_TAG = "item";
	
	public static final String TYPE_ATTR = "type";
	public static final String MULTIVALUE_ATTR = "multivalue";
	public static final String ID_ATTR = "id";
	public static final String SCHEMA_ATTR = "schema";
	
	public static final String STRING_VALUE = "string";
	public static final String CONTENT_VALUE = "content";
	public static final String DATE_VALUE = "date";
	public static final String DATETIME_VALUE = "datetime";
	
	public static final String DATE_PATTERN = "datePattern";
	public static final String DATETIME_PATTERN = "datetimePattern";

	String previousSystemId;

	private Reader reader;
	private String schema = null;
	private Map<String, Object> fields = null;
	private Map<String, String> patterns = null;
	XMLStreamReader xmlReader;

	IOServices ioServices;

	private int index;

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

		}
		ioServices.closeQuietly(reader);
	}

	private ImportData parseRecord()
			throws XMLStreamException {
		String type = null;
		Object value;
		index = 0;		 
		
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
						
					case RECORD_TAG:
						schema = xmlReader.getAttributeValue("", SCHEMA_ATTR);
						if(schema == null) {
							schema = "default";
						}
						previousSystemId = xmlReader.getAttributeValue("", ID_ATTR);
						fields = new HashMap<>();
						break;
											
					default:
						type = getType();
						value = isMultivalue() ? parseMultivalue(xmlReader.getLocalName(), type) : parseScalar(type);
	
						if (value != null) {
							fields.put(xmlReader.getLocalName(), value);
						}
		
						break;
				}
			} else if (event == XMLStreamConstants.END_ELEMENT && xmlReader.getLocalName().equals(RECORD_TAG)) {
				return new ImportData(++index, schema, previousSystemId, fields);
			}
			
		}
		return null;
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
		while (xmlReader.hasNext()) {
			int event = xmlReader.next();
			if (event == XMLStreamConstants.CHARACTERS) {
				content = xmlReader.getText().trim();
			} else if (event == XMLStreamConstants.END_ELEMENT) {
				break;
			}
		}
		
		if(content.isEmpty() || content.equals("null")) {
			return null;
		}
		
		switch (type) {		
			case DATE_VALUE:
				DateTimeFormatter datePattern = DateTimeFormat.forPattern(patterns.get(DATE_PATTERN));
				try {
					LocalDate dateValue = datePattern.parseLocalDate(content);
					return dateValue;
				} catch (IllegalArgumentException exception) {
					throw new ImportDataIteratorRuntimeException_InvalidDate(patterns.get(DATE_PATTERN), content);
				}
		
			case DATETIME_VALUE:			
				DateTimeFormatter datetimePattern = DateTimeFormat.forPattern(patterns.get(DATETIME_PATTERN));				
				try {
					LocalDateTime datetimeValue = datetimePattern.parseLocalDateTime(content);
					return datetimeValue;
				} catch (IllegalArgumentException exception) {
					throw new ImportDataIteratorRuntimeException_InvalidDate(patterns.get(DATETIME_PATTERN), content);
				}
				
			default:
				return content;
			}
	}

	private String getType() {
		String type = xmlReader.getAttributeValue("", TYPE_ATTR);
		return Strings.isNullOrEmpty(type) ? STRING_VALUE : type;
	}

	private boolean isMultivalue() {
		 String multivalue = xmlReader.getAttributeValue("", MULTIVALUE_ATTR);
	     return Strings.isNullOrEmpty(multivalue) ? false : Boolean.parseBoolean(multivalue);
	}

	private XMLStreamReader newXMLStreamReader(Reader reader) {

		try {
			XMLStreamReader streamReader = XMLInputFactory.newInstance().createXMLStreamReader(reader);
			return streamReader;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		closeQuietly();
	}
}