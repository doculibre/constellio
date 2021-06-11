package com.constellio.app.modules.rm.wrappers;

import com.constellio.app.modules.rm.enums.TemplateVersionType;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.contents.ContentImpl;


public class Printable extends RecordWrapper {
	public static final String SCHEMA_TYPE = "printable";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String JASPERFILE = "jasperfile";
	public static final String ISDELETABLE = "isdeletable";
	public static final String DISABLED = "disabled";
	public static final String CODE = "code";

	public static final String TEMPLATE_VERSION = "templateVersion";

	public Printable(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public Printable(Record record, MetadataSchemaTypes types, String schema) {
		super(record, types, schema);
	}


	public Printable setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public Printable setJasperFile(Content file) {
		set(JASPERFILE, file);
		return this;
	}

	public ContentImpl getJasperfile() {
		return get(JASPERFILE);
	}

	public Printable setIsDeletable(Boolean isDeletable) {
		set(ISDELETABLE, isDeletable);
		return this;
	}

	public Boolean isDeletable() {
		return get(ISDELETABLE);
	}

	public TemplateVersionType getTemplateVersion() {
		return get(TEMPLATE_VERSION);
	}

	public Printable setDisabled(Boolean disabled) {
		set(DISABLED, disabled);
		return this;
	}

	public Boolean isDisabled() {
		return getBooleanWithDefaultValue(DISABLED, false);
	}

	public Printable setCode(String code) {
		set(CODE, code);
		return this;
	}

	public String getCode() {
		return get(CODE);
	}

}
