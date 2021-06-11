package com.constellio.app.modules.rm.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.Locale;

public class RetentionRuleDocumentType extends RecordWrapper {
	public static final String SCHEMA_TYPE = "retentionRuleDocumentType";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String RETENTION_RULE = "retentionRule";
	public static final String RETENTION_RULE_COPY = "retentionRuleCopy";
	public static final String CATEGORY = "category";
	public static final String DOCUMENT_TYPE = "documentType";

	public RetentionRuleDocumentType(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public RetentionRuleDocumentType(Record record, MetadataSchemaTypes types, Locale locale) {
		super(record, types, SCHEMA_TYPE, locale);
	}

	public RetentionRuleDocumentType setTitle(Locale locale, String title) {
		super.set(TITLE, locale, title);
		return this;
	}

	public RetentionRuleDocumentType setTitle(String title) {
		super.set(TITLE, title);
		return this;
	}

	public String getRule() {
		return get(RETENTION_RULE);
	}

	public RetentionRuleDocumentType setRule(String id) {
		set(RETENTION_RULE, id);
		return this;
	}

	public String getRuleCopy() {
		return get(RETENTION_RULE_COPY);
	}

	public RetentionRuleDocumentType setRuleCopy(String id) {
		set(RETENTION_RULE_COPY, id);
		return this;
	}

	public String getCategory() {
		return get(CATEGORY);
	}

	public RetentionRuleDocumentType setCategory(String id) {
		set(CATEGORY, id);
		return this;
	}

	public String getDocumentType() {
		return get(DOCUMENT_TYPE);
	}

	public RetentionRuleDocumentType setDocumentType(String id) {
		set(DOCUMENT_TYPE, id);
		return this;
	}
}
