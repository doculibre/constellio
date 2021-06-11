package com.constellio.app.modules.rm.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LegalRequirement extends RecordWrapper {
	public static final String SCHEMA_TYPE = "legalRequirement";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String CODE = "code";
	public static final String TYPES = "type";
	public static final String AUTHOR = "author";
	public static final String DESCRIPTION = "description";
	public static final String CONSEQUENCES = "consequences";
	public static final String OBJECT_TYPES = "objectTypes";

	public LegalRequirement(Record record, MetadataSchemaTypes types, Locale locale) {
		super(record, types, SCHEMA_TYPE, locale);
	}

	public LegalRequirement setTitle(Locale locale, String title) {
		super.set(TITLE, locale, title);
		return this;
	}

	public LegalRequirement setTitle(String title) {
		super.set(TITLE, title);
		return this;
	}

	public String getCode() {
		return get(CODE);
	}

	public LegalRequirement setCode(String code) {
		set(CODE, code);
		return this;
	}

	public String getAuthor() {
		return get(AUTHOR);
	}

	public LegalRequirement setAuthor(String id) {
		set(AUTHOR, id);
		return this;
	}

	public String getDescription() {
		return get(DESCRIPTION);
	}

	public LegalRequirement setDescription(String description) {
		set(DESCRIPTION, description);
		return this;
	}

	public String getConsequences() {
		return get(CONSEQUENCES);
	}

	public LegalRequirement setConsequences(String consequences) {
		set(CONSEQUENCES, consequences);
		return this;
	}

	public String getObjectTypes() {
		return get(OBJECT_TYPES);
	}

	public LegalRequirement setObjectTypes(String types) {
		set(OBJECT_TYPES, types);
		return this;
	}

	public List<String> getTypes() {
		return getList(TYPES);
	}

	public LegalRequirement setTypes(List<String> types) {
		set(TYPES, types);
		return this;
	}

	public void removeType(String type) {
		removeTypes(Arrays.asList(type));
	}

	public void removeTypes(List<String> typesToRemove) {
		List<String> types = new ArrayList<>();
		types.addAll(getTypes());
		types.removeAll(typesToRemove);
		setTypes(types);
	}

	public void addType(String type) {
		addTypes(Arrays.asList(type));
	}

	public void addTypes(List<String> typesToAdd) {
		List<String> types = new ArrayList<>();
		types.addAll(getTypes());
		for (String type : typesToAdd) {
			if (!types.contains(type)) {
				types.add(type);
			}
		}
		setTypes(types);
	}
}
