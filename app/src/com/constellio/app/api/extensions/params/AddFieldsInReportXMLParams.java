package com.constellio.app.api.extensions.params;

import com.constellio.model.entities.records.Record;
import org.jdom2.Element;

public class AddFieldsInReportXMLParams {

	private Record record;

	private Element recordElement;
	private Element metadatas;

	public AddFieldsInReportXMLParams(Record record, Element recordElement, Element metadatas) {
		this.record = record;
		this.recordElement = recordElement;
		this.metadatas = metadatas;
	}

	public Record getRecord() {
		return record;
	}

	public Element getRecordElement() {
		return recordElement;
	}

	public Element getMetadatas() {
		return metadatas;
	}

	public boolean isSchemaType(String code) {
		return record.getTypeCode().equals(code);
	}
}
