package com.constellio.app.modules.rm.wrappers.type;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ValueListItem;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.List;
import java.util.Map;

public class DocumentType extends ValueListItem implements SchemaLinkingType {
	public static final String SCHEMA_TYPE = "ddvDocumentType";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String LINKED_SCHEMA = "linkedSchema";
	public static final String EMAIL_DOCUMENT_TYPE = "emailDocumentType";
	public static final String TEMPLATES = "templates";
	public static final String PARENT = "parent";

	public DocumentType(Record record,
						MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public DocumentType setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	@Override
	public DocumentType setTitles(Map<Language, String> titles) {
		return (DocumentType) super.setTitles(titles);
	}

	public DocumentType setCode(String code) {
		super.setCode(code);
		return this;
	}

	public DocumentType setDescription(String description) {
		super.setDescription(description);
		return this;
	}

	public String getLinkedSchema() {
		return get(LINKED_SCHEMA);
	}

	public DocumentType setLinkedSchema(String documentSchema) {
		set(LINKED_SCHEMA, documentSchema);
		return this;
	}

	public List<Content> getTemplates() {
		return getList(TEMPLATES);
	}

	public DocumentType setTemplates(List<Content> templates) {
		set(TEMPLATES, templates);
		return this;
	}

	public String getParent() {
		return get(PARENT);
	}

	public DocumentType setParent(String id) {
		set(PARENT, id);
		return this;
	}
}
