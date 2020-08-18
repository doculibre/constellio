package com.constellio.app.modules.rm.model;

import com.constellio.app.modules.rm.wrappers.type.SchemaLinkingType;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.Map;

public class ExternalLinkType extends RecordWrapper implements SchemaLinkingType {

	public static final String SCHEMA_TYPE = "externalLinkType";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String LINKED_SCHEMA = "linkedSchema";
	public static final String CODE = "code";

	public ExternalLinkType(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public String getCode() {
		return get(CODE);
	}

	public ExternalLinkType setCode(String code) {
		this.set(CODE, code);
		return this;
	}

	@Override
	public ExternalLinkType setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	@Override
	public ExternalLinkType setTitles(Map<Language, String> titles) {
		return (ExternalLinkType) super.setTitles(titles);
	}

	public String getLinkedSchema() {
		return get(LINKED_SCHEMA);
	}

	public ExternalLinkType setLinkedSchema(String connectorSchema) {
		set(LINKED_SCHEMA, connectorSchema);
		return this;
	}
}
