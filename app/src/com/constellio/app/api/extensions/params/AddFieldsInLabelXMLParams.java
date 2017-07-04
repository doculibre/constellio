package com.constellio.app.api.extensions.params;

import org.jdom2.Element;

import com.constellio.model.entities.records.Record;

public class AddFieldsInLabelXMLParams {

	private Record record;

	private Element recordElement;
	private Element metadatas;

	public AddFieldsInLabelXMLParams(Record record, Element recordElement, Element metadatas) {
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
