package com.constellio.app.modules.rm.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.Locale;

public class LegalRequirementReference extends RecordWrapper {
	public static final String SCHEMA_TYPE = "legalRequirementReference";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String RULE_REQUIREMENT = "ruleRequirement";
	public static final String RULE_REFERENCE = "ruleReference";
	public static final String DESCRIPTION = "description";

	public LegalRequirementReference(Record record, MetadataSchemaTypes types, Locale locale) {
		super(record, types, SCHEMA_TYPE, locale);
	}

	public LegalRequirementReference setTitle(Locale locale, String title) {
		super.set(TITLE, locale, title);
		return this;
	}

	public LegalRequirementReference setTitle(String title) {
		super.set(TITLE, title);
		return this;
	}

	public String getRequirement() {
		return get(RULE_REQUIREMENT);
	}

	public LegalRequirementReference setRequirement(String id) {
		set(RULE_REQUIREMENT, id);
		return this;
	}

	public String getReference() {
		return get(RULE_REFERENCE);
	}

	public LegalRequirementReference setReference(String id) {
		set(RULE_REFERENCE, id);
		return this;
	}

	public String getDescription() {
		return get(DESCRIPTION);
	}

	public LegalRequirementReference setDescription(String description) {
		set(DESCRIPTION, description);
		return this;
	}
}
