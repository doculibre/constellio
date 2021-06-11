package com.constellio.app.modules.rm.wrappers;

import com.constellio.app.modules.rm.model.ExternalLinkType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import org.joda.time.LocalDateTime;

import java.util.Locale;

public class ExternalLink extends RecordWrapper {

	public static final String SCHEMA_TYPE = "externalLink";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String TYPE = "type";
	public static final String TYPE_CODE = "typeCode";
	public static final String IMPORTED_ON = "importedOn";
	public static final String LINKED_TO = "linkedTo";

	public ExternalLink(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public ExternalLink setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public ExternalLink setTitle(Locale locale, String title) {
		super.setTitle(locale, title);
		return this;
	}

	public String getType() {
		return get(TYPE);
	}

	public ExternalLink setType(ExternalLinkType type) {
		set(TYPE, type);
		return this;
	}

	public ExternalLink setType(String type) {
		set(TYPE, type);
		return this;
	}

	public String getLinkedto() {
		return get(LINKED_TO);
	}

	public ExternalLink setLinkedto(Folder type) {
		set(LINKED_TO, type);
		return this;
	}

	public ExternalLink setLinkedto(String type) {
		set(LINKED_TO, type);
		return this;
	}

	public ExternalLink setImportedOn(LocalDateTime importedOn) {
		set(IMPORTED_ON, importedOn);
		return this;
	}

	public LocalDateTime getImportedOn() {
		return get(IMPORTED_ON);
	}
}
