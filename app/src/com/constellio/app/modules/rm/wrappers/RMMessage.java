package com.constellio.app.modules.rm.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Message;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.List;

public class RMMessage extends Message {
	public static final String LINKED_DOCUMENTS = "linkedDocuments";
	public static final String LINKED_DOCUMENTS_TITLES = "linkedDocumentsTitles";
	public static final String HAS_LINKED_DOCUMENTS = "haslinkedDocuments";

	public RMMessage(Record record, MetadataSchemaTypes types) {
		super(record, types);
	}

	public List<String> getLinkedDocuments() {
		return get(LINKED_DOCUMENTS);
	}

	public Message setLinkedDocuments(List<String> linkedDocuments) {
		set(LINKED_DOCUMENTS, linkedDocuments);
		return this;
	}

	public List<String> getLinkedDocumentsTitles() {
		return get(LINKED_DOCUMENTS_TITLES);
	}

	public boolean hasLinkedDocuments() {
		return get(HAS_LINKED_DOCUMENTS);
	}

	public static RMMessage wrapFromMessage(Message message) {
		return new RMMessage(message.getWrappedRecord(), message.getMetadataSchemaTypes());
	}
}
