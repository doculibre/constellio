package com.constellio.app.api.extensions.params;

import org.jdom2.Element;

import com.constellio.model.entities.records.Record;

public class AddFieldInLabelXMLParams {

	private Record record;

	private Element container;
	private Element metadatas;

	public AddFieldInLabelXMLParams(Record record, Element container, Element metadatas) {
		this.record = record;
		this.container = container;
		this.metadatas = metadatas;
	}

	public Record getRecord() {
		return record;
	}

	public Element getContainer() {
		return container;
	}

	public Element getMetadatas() {
		return metadatas;
	}

	public boolean isSchemaType(String code) {
		return record.getTypeCode().equals(code);
	}
}
